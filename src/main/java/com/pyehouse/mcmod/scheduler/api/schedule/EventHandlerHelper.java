package com.pyehouse.mcmod.scheduler.api.schedule;

import com.pyehouse.mcmod.scheduler.api.ScheduledTask;
import com.pyehouse.mcmod.scheduler.api.Scheduler;
import com.pyehouse.mcmod.scheduler.api.registry.ScheduleTypeRegistry;
import com.pyehouse.mcmod.scheduler.server.handler.ServerConfigHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventHandlerHelper {
    private static final Logger LOGGER = LogManager.getLogger();

    private static boolean started = false;

    public static final String MOD = "mod";
    public static final String FORGE = "forge";

    public static void stop() { started = false; }
    public static void start() { started = true; }

    @SubscribeEvent
    public static void serverStopping(FMLServerStoppingEvent event) {
        Scheduler.stop();
    }

    public static void register(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.register(ModBusHandler.class);
        forgeEventBus.register(ForgeBusHandler.class);
    }

    private static void fireForEvent(Class<?> eventClass) {
        if (!started) return;

        // iterate the scheduledTasks in config
        for (String scheduleString : ServerConfigHandler.SERVER.schedules.get()) {
            ScheduledTask scheduledTask = new ScheduledTask(scheduleString);
            // check if EventHandler can handle it
            if (!ScheduleTypeRegistry.EVENT_HANDLER.get().handlesScheduleType(scheduledTask)) {
                continue;
            }
            // check if the scheduleData (i.e. event to fire) matches the eventClass
            HandledEvent handledEvent = getHandledEvent(scheduledTask);
            if (handledEvent.handlesEvent(eventClass)) {
                // iterate the task handlers to see who can deal with this
                Scheduler.perform(scheduledTask);
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
    public static <T extends Event> void createEventHandler(TypeReference<T> typeReference, IEventBus eventBus) {
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
    }
     */

    public static class ModBusHandler {

    }

    public static class ForgeBusHandler {

        @SubscribeEvent
        public static void serverStarted(FMLServerStartedEvent event) {
            LOGGER.error("\n\nSERVER STARTED\n\n");
            fireForEvent(event.getClass());
        }
    }
}
