package com.pyehouse.mcmod.cronmc.api.registry;

import com.pyehouse.mcmod.cronmc.CronmcMod;
import com.pyehouse.mcmod.cronmc.api.ScheduleHandler;
import com.pyehouse.mcmod.cronmc.api.ScheduledTask;
import com.pyehouse.mcmod.cronmc.api.schedule.CronHandler;
import com.pyehouse.mcmod.cronmc.api.schedule.EventHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ScheduleTypeRegistry {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String REGISTER_ID = "scheduletype";
    public static final ResourceLocation REGISTER_URL =
            new ResourceLocation(CronmcMod.MODID, REGISTER_ID);

    private static final DeferredRegister<ScheduleHandler> SCHEDULE_HANDLERS =
            DeferredRegister.create(REGISTER_URL, CronmcMod.MODID);

    public static final RegistryObject<ScheduleHandler> CRON_HANDLER = SCHEDULE_HANDLERS
            .register(CronHandler.HANDLER_ID, CronHandler::new);
    public static final RegistryObject<ScheduleHandler> EVENT_HANDLER = SCHEDULE_HANDLERS
            .register(EventHandler.HANDLER_ID, EventHandler::new);

    public static void register(final IEventBus modEventBus) {
        SCHEDULE_HANDLERS.makeRegistry(ScheduleHandler.class, () ->
                new RegistryBuilder<ScheduleHandler>()
                        .setName(new ResourceLocation(CronmcMod.MODID, REGISTER_ID))
                        .setType(ScheduleHandler.class)
        );
        SCHEDULE_HANDLERS.register(modEventBus);
    }

    public static void scheduleTask(ScheduledTask scheduledTask) {
        for (RegistryObject<ScheduleHandler> scheduleHandlerRegistryObject : SCHEDULE_HANDLERS.getEntries()) {
            scheduleHandlerRegistryObject.ifPresent((scheduleHandler -> {
                if (scheduleHandler.handlesScheduleType(scheduledTask)) {
                    scheduleHandler.handleScheduledTask(scheduledTask);
                }
            }));
        }
    }
}
