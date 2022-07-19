package com.pyehouse.mcmod.scheduler;

import com.pyehouse.mcmod.scheduler.common.CommonEventRegistrar;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SchedulerMod.MODID)
public class SchedulerMod {
    public static final String MODID = "scheduler";

    public SchedulerMod() {
        registerConfigs();

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        final CommonEventRegistrar commonEventRegistrar = new CommonEventRegistrar(modEventBus, forgeEventBus);
        commonEventRegistrar.registration();

    }

    private static void registerConfigs() {

    }
}
