package com.pyehouse.mcmod.cronmc.api.schedule;

import com.pyehouse.mcmod.cronmc.api.Cronmc;
import com.pyehouse.mcmod.cronmc.api.ScheduleHandler;
import com.pyehouse.mcmod.cronmc.api.ScheduledTask;
import it.sauronsoftware.cron4j.SchedulingPattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CronHandler extends ScheduleHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String HANDLER_ID = "cron";

    @Override
    public boolean handlesScheduleType(ScheduledTask scheduledTask) {
        if (!HANDLER_ID.equals(scheduledTask.getScheduleType())) {
            return false;
        }

        if (!SchedulingPattern.validate(scheduledTask.getScheduleData())) {
            LOGGER.warn(String.format("Rejecting scheduledTask '%s' because the cron pattern was invalid", scheduledTask));
            return false;
        }

        return true;
    }

    @Override
    public void handleScheduledTask(ScheduledTask scheduledTask) {
        if (scheduledTask == null || !scheduledTask.isValid()) {
            LOGGER.warn("Tried to handle an invalid schedule (null or invalid)");
            return;
        }

        String pattern = scheduledTask.getScheduleData();

        if (!SchedulingPattern.validate(pattern)) {
            LOGGER.warn(String.format("Rejecting scheduledTask '%s' because the cron pattern was invalid '%s'", scheduledTask, pattern));
            return;
        }

        Cronmc.get().schedule(scheduledTask.getScheduleData(), new CronTask(scheduledTask));
    }

}
