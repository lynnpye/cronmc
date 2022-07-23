package com.pyehouse.mcmod.cronmc.api.task;

import com.pyehouse.mcmod.cronmc.api.Cronmc;
import com.pyehouse.mcmod.cronmc.api.ScheduledTask;
import com.pyehouse.mcmod.cronmc.api.TaskHandler;
import com.pyehouse.mcmod.cronmc.api.schedule.CronTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.ExceptionUtils;

public class RunnableHandler extends TaskHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String HANDLER_ID = "runnable";

    @Override
    public boolean handlesTaskType(ScheduledTask scheduledTask) {
        if (scheduledTask == null || !scheduledTask.isValid() || !HANDLER_ID.equals(scheduledTask.getTaskType())) {
            return false;
        }

        boolean handles = false;
        try {
            String runnableClassName = scheduledTask.getTaskData();
            Class<?> clazz = Class.forName(runnableClassName);
            handles = Runnable.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        return handles;
    }

    @Override
    public void handleScheduledTask(ScheduledTask scheduledTask) {
        if (scheduledTask == null || !scheduledTask.isValid()) {
            LOGGER.warn("Tried to handle null or invalid task");
            return;
        }

        try {
            String runnableClassName = scheduledTask.getTaskData();
            Class<?> clazz = Class.forName(runnableClassName);
            if (Runnable.class.isAssignableFrom(clazz)) {
                Runnable runnable = (Runnable) clazz.newInstance();
                Cronmc.get().launch(new CronTask(scheduledTask, runnable));
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

    }

    public static class TestRunnable implements Runnable {

        @Override
        public void run() {
            Cronmc.get().opSay("[Cronmc/TestRunnable] (Safely delete this task) Cronmc is running a Runnable %s",
                    this.getClass().getName()
            );

            try {
                Thread.sleep(120000);
            } catch (InterruptedException e) {
            }

            Cronmc.get().opSay("[Cronmc/TestRunnable] (Safely delete this task) Cronmc is done running Runnable %s",
                        this.getClass().getName()
            );
        }
    }

    public static class TestRunnable2 implements Runnable {

        @Override
        public void run() {
            Cronmc.get().opSay("[Cronmc/TestRunnable2] (Safely delete this task) Cronmc executing <<<<<");
        }
    }

}
