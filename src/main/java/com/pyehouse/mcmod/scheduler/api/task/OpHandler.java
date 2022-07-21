package com.pyehouse.mcmod.scheduler.api.task;

import com.pyehouse.mcmod.scheduler.api.ScheduledTask;
import com.pyehouse.mcmod.scheduler.api.TaskHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpHandler extends TaskHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String HANDLER_ID = "op";

    @Override
    public boolean handlesTaskType(ScheduledTask scheduledTask) {
        if (scheduledTask == null || !scheduledTask.isValid() || !HANDLER_ID.equals(scheduledTask.getTaskType())) {
            return false;
        }
        return true;
    }

    @Override
    public void handleScheduledTask(ScheduledTask scheduledTask) {
        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> new DistExecutor.SafeRunnable() {
            final String taskData = scheduledTask.getTaskData();
            @Override
            public void run() {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                server.getCommands().performCommand(server.createCommandSourceStack(), taskData);
            }
        });
    }
}
