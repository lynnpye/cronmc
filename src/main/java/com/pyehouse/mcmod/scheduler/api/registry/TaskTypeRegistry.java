package com.pyehouse.mcmod.scheduler.api.registry;

import com.pyehouse.mcmod.scheduler.SchedulerMod;
import com.pyehouse.mcmod.scheduler.api.ScheduledTask;
import com.pyehouse.mcmod.scheduler.api.TaskHandler;
import com.pyehouse.mcmod.scheduler.api.task.OpHandler;
import com.pyehouse.mcmod.scheduler.api.task.RunnableHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;

public class TaskTypeRegistry {

    public static final String REGISTER_ID = "tasktype";

    private static final DeferredRegister<TaskHandler> TASK_HANDLERS =
            DeferredRegister.create(TaskHandler.class, SchedulerMod.MODID);

    public static final RegistryObject<TaskHandler> OP_HANDLER = TASK_HANDLERS
            .register(OpHandler.HANDLER_ID, OpHandler::new);
    public static final RegistryObject<TaskHandler> RUNNABLE_HANDLER = TASK_HANDLERS
            .register(RunnableHandler.HANDLER_ID, RunnableHandler::new);

    public static void register(IEventBus modEventBus) {
        TASK_HANDLERS.makeRegistry(REGISTER_ID, () ->
                new RegistryBuilder<TaskHandler>()
                        .setName(new ResourceLocation(SchedulerMod.MODID, REGISTER_ID))
                        .setType(TaskHandler.class)
        );
        TASK_HANDLERS.register(modEventBus);
    }

    public static void performTask(ScheduledTask scheduledTask) {
        for (RegistryObject<TaskHandler> taskHandlerRegistryObject : TASK_HANDLERS.getEntries()) {
            taskHandlerRegistryObject.ifPresent((taskHandler) -> {
                if (taskHandler.handlesTaskType(scheduledTask)) {
                    taskHandler.handleScheduledTask(scheduledTask);
                }
            });
        }
    }
}
