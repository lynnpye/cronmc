package com.pyehouse.mcmod.scheduler.api;

public interface IScheduledTask {

    String getScheduleType();
    String getScheduleData();
    String getTaskType();
    String getTaskData();
    boolean isValid();

}
