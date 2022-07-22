package com.pyehouse.mcmod.cronmc.api.task;

import com.pyehouse.mcmod.cronmc.api.Cronmc;
import com.pyehouse.mcmod.cronmc.api.ScheduledTask;
import com.pyehouse.mcmod.cronmc.api.TaskHandler;
import com.pyehouse.mcmod.cronmc.api.schedule.CronTask;
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
    public void handleScheduledTask(final ScheduledTask scheduledTask) {
        Runnable opRunner = new Runnable() {
            @Override
            public void run() {
                Cronmc.get().opCommand(scheduledTask.getTaskData());
            }
        };

        Cronmc.get().launch(new CronTask(scheduledTask, opRunner));
    }
}
