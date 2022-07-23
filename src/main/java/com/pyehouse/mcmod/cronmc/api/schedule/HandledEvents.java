package com.pyehouse.mcmod.cronmc.api.schedule;

import com.pyehouse.mcmod.cronmc.CronmcMod;
import com.pyehouse.mcmod.cronmc.api.ScheduledTask;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class HandledEvents {
    private HandledEvents() {}

    private static final Logger LOGGER = LogManager.getLogger();

    public static final Set<HandledEvent> eventConsumerSet = new HashSet<>();

    public static HandledEvent handledEventFromScheduledTask(ScheduledTask scheduledTask) {
        return HandledEvent.valueOf(scheduledTask.getScheduleData());
    }

    public static final Consumer<FMLServerStartedEvent> FML_SERVER_STARTED_EVENT_CONSUMER =
        new Consumer<FMLServerStartedEvent>() {
            @SubscribeEvent
            @Override
            public void accept(FMLServerStartedEvent event) {
                EventHandlerHelper.fireForEvent(event, HandledEvent.FML_SERVER_STARTED_EVENT);
            }
        };
    public static final Consumer<CommandEvent> COMMAND_EVENT_CONSUMER = event -> {
        new Consumer<CommandEvent>() {
            @SubscribeEvent
            @Override
            public void accept(CommandEvent event) {
                EventHandlerHelper.fireForEvent(event, HandledEvent.COMMAND_EVENT);
            }
        };
    };


    public enum HandledEvent {
        FML_SERVER_STARTED_EVENT(FML_SERVER_STARTED_EVENT_CONSUMER, FMLServerStartedEvent.class, CronmcMod::forgeBus)
        , COMMAND_EVENT(COMMAND_EVENT_CONSUMER, CommandEvent.class, CronmcMod::forgeBus)
        ;

        private final Consumer<? extends Event> consumer;
        private final Supplier<IEventBus> busSupplier;

        public Consumer<? extends Event> getConsumer() { return this.consumer; }
        public IEventBus getBus() { return busSupplier.get(); }

        HandledEvent(Consumer<? extends Event> consumer, Class<? extends Event> eventClass, Supplier<IEventBus> busSupplier) {
            this.consumer = consumer;
            this.busSupplier = busSupplier;
        }

        public void register() {
            synchronized(eventConsumerSet) {
                HandledEvent handledEvent = this;
                if (eventConsumerSet.contains(handledEvent)) {
                    return;
                }
                handledEvent.getBus().addListener(handledEvent.getConsumer());
                eventConsumerSet.add(handledEvent);
            }
        }

        public void deregister() {
            synchronized (eventConsumerSet) {
                HandledEvent handledEvent = this;
                if (!eventConsumerSet.contains(handledEvent)) {
                    return;// nothing to do
                }
                eventConsumerSet.remove(handledEvent);
                handledEvent.getBus().unregister(handledEvent.getConsumer());
            }
        }
    }
}
