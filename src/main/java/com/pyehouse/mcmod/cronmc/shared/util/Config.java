package com.pyehouse.mcmod.cronmc.shared.util;

import com.pyehouse.mcmod.cronmc.api.Cronmc;
import com.pyehouse.mcmod.cronmc.api.util.CronmcHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
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

    static {
        final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    public static class ServerConfig {
        public final ForgeConfigSpec.BooleanValue startOnServerStart;
        public final ForgeConfigSpec.ConfigValue<String> cronTimeZone;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> schedules;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Cronmc");

            builder.push("Start Cronmc On Server Start");
            startOnServerStart = builder
                    .comment("Start Cronmc on server start")
                            .define("startOnServerStart", true);
            builder.pop();

            builder.push("Cron TimeZone");
            cronTimeZone = builder
                    .comment("If possible, this timezone will be used for scheduling; otherwise defaults to system timezone '%s'")
                            .define("cronTimeZone", TimeZone.getDefault().getID(), CronmcHelper::isCronTimeZoneValid);
            builder.pop();

            builder.push("Schedules");
            schedules = builder
                    .comment("A string in the form of <schedule type>:<schedule data>|<task type>:<task data>")
                    .defineList("scheduledTasks", Arrays.asList(
                            "event:serverStarted|op:say Cronmc here, letting you know the server is up.",
                            "event:serverStarted|runnable:com.pyehouse.mcmod.cronmc.api.task.RunnableHandler$TestRunnable",
                            "cron:* * * * *|runnable:com.pyehouse.mcmod.cronmc.api.task.RunnableHandler$TestRunnable",
                            "cron:* * * * *|runnable:com.pyehouse.mcmod.cronmc.api.task.RunnableHandler$TestRunnable2",
                            "event:serverStarted|op:say Cronmc here, with a cron-based op command"
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

    private static boolean allowUpdateCronmc = false;
    public static void updateCronmc() {
        if (!allowUpdateCronmc) return;

        LOGGER.info("Updating Cronmc with latest server config");

        String timezoneId = SERVER.cronTimeZone.get();
        TimeZone cronTimeZone = null;

        if (!CronmcHelper.isCronTimeZoneValid(timezoneId)) {
            cronTimeZone = TimeZone.getDefault();
            LOGGER.warn(String.format("\n\nInvalid TimeZone '%s' in cronmc-server.toml, changing to system default '%s'\n\n"
                    , timezoneId, cronTimeZone.getID()));
        } else {
            cronTimeZone = TimeZone.getTimeZone(timezoneId);
        }

        // introduce the new schedule
        Cronmc.get().resetSchedule(SERVER.startOnServerStart.get(), cronTimeZone, getSchedules());

    }

    @SubscribeEvent
    public static void onServerStarted(FMLServerStartedEvent event) {
        allowUpdateCronmc = true;
        updateCronmc();
    }

    @SubscribeEvent
    public static void onConfigUpdate(ModConfig.ModConfigEvent event) {
        updateCronmc();
    }
}
