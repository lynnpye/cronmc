package com.pyehouse.mcmod.scheduler.common.handler;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

public class ServerLifecycleEventHandler {

    @SubscribeEvent
    public static void serverStarted(FMLServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        server.getCommands().performCommand(server.createCommandSourceStack(), "say flarbleglarben");
    }
}
