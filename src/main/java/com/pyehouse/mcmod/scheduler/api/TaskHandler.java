package com.pyehouse.mcmod.scheduler.api;

import net.minecraftforge.registries.ForgeRegistryEntry;

public class TaskHandler extends ForgeRegistryEntry<TaskHandler> {
    public boolean handlesTaskType(ScheduledTask scheduledTask) { return false; }
    public void handleScheduledTask(ScheduledTask scheduledTask) {}
}
