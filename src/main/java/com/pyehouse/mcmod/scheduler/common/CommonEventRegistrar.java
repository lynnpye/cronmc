package com.pyehouse.mcmod.scheduler.common;

import com.pyehouse.mcmod.scheduler.common.handler.ServerLifecycleEventHandler;
import com.pyehouse.mcmod.scheduler.common.util.ModEventRegistrar;
import net.minecraftforge.eventbus.api.IEventBus;

public class CommonEventRegistrar extends ModEventRegistrar {

    public CommonEventRegistrar(IEventBus modEventBus, IEventBus forgeEventBus) {
        super(modEventBus, forgeEventBus);
    }

    @Override
    public void registration() {
        forgeEventBus.register(ServerLifecycleEventHandler.class);
    }
}
