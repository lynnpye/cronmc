package com.pyehouse.mcmod.cronmc.api.schedule;

import com.pyehouse.mcmod.cronmc.CronmcMod;
import com.pyehouse.mcmod.cronmc.api.ScheduledTask;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.DifficultyChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.village.VillageSiegeEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
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
    public static final Consumer<CommandEvent> COMMAND_EVENT_CONSUMER =
            new Consumer<CommandEvent>() {
                @SubscribeEvent
                @Override
                public void accept(CommandEvent event) {
                    EventHandlerHelper.fireForEvent(event, HandledEvent.COMMAND_EVENT);
                }
            };
    public static final Consumer<PlayerEvent.PlayerChangeGameModeEvent> PLAYER_CHANGE_GAME_MODE_EVENT_CONSUMER =
            new Consumer<PlayerEvent.PlayerChangeGameModeEvent>() {
                @SubscribeEvent
                @Override
                public void accept(PlayerEvent.PlayerChangeGameModeEvent event) {
                    EventHandlerHelper.fireForEvent(event, HandledEvent.PLAYER_CHANGE_GAME_MODE_EVENT);
                }
            };
    public static final Consumer<WorldEvent.Load> WORLD_LOAD_EVENT_CONSUMER =
            new Consumer<WorldEvent.Load>() {
                @SubscribeEvent
                @Override
                public void accept(WorldEvent.Load event) {
                    EventHandlerHelper.fireForEvent(event, HandledEvent.WORLD_LOAD_EVENT);
                }
            };
    public static final Consumer<VillageSiegeEvent> VILLAGE_SIEGE_EVENT_CONSUMER =
            new Consumer<VillageSiegeEvent>() {
                @SubscribeEvent
                @Override
                public void accept(VillageSiegeEvent event) {
                    EventHandlerHelper.fireForEvent(event, HandledEvent.VILLAGE_SIEGE_EVENT);
                }
            };
    public static final Consumer<DifficultyChangeEvent> DIFFICULTY_CHANGE_EVENT_CONSUMER =
            new Consumer<DifficultyChangeEvent>() {
                @SubscribeEvent
                @Override
                public void accept(DifficultyChangeEvent event) {
                    EventHandlerHelper.fireForEvent(event, HandledEvent.DIFFICULTY_CHANGE_EVENT);
                }
            };
    public static final Consumer<ExplosionEvent> EXPLOSION_EVENT_CONSUMER =
            new Consumer<ExplosionEvent>() {
                @SubscribeEvent
                @Override
                public void accept(ExplosionEvent event) {
                    EventHandlerHelper.fireForEvent(event, HandledEvent.EXPLOSION_EVENT);
                }
            };
    public static final Consumer<PlayerEvent.PlayerLoggedInEvent> PLAYER_LOGGED_IN_EVENT_CONSUMER =
            new Consumer<PlayerEvent.PlayerLoggedInEvent>() {
                @SubscribeEvent
                @Override
                public void accept(PlayerEvent.PlayerLoggedInEvent event) {
                    EventHandlerHelper.fireForEvent(event, HandledEvent.PLAYER_LOGGED_IN_EVENT);
                }
            };


    public enum HandledEvent {
        FML_SERVER_STARTED_EVENT(FML_SERVER_STARTED_EVENT_CONSUMER, FMLServerStartedEvent.class, CronmcMod::forgeBus)
        , COMMAND_EVENT(COMMAND_EVENT_CONSUMER, CommandEvent.class, CronmcMod::forgeBus)
        , PLAYER_CHANGE_GAME_MODE_EVENT(PLAYER_CHANGE_GAME_MODE_EVENT_CONSUMER, PlayerEvent.PlayerChangeGameModeEvent.class, CronmcMod::forgeBus)
        , WORLD_LOAD_EVENT(WORLD_LOAD_EVENT_CONSUMER, WorldEvent.Load.class, CronmcMod::forgeBus)
        , VILLAGE_SIEGE_EVENT(VILLAGE_SIEGE_EVENT_CONSUMER, VillageSiegeEvent.class, CronmcMod::forgeBus)
        , DIFFICULTY_CHANGE_EVENT(DIFFICULTY_CHANGE_EVENT_CONSUMER, DifficultyChangeEvent.class, CronmcMod::forgeBus)
        , EXPLOSION_EVENT(EXPLOSION_EVENT_CONSUMER, ExplosionEvent.class, CronmcMod::forgeBus)
        , PLAYER_LOGGED_IN_EVENT(PLAYER_LOGGED_IN_EVENT_CONSUMER, PlayerEvent.PlayerLoggedInEvent.class, CronmcMod::forgeBus)
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
