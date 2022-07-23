package com.pyehouse.mcmod.cronmc.api.schedule;

import com.pyehouse.mcmod.cronmc.api.ScheduledTask;
import com.pyehouse.mcmod.cronmc.api.Cronmc;
import net.minecraftforge.eventbus.api.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventHandlerHelper {
    private static final Logger LOGGER = LogManager.getLogger();

    private static boolean started = false;
    private static final Map<HandledEvents.HandledEvent, List<ScheduledTask>> eventTaskListMap =
            new HashMap<>();

    public static void stop(boolean clear) {
        if (clear) {
            clearHandledEvents();
        }
        started = false;
    }
    public static void start() { started = true; }

    public static void fireForEvent(Event event, HandledEvents.HandledEvent handledEvent) {
        if (!started) return;

        // got any scheduledtasks recorded?
        List<ScheduledTask> scheduledTaskList = eventTaskListMap.get(handledEvent);
        if (scheduledTaskList == null) {
            return;
        }

        for (ScheduledTask scheduledTask : scheduledTaskList) {
            // iterate the task handlers to see who can deal with this
            Cronmc.get().performAllTaskTypeMatches(scheduledTask);
        }
    }

    public static void add(HandledEvents.HandledEvent handledEvent, ScheduledTask scheduledTask) {
        List<ScheduledTask> scheduledTaskList = eventTaskListMap.containsKey(handledEvent)
                ? eventTaskListMap.get(handledEvent)
                : new ArrayList<>();
        eventTaskListMap.putIfAbsent(handledEvent, scheduledTaskList);
        scheduledTaskList.add(scheduledTask);
    }

    public static void clearHandledEvents() {
        for (HandledEvents.HandledEvent handledEvent : HandledEvents.HandledEvent.values()) {
            handledEvent.deregister();
        }
        eventTaskListMap.clear();
    }
}
