package com.pyehouse.mcmod.scheduler.api;

import net.minecraft.util.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScheduledTask implements IScheduledTask {
    private static final Logger LOGGER = LogManager.getLogger();

    private String scheduleType;
    private String scheduleData;
    private String taskType;
    private String taskData;
    private boolean valid;

    public ScheduledTask() {
        setScheduledTask(null);
    }

    public ScheduledTask(String scheduledTask) {
        setScheduledTask(scheduledTask);
    }

    @Override
    public String getScheduleType() {
        return scheduleType;
    }

    @Override
    public String getScheduleData() {
        return scheduleData;
    }

    @Override
    public String getTaskType() {
        return taskType;
    }

    @Override
    public String getTaskData() {
        return taskData;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    /**
     * This is not a very intelligent setter. It will, broadly speaking, try to chop the string up
     * into the two chunks, a schedule and a task. These are pipe delimited '|'. Each is then further
     * delimited by a colon ':' into the type and the info.
     *
     * The format is:
     *   <schedule type>:<schedule data>|<task type>:<task data>
     *
     * So, something like:
     *   event:serverStarted | op:say server was started
     * should presumably result in an op call to /say server was started when the forgeEventBus fires ServerStarted.
     * The schedule and task are pipe delimited, and each is prefixed by its type and a colon.
     * The rest is interpreted by the handler for each type.
     *
     * @param scheduledTask
     */
    public void setScheduledTask(String scheduledTask) {
        this.valid = false;

        Tuple<Tuple<String, String>, Tuple<String, String>> tuple = Scheduler.splitScheduledTask(scheduledTask);
        if (tuple == null) {
            LOGGER.warn("splitScheduledTask(String) returned null, not setting scheduledTask");
            this.scheduleType = null;
            this.taskType = null;
            return;
        }

        this.scheduleType = tuple.getA().getA();
        this.scheduleData = tuple.getA().getB();
        this.taskType = tuple.getB().getA();
        this.taskData = tuple.getB().getB();
        this.valid = true;
    }


    @Override
    public String toString() {
        return String.format("%s:%s|%s:%s", this.scheduleType, this.scheduleData, this.taskType, this.taskData);
    }

}
