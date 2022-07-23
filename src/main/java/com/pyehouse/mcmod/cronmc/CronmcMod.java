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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CronmcMod.MODID)
public class CronmcMod {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "cronmc";

    public CronmcMod() {

        registerConfigs();

        final ServerEventRegistrar serverEventRegistrar = new ServerEventRegistrar(modBus(), forgeBus());
        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> serverEventRegistrar::registration);

    }

    // <.<
    //         >.>
    public static IEventBus modBus() {
        return FMLJavaModLoadingContext.get().getModEventBus();
    }

    public static IEventBus forgeBus() {
        return MinecraftForge.EVENT_BUS;
    }

    private static void registerConfigs() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
    }
}
