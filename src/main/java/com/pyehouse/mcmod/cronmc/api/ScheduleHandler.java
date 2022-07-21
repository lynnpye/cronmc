package com.pyehouse.mcmod.cronmc.api;

import net.minecraftforge.registries.ForgeRegistryEntry;

public class ScheduleHandler extends ForgeRegistryEntry<ScheduleHandler> {
    public boolean handlesScheduleType(ScheduledTask scheduledTask) { throw new IllegalStateException("handlesScheduleType not implemented"); }
    public void handleScheduledTask(ScheduledTask scheduledTask) { throw new IllegalStateException("handleScheduledTask not implemented"); }
}
