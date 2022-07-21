package com.pyehouse.mcmod.scheduler.server.handler;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.pyehouse.mcmod.scheduler.api.Scheduler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommandHandler {

    public static final String CMD_command = "schedule";
    public static final String CMD_stop = "stop";
    public static final String CMD_start = "start";

    public static final String I18N_START_SUCCESS = "scheduler.start.success";
    public static final String I18N_STOP_SUCCESS = "scheduler.stop.success";

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        final CommandDispatcher<CommandSource> commandDispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSource> scheduleCommand =
                Commands.literal(CMD_command)
                        .requires((commandSource) -> commandSource.hasPermission(4))
                        .then(
                                Commands.literal(CMD_stop)
                                        .executes((command) -> stop(command))
                                        .then(
                                                Commands.literal(CMD_start)
                                                        .executes((command) -> start(command))
                                        )

                        );
        commandDispatcher.register(scheduleCommand);
    }

    static ITextComponent makeTC(String id, String... extra) {
        return new TranslationTextComponent(id, (Object[]) extra);
    }

    public static int start(CommandContext<CommandSource> commandContext) {
        Scheduler.start();

        commandContext.getSource().sendSuccess(makeTC(I18N_START_SUCCESS), false);

        return 1;
    }

    public static int stop(CommandContext<CommandSource> commandContext) {
        Scheduler.stop();

        commandContext.getSource().sendSuccess(makeTC(I18N_STOP_SUCCESS), false);

        return 1;
    }
}
