package com.pyehouse.mcmod.scheduler.api.registry;

import com.pyehouse.mcmod.scheduler.SchedulerMod;
import com.pyehouse.mcmod.scheduler.api.ScheduleHandler;
import com.pyehouse.mcmod.scheduler.api.ScheduledTask;
import com.pyehouse.mcmod.scheduler.api.schedule.CronHandler;
import com.pyehouse.mcmod.scheduler.api.schedule.EventHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ScheduleTypeRegistry {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String REGISTER_ID = "scheduletype";

    private static final DeferredRegister<ScheduleHandler> SCHEDULE_HANDLERS =
            DeferredRegister.create(ScheduleHandler.class, SchedulerMod.MODID);

    public static final RegistryObject<ScheduleHandler> CRON_HANDLER = SCHEDULE_HANDLERS
            .register(CronHandler.HANDLER_ID, CronHandler::new);
    public static final RegistryObject<ScheduleHandler> EVENT_HANDLER = SCHEDULE_HANDLERS
            .register(EventHandler.HANDLER_ID, EventHandler::new);

    public static void register(final IEventBus modEventBus) {
        SCHEDULE_HANDLERS.makeRegistry(REGISTER_ID, () ->
                new RegistryBuilder<ScheduleHandler>()
                        .setName(new ResourceLocation(SchedulerMod.MODID, REGISTER_ID))
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
