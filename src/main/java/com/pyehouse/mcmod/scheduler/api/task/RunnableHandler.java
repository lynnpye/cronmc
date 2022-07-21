package com.pyehouse.mcmod.scheduler.api.task;

import com.pyehouse.mcmod.scheduler.api.ScheduledTask;
import com.pyehouse.mcmod.scheduler.api.TaskHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
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
        if (scheduledTask == null | !scheduledTask.isValid()) {
            LOGGER.warn("Tried to handle null or invalid task");
            return;
        }

        try {
            String runnableClassName = scheduledTask.getTaskData();
            Class clazz = Class.forName(runnableClassName);
            if (Runnable.class.isAssignableFrom(clazz)) {
                Runnable runnable = (Runnable) clazz.newInstance();
                DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> new DistExecutor.SafeRunnable() {
                    final Runnable runner = runnable;
                    @Override
                    public void run() {
                        this.runner.run();
                    }
                });
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

    }

    public static class TestRunnable implements Runnable {

        @Override
        public void run() {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            server.getCommands().performCommand(server.createCommandSourceStack(), "say Scheduler is running a TestRunnable");
        }
    }

}
