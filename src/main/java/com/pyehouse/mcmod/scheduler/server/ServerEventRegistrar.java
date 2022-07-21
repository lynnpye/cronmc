package com.pyehouse.mcmod.scheduler.server;

import com.pyehouse.mcmod.scheduler.api.Scheduler;
import com.pyehouse.mcmod.scheduler.server.handler.ServerConfigHandler;
import com.pyehouse.mcmod.scheduler.server.handler.ServerLifecycleEventHandler;
import com.pyehouse.mcmod.scheduler.shared.util.ModEventRegistrar;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerEventRegistrar extends ModEventRegistrar {
    private static final Logger LOGGER = LogManager.getLogger();

    public ServerEventRegistrar(IEventBus modEventBus, IEventBus forgeEventBus) {
        super(modEventBus, forgeEventBus);
    }

    @Override
    public void registration() {
        modEventBus.register(ServerConfigHandler.class);

        forgeEventBus.register(ServerLifecycleEventHandler.class);

        Scheduler.registration(modEventBus, forgeEventBus);
    }
}
