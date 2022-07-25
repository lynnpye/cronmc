package com.pyehouse.mcmod.cronmc.api.registry;

import com.pyehouse.mcmod.cronmc.CronmcMod;
import com.pyehouse.mcmod.cronmc.api.ScheduledTask;
import com.pyehouse.mcmod.cronmc.api.TaskHandler;
import com.pyehouse.mcmod.cronmc.api.task.OpHandler;
import com.pyehouse.mcmod.cronmc.api.task.RunnableHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class TaskTypeRegistry {

    public static final String REGISTER_ID = "tasktype";
    public static final ResourceLocation REGISTER_URL =
            new ResourceLocation(CronmcMod.MODID, REGISTER_ID);

    private static final DeferredRegister<TaskHandler> TASK_HANDLERS =
            DeferredRegister.create(REGISTER_URL, CronmcMod.MODID);

    public static final RegistryObject<TaskHandler> OP_HANDLER = TASK_HANDLERS
            .register(OpHandler.HANDLER_ID, OpHandler::new);
    public static final RegistryObject<TaskHandler> RUNNABLE_HANDLER = TASK_HANDLERS
            .register(RunnableHandler.HANDLER_ID, RunnableHandler::new);

    public static void register(IEventBus modEventBus) {
        TASK_HANDLERS.makeRegistry(RegistryBuilder::new);
        TASK_HANDLERS.register(modEventBus);
    }

    public static void performTask(ScheduledTask scheduledTask) {
        for (RegistryObject<TaskHandler> taskHandlerRegistryObject : TASK_HANDLERS.getEntries()) {
            TaskHandler taskHandler = taskHandlerRegistryObject.get();
            if (taskHandler.handlesTaskType(scheduledTask)) {
                taskHandler.handleScheduledTask(scheduledTask);
            }
        }
    }
}
