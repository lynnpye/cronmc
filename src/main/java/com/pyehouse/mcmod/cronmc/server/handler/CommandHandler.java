package com.pyehouse.mcmod.cronmc.server.handler;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pyehouse.mcmod.cronmc.api.Cronmc;
import com.pyehouse.mcmod.cronmc.api.util.CronmcHelper;
import com.pyehouse.mcmod.cronmc.shared.util.Config;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.ExceptionUtils;

import java.util.TimeZone;

public class CommandHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String CMD_command = "cronmc";
    public static final String CMD_stop = "stop";
    public static final String CMD_start = "start";
    public static final String CMD_refresh = "refresh";
    public static final String CMD_settz = "settz";
    public static final String CMD_list = "list";

    public static final String ARG_settz = "arg_settz";

    public static final String I18N_START_SUCCESS = "cronmc.start.success";
    public static final String I18N_STOP_SUCCESS = "cronmc.stop.success";
    public static final String I18N_REFRESH_SUCCESS = "cronmc.refresh.success";
    public static final String I18N_SETTIMEZONE_SUCCESS = "cronmc.settz.success";
    public static final String I18N_SETTIMEZONE_FAILURE = "cronmc.settz.failure";

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        final CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> scheduleCommand =
                Commands.literal(CMD_command)
                        .requires((commandSource) -> commandSource.hasPermission(4))
                        .then(
                                Commands.literal(CMD_stop)
                                        .executes((command) -> stop(command))

                        )
                        .then(
                                Commands.literal(CMD_start)
                                        .executes((command) -> start(command))
                        )
                        .then(
                                Commands.literal(CMD_refresh)
                                        .executes((command) -> refresh(command))
                        )
                        .then(
                                Commands.literal(CMD_settz)
                                        .then(
                                                RequiredArgumentBuilder.<CommandSourceStack, String>argument(ARG_settz, StringArgumentType.greedyString())
                                                        .executes((command) -> settz(command))
                                        )
                        )
                        .then(
                                Commands.literal(CMD_list)
                                        .executes((command) -> list(command))
                        )
                ;
        commandDispatcher.register(scheduleCommand);
    }

    static TranslatableComponent makeTC(String id, String... extra) {
        return new TranslatableComponent(id, (Object[]) extra);
    }

    public static int start(CommandContext<CommandSourceStack> commandContext) {
        Cronmc.get().start();

        commandContext.getSource().sendSuccess(makeTC(I18N_START_SUCCESS), true);

        return 1;
    }

    public static int stop(CommandContext<CommandSourceStack> commandContext) {
        Cronmc.get().stop();

        commandContext.getSource().sendSuccess(makeTC(I18N_STOP_SUCCESS), true);

        return 1;
    }

    public static int refresh(CommandContext<CommandSourceStack> commandContext) {
        Cronmc.get().refresh();

        commandContext.getSource().sendSuccess(makeTC(I18N_REFRESH_SUCCESS), true);

        return 1;
    }

    public static int settz(CommandContext<CommandSourceStack> commandContext) {
        String tzString = commandContext.getArgument(ARG_settz, String.class);

        if (!CronmcHelper.isCronTimeZoneValid(tzString)) {
            commandContext.getSource().sendFailure(makeTC(I18N_SETTIMEZONE_FAILURE));
        } else {
            TimeZone cronTimeZone = TimeZone.getTimeZone(tzString);
            Config.setCronTimeZone(cronTimeZone);

            commandContext.getSource().sendSuccess(makeTC(I18N_SETTIMEZONE_SUCCESS), true);
        }

        return 1;
    }

    public static int list(CommandContext<CommandSourceStack> commandContext) {
        String[] cronStrings = Cronmc.get().getCronStrings();

        ServerPlayer player = null;
        try {
            player = commandContext.getSource().getPlayerOrException();

            if (cronStrings.length < 1) {
                Cronmc.get().opSay(player, "No Cronmc tasks are queued");
            }

            for (String cronString : cronStrings) {
                Cronmc.get().opSay(player, cronString);
            }
        } catch (CommandSyntaxException e) {
            LOGGER.error(String.format("Error trying to run cronmc list: %s", ExceptionUtils.getStackTrace(e)));
        }

        return 1;
    }
}
