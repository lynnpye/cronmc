package com.pyehouse.mcmod.cronmc.api.schedule;

import com.pyehouse.mcmod.cronmc.api.ScheduleHandler;
import com.pyehouse.mcmod.cronmc.api.ScheduledTask;
import com.pyehouse.mcmod.cronmc.api.Cronmc;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.TimeZone;

public class CronHandler extends ScheduleHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String HANDLER_ID = "cron";

    private static Scheduler cron4j = null;

    public static Scheduler cron() {
        if (cron4j == null) {
            cron4j = new Scheduler();
        }
        return cron4j;
    }

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

        if (!SchedulingPattern.validate(scheduledTask.getScheduleData())) {
            LOGGER.warn(String.format("Rejecting scheduledTask '%s' because the cron pattern was invalid", scheduledTask));
            return;
        }

        scheduleTask(scheduledTask.getScheduleData(), new CronTask(scheduledTask));
    }

    public static void scheduleTask(String schedule, Task task) {
        cron().schedule(schedule, task);
    }

    public static boolean isTimeZoneValid(Object timeZone) {
        for (String availableId : TimeZone.getAvailableIDs()) {
            if (availableId.equals(timeZone)) {
                return true;
            }
        }
        return false;
    }

    public static void setCronTimeZone(TimeZone timeZone) {
        if (timeZone == null) {
            LOGGER.error("Cannot assign null TimeZone to cron");
            return;
        }
        LOGGER.info(String.format("Current cron timezone is '%s'", CronHandler.cron().getTimeZone().getID()));

        cron().setTimeZone(timeZone);

        LOGGER.info(String.format("Updated cron timezone to '%s'", CronHandler.cron().getTimeZone().getID()));
    }

    public static void stop() {
        if (cron4j == null || !cron4j.isStarted()) {
            return;
        }
        LOGGER.info("Attempting to stop cron handler and jobs");
        cron4j.stop();
        LOGGER.info("Cron handler and jobs stopped");
    }

    public static void start() {
        if (!cron().isStarted()) {
            cron().start();
        }
    }

    public static class CronTask extends Task {

        public final ScheduledTask scheduledTask;
        private TaskExecutionContext taskExecutionContext;

        public CronTask(ScheduledTask scheduledTask) {
            if (scheduledTask == null || !scheduledTask.isValid()) {
                throw new IllegalArgumentException("Cannot schedule a null or invalid ScheduledTask");
            }
            this.scheduledTask = scheduledTask;
        }


        @Override
        public void execute(TaskExecutionContext context) throws RuntimeException {
            this.taskExecutionContext = context;
            DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> new DistExecutor.SafeRunnable() {
                final ScheduledTask crontask = scheduledTask;
                @Override
                public void run() {
                    Cronmc.get().perform(crontask);
                }
            });
        }
    }

}
