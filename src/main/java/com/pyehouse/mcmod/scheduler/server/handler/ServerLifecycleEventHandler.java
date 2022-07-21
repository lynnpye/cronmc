package com.pyehouse.mcmod.scheduler.server.handler;

import com.pyehouse.mcmod.scheduler.api.Scheduler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class ServerLifecycleEventHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void serverAboutToStart(FMLServerAboutToStartEvent event) {
        Scheduler.start();
        Object[] objectStrings = ServerConfigHandler.SERVER.schedules.get().toArray();
        Scheduler.scheduleTasks(Arrays.copyOf(objectStrings, objectStrings.length, String[].class));
    }
}
