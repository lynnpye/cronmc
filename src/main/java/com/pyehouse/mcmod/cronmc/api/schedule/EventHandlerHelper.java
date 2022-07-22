package com.pyehouse.mcmod.cronmc.api.schedule;

import com.pyehouse.mcmod.cronmc.api.ScheduledTask;
import com.pyehouse.mcmod.cronmc.api.Cronmc;
import com.pyehouse.mcmod.cronmc.api.registry.ScheduleTypeRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventHandlerHelper {
    private static final Logger LOGGER = LogManager.getLogger();

    private static boolean started = false;

    public static final String MOD = "mod";
    public static final String FORGE = "forge";

    public static void stop() { started = false; }
    public static void start() { started = true; }

    public static void register(IEventBus modEventBus, IEventBus forgeEventBus) {

        forgeEventBus.register(EventHandlerHelper.class);

        modEventBus.register(ModBusHandler.class);
        forgeEventBus.register(ForgeBusHandler.class);
    }

    private static void fireForEvent(Class<?> eventClass) {
        if (!started) return;

        // iterate the scheduledTasks in config
        for (String scheduleString : Cronmc.get().getScheduleStrings()) {
            ScheduledTask scheduledTask = new ScheduledTask(scheduleString);
            // check if EventHandler can handle it
            if (!ScheduleTypeRegistry.EVENT_HANDLER.get().handlesScheduleType(scheduledTask)) {
                continue;
            }
            // check if the scheduleData (i.e. event to fire) matches the eventClass
            HandledEvent handledEvent = getHandledEvent(scheduledTask);
            if (handledEvent.handlesEvent(eventClass)) {
                // iterate the task handlers to see who can deal with this
                Cronmc.get().performAllTaskTypeMatches(scheduledTask);
            }
        }
    }

    private static HandledEvent getHandledEvent(ScheduledTask scheduledTask) {
        for (HandledEvent handledEvent : HandledEvent.values()) {
            if (handledEvent.name().equals(scheduledTask.getScheduleData())) {
                return handledEvent;
            }
        }
        return null;
    }

    /*
    public static <T extends Event> Object createEventHandler(TypeReference<T> typeReference, IEventBus eventBus) {
        try {
            LOGGER.error(String.format("\n\ncreating event handler '%s'\n\n", typeReference.getTypeClass().getName()));
        } catch (ClassNotFoundException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }

        Object o = new Object();
        try {
            o = new Object() {
                Class<?> typeClass = typeReference.getTypeClass();

                @SubscribeEvent
                public void handleEvent(T event) {
                    LOGGER.error(String.format("\n\nhandling event '%s'\n\n", event.getClass().getName()));
                    if (event.getClass().equals(typeClass)) {
                        fireForEvent(typeClass);
                    }
                }
            };
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        eventBus.register(o);

        return o;
    }
     */

    public static class ModBusHandler {

    }

    public static class ForgeBusHandler {

        @SubscribeEvent
        public static void serverStarted(FMLServerStartedEvent event) {
            fireForEvent(event.getClass());
        }
    }
}
