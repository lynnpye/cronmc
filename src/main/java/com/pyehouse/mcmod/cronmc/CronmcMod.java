package com.pyehouse.mcmod.cronmc;

import com.pyehouse.mcmod.cronmc.server.ServerEventRegistrar;
import com.pyehouse.mcmod.cronmc.shared.util.Config;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CronmcMod.MODID)
public class CronmcMod {
    public static final String MODID = "cronmc";

    public CronmcMod() {
        registerConfigs();

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        final ServerEventRegistrar serverEventRegistrar = new ServerEventRegistrar(modEventBus, forgeEventBus);
        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> serverEventRegistrar::registration);

    }

    private static void registerConfigs() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
    }
}
