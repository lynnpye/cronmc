package com.pyehouse.mcmod.scheduler.api.schedule;

import com.pyehouse.mcmod.scheduler.api.ScheduleHandler;
import com.pyehouse.mcmod.scheduler.api.ScheduledTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventHandler extends ScheduleHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String HANDLER_ID = "event";

    @Override
    public boolean handlesScheduleType(ScheduledTask scheduledTask) {
        if (scheduledTask == null || !scheduledTask.isValid() || !HANDLER_ID.equals(scheduledTask.getScheduleType())) {
            return false;
        }
        for (HandledEvent handledEvent : HandledEvent.values()) {
            if (scheduledTask.getScheduleData().equals(handledEvent.name())) {
                return true;
            }
        }
        return false;
    }

    /**
     * This is a NOP. EventHandler is hooked into the events that it knows and
     * doesn't actually 'handle' a scheduled task.
     *
     * @param scheduledTask
     */
    @Override
    public void handleScheduledTask(ScheduledTask scheduledTask) {
    }



}
