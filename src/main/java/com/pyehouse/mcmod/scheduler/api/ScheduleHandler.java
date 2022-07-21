package com.pyehouse.mcmod.scheduler.api;

import net.minecraftforge.registries.ForgeRegistryEntry;

public class ScheduleHandler extends ForgeRegistryEntry<ScheduleHandler> {
    public boolean handlesScheduleType(ScheduledTask scheduledTask) { return false; }
    public void handleScheduledTask(ScheduledTask scheduledTask) {}
}
