package com.pyehouse.mcmod.cronmc.server;

import com.pyehouse.mcmod.cronmc.api.Cronmc;
import com.pyehouse.mcmod.cronmc.server.handler.CommandHandler;
import com.pyehouse.mcmod.cronmc.shared.util.Config;
import com.pyehouse.mcmod.cronmc.shared.util.ModEventRegistrar;
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
        modEventBus.addListener(Config::onConfigUpdate);

        forgeEventBus.register(CommandHandler.class);
        forgeEventBus.addListener(Config::onServerAboutToStart);

        Cronmc.get().registration(modEventBus, forgeEventBus);
    }
}
