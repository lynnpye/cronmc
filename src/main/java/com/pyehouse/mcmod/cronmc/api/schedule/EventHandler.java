package com.pyehouse.mcmod.cronmc.api.schedule;

import com.pyehouse.mcmod.cronmc.api.ScheduleHandler;
import com.pyehouse.mcmod.cronmc.api.ScheduledTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.ExceptionUtils;

public class EventHandler extends ScheduleHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String HANDLER_ID = "event";

    @Override
    public boolean handlesScheduleType(ScheduledTask scheduledTask) {
        if (scheduledTask == null || !scheduledTask.isValid() || !HANDLER_ID.equals(scheduledTask.getScheduleType())) {
            return false;
        }
        for (HandledEvents.HandledEvent handledEvent : HandledEvents.HandledEvent.values()) {
            if (scheduledTask.getScheduleData().equals(handledEvent.name())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleScheduledTask(ScheduledTask scheduledTask) {
        if (scheduledTask == null || !scheduledTask.isValid()) {
            LOGGER.warn("Tried to handle an invalid schedule (null or invalid)");
            return;
        }

        try {
            HandledEvents.HandledEvent handledEvent = HandledEvents.handledEventFromScheduledTask(scheduledTask);
            EventHandlerHelper.add(handledEvent, scheduledTask);
            handledEvent.register();
        } catch (IllegalArgumentException e) {
            LOGGER.error(String.format("Cronmc unable to handle the event from scheduledTask %s :Exception: %s",
                    scheduledTask, ExceptionUtils.getStackTrace(e)));
        }
    }



}
