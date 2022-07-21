package com.pyehouse.mcmod.scheduler.server.handler;

import com.pyehouse.mcmod.scheduler.api.Scheduler;
import com.pyehouse.mcmod.scheduler.api.schedule.CronHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class ServerConfigHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    public static class ServerConfig {
        public final ForgeConfigSpec.ConfigValue<String> cronTimeZone;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> schedules;

        public ServerConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Scheduler");

            builder.push("Cron TimeZone");
            cronTimeZone = builder
                    .comment("If possible, this timezone will be used for scheduling; otherwise defaults to system timezone '%s'")
                            .define("cronTimeZone", TimeZone.getDefault().getID(), CronHandler::isTimeZoneValid);
            builder.pop();

            builder.push("Schedules");
            schedules = builder
                    .comment("A string in the form of <schedule type>:<schedule data>|<task type>:<task data>")
                    .defineList("scheduledTasks", Arrays.asList(
                            "event:serverStarted|op:say Scheduler here, letting you know the server is up.",
                            "event:serverStarted|runnable:com.pyehouse.mcmod.scheduler.api.task.RunnableHandler$TestRunnable"
                    ), Scheduler::isValidSchedule);
            builder.pop();
        }

    }

    @SubscribeEvent
    public static void onConfigUpdate(ModConfig.ModConfigEvent event) {

        String timezoneId = SERVER.cronTimeZone.get();

        LOGGER.info(String.format("%s config updated", event.getConfig().getFileName()));

        if (!CronHandler.isTimeZoneValid(timezoneId)) {
            LOGGER.error(String.format("Attempting to assign invalid TimeZone '%s'", timezoneId));
            return;
        }

        Scheduler.setCronTimeZone(TimeZone.getTimeZone(timezoneId));
    }
}
