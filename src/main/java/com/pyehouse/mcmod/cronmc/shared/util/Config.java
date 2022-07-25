package com.pyehouse.mcmod.cronmc.shared.util;

import com.pyehouse.mcmod.cronmc.api.Cronmc;
import com.pyehouse.mcmod.cronmc.api.util.CronmcHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class Config {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    public static final String VAR_START_ON_SERVER_START = "startOnServerStart";
    public static final String VAR_CONSOLE_MESSAGE_MIN_PERMISSION_LEVEL = "consoleMessageMinPermissionLevel";
    public static final String VAR_SCHEDULED_TASKS = "scheduledTasks";
    public static final String VAR_CRON_TIME_ZONE = "cronTimeZone";
    public static final String VAR_OUTPUT_TO_CONSOLE = "outputToConsole";

    static {
        final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    public static class ServerConfig {
        public final ForgeConfigSpec.BooleanValue startOnServerStart;
        public final ForgeConfigSpec.ConfigValue<String> cronTimeZone;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> schedules;
        public final ForgeConfigSpec.IntValue consoleMessageMinPermissionLevel;
        public final ForgeConfigSpec.BooleanValue outputToConsole;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Cronmc");

            builder.push("Start Cronmc On Server Start");
            startOnServerStart = builder
                    .comment("Start Cronmc on server start")
                            .define(VAR_START_ON_SERVER_START, true);
            builder.pop();

            builder.push("Output To Console");
            outputToConsole = builder
                    .comment("Output Cronmc messages to console as well as [INFO] messages in the server log.")
                            .define(VAR_OUTPUT_TO_CONSOLE, false);
            builder.pop();

            builder.push("Minimum Permission Level to see Cronmc Console Messages");
            consoleMessageMinPermissionLevel = builder
                    .comment(String.format("Only functions if output to console is enabled. Specify any number from 0-4, inclusive. 0 is no permissions, 4 is max. See https://minecraft.fandom.com/wiki/Permission_level for more details"))
                            .defineInRange(VAR_CONSOLE_MESSAGE_MIN_PERMISSION_LEVEL, 4, 0, 4);
            builder.pop();

            builder.push("Cron TimeZone");
            cronTimeZone = builder
                    .comment(String.format("If possible, this timezone will be used for scheduling; otherwise defaults to system timezone '%s'", TimeZone.getDefault().getID()))
                            .define(VAR_CRON_TIME_ZONE, TimeZone.getDefault().getID(), CronmcHelper::isCronTimeZoneValid);
            builder.pop();

            builder.push("Schedules");
            schedules = builder
                    .comment("A string in the form of <schedule type>:<schedule data>|<task type>:<task data>")
                    .defineList(VAR_SCHEDULED_TASKS, Arrays.asList(
                            "event:FML_SERVER_STARTED_EVENT|op:say Cronmc here, letting you know the server is up.",
                            "event:FML_SERVER_STARTED_EVENT|runnable:com.pyehouse.mcmod.cronmc.api.task.RunnableHandler$TestRunnable",
                            "cron:* * * * *|runnable:com.pyehouse.mcmod.cronmc.api.task.RunnableHandler$TestRunnable",
                            "cron:* * * * *|runnable:com.pyehouse.mcmod.cronmc.api.task.RunnableHandler$TestRunnable2",
                            "event:FML_SERVER_STARTED_EVENT|op:say Cronmc here, with a cron-based op command",
                            "event:PLAYER_CHANGE_GAME_MODE_EVENT|op:say Player changed game mode"
                    ), CronmcHelper::isValidSchedule);
            builder.pop();
        }

    }

    public static void setCronTimeZone(TimeZone cronTimeZone) {
        if (cronTimeZone == null) {
            cronTimeZone = TimeZone.getDefault();
        }
        SERVER.cronTimeZone.set(cronTimeZone.getID());

        updateCronmc();
    }

    public static String[] getSchedules() {
        return Arrays.copyOf(SERVER.schedules.get().toArray(), SERVER.schedules.get().size(), String[].class);
    }

    private final static Object updateCronmcSyncObj = new Object();
    private static Boolean allowUpdateCronmc = false;
    public static void updateCronmc() {
        synchronized (updateCronmcSyncObj) {
            if (!allowUpdateCronmc) return;

            LOGGER.info("Updating Cronmc with latest server config");

            String timezoneId = SERVER.cronTimeZone.get();
            TimeZone cronTimeZone = null;

            if (!CronmcHelper.isCronTimeZoneValid(timezoneId)) {
                cronTimeZone = TimeZone.getDefault();
                LOGGER.warn(String.format("Invalid TimeZone '%s' in cronmc-server.toml, changing to system default '%s'"
                        , timezoneId, cronTimeZone.getID()));
            } else {
                cronTimeZone = TimeZone.getTimeZone(timezoneId);
            }

            // introduce the new schedule
            Cronmc.get().resetSchedule(SERVER.startOnServerStart.get(), cronTimeZone, getSchedules());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        allowUpdateCronmc = true;
        updateCronmc();
    }

    @SubscribeEvent
    public static void onConfigUpdate(ModConfigEvent event) {
        updateCronmc();
    }
}
