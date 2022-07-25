package com.pyehouse.mcmod.cronmc.api;

public class TaskHandler  {
    public boolean handlesTaskType(ScheduledTask scheduledTask) { return false; }
    public void handleScheduledTask(ScheduledTask scheduledTask) {}
}
