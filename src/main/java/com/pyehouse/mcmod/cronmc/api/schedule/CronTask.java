package com.pyehouse.mcmod.cronmc.api.schedule;

import com.pyehouse.mcmod.cronmc.api.Cronmc;
import com.pyehouse.mcmod.cronmc.api.ScheduledTask;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CronTask extends Task implements ICronTask {
    private static final Logger LOGGER = LogManager.getLogger();

    private enum CronTaskMode {
        UNINITIALIZED,
        SCHEDULED_TASK,
        RUNNABLE;
    }

    private final ScheduledTask scheduledTask;
    public final Runnable runnable;
    private TaskExecutionContext taskExecutionContext;
    private CronTaskMode cronTaskMode = CronTaskMode.UNINITIALIZED;

    public CronTask(final ScheduledTask scheduledTask) {
        this(scheduledTask, null, CronTaskMode.SCHEDULED_TASK);
    }

    public CronTask(final ScheduledTask scheduledTask, Runnable runnable) {
        this(scheduledTask, runnable, CronTaskMode.RUNNABLE);
    }

    public CronTask(final ScheduledTask scheduledTask, Runnable runnable, CronTaskMode cronTaskMode) {
        this.cronTaskMode = cronTaskMode;
        if (cronTaskMode == CronTaskMode.RUNNABLE && runnable == null) {
            throw new IllegalArgumentException("Cannot schedule a null Runnable");
        }
        if (cronTaskMode == CronTaskMode.SCHEDULED_TASK && (scheduledTask == null || !scheduledTask.isValid())) {
            throw new IllegalArgumentException("Cannot schedule a null or invalid ScheduledTask");
        }
        this.scheduledTask = scheduledTask;
        this.runnable = runnable;
    }

    @Override
    public String getCronString() {
        return scheduledTask.toString();
    }

    @Override
    public boolean canBeStopped() {
        return true;
    }

    @Override
    public void execute(TaskExecutionContext context) throws RuntimeException {
        this.taskExecutionContext = context;
        switch (this.cronTaskMode) {
            case UNINITIALIZED:
                throw new IllegalStateException("Unable to execute uninitialized CronTask.");

            case RUNNABLE:
                executeRunnable(this.runnable);
                break;

            case SCHEDULED_TASK:
                executeScheduledTask(this.scheduledTask);
                break;
        }
    }

    private void executeScheduledTask(ScheduledTask performableScheduleTask) {
        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                Cronmc.get().performAllTaskTypeMatches(performableScheduleTask);
            }
        });

    }

    private void executeRunnable(Runnable executableRunnable) {
        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                executableRunnable.run();
            }
        });
    }
}
