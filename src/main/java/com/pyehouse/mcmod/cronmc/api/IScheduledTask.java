package com.pyehouse.mcmod.cronmc.api;

public interface IScheduledTask {

    String getScheduleType();
    String getScheduleData();
    String getTaskType();
    String getTaskData();
    boolean isValid();

}
