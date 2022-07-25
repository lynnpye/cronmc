package com.pyehouse.mcmod.cronmc.api;

import com.pyehouse.mcmod.cronmc.api.registry.ScheduleTypeRegistry;
import com.pyehouse.mcmod.cronmc.api.registry.TaskTypeRegistry;
import com.pyehouse.mcmod.cronmc.api.schedule.EventHandlerHelper;
import com.pyehouse.mcmod.cronmc.api.util.CronmcHelper;
import com.pyehouse.mcmod.cronmc.shared.util.Config;
import com.pyehouse.mcmod.cronmc.shared.util.TC;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.Task;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import net.minecraftforge.fmlserverevents.FMLServerAboutToStartEvent;
import net.minecraftforge.fmlserverevents.FMLServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.TimeZone;
import java.util.function.Supplier;

public final class Cronmc {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Cronmc INSTANCE = new Cronmc();

    public static Cronmc get() {
        return INSTANCE;
    }

    private static Scheduler cron4j = new Scheduler();

    public static Scheduler cron() {
        if (cron4j == null) {
            createNewScheduler();
        }
        return cron4j;
    }

    private static void createNewScheduler() {
        cron4j = new Scheduler();
    }

    private Cronmc() {}

    private boolean shuttingDown = false;
    private boolean startOnServerStart = true;
    private boolean ready = false;
    private Supplier<String[]> scheduleStringsSupplier = () -> new String[0];

    public boolean isReady() {
        return ready && !shuttingDown;
    }

    public String[] getScheduleStrings() {
        return this.scheduleStringsSupplier.get();
    }

    // BELOW HERE - these functions are NOT safe when Scheduler is not ready

    public void performAllTaskTypeMatches(ScheduledTask scheduledTask) {
        failIfNotReady();

        if (scheduledTask == null) {
            LOGGER.warn("Tried to perform a null task");
            return;
        }

        TaskTypeRegistry.performTask(scheduledTask);
    }

    public void launch(Task task) {
        failIfNotReady();

        cron().launch(task);
    }

    // ABOVE HERE - these functions are NOT safe when Scheduler is not ready
    // BELOW HERE - these functions are safe when Scheduler is not ready

    public void schedule(String schedulePattern, Task task) {
        cron().schedule(schedulePattern, task);
    }

    public String[] getCronStrings() {
        return CronmcHelper.getCronStrings();
    }

    public void opSay(String msg, Object... args) {
        opSay(null, msg, args);
    }

    public void opSay(ServerPlayer serverPlayer, String msg, Object... args) {
        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                LOGGER.info(String.format("[Cronmc] say " + msg, args));
                if (serverPlayer != null) {
                    tellPlayer(serverPlayer, msg, args);
                }
                if (Config.SERVER.outputToConsole.get()) {
                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        if (player.hasPermissions(Config.SERVER.consoleMessageMinPermissionLevel.get())
                            || (serverPlayer == null || !serverPlayer.getUUID().equals(player.getUUID()))) {
                            tellPlayer(player, msg, args);
                        }
                    }
                }
            }
        });
    }

    public void opCommand(String msg, Object... args) {
        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                LOGGER.info(String.format("[Cronmc] Executing OP command: " + msg, args));
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                server.getCommands().performCommand(
                        server.createCommandSourceStack().withPermission(Config.SERVER.consoleMessageMinPermissionLevel.get()),
                        String.format(msg, args));
            }
        });
    }

    public void tellPlayer(ServerPlayer player, String msg, Object... args) {
        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                player.sendMessage(TC.simpleTC(msg, args), player.getUUID());
            }
        });
    }

    /**
     * Stops current jobs, resets the supplier to use the provided schedules if the
     * list is not null (even if it is zero length, i.e. no jobs), and resets the
     * cron timezone if it is not null.
     *
     * @param cronTimeZone
     * @param scheduleStrings
     */
    public void resetSchedule(boolean startOnServerStart, TimeZone cronTimeZone, final String[] scheduleStrings) {
        if (shuttingDown) return;

        this.startOnServerStart = startOnServerStart;

        boolean wasReady = ready;
        LOGGER.info(String.format("Resetting schedule for Cronmc and retaining ready status '%s'", wasReady));

        stop(true);

        // reset our Supplier
        final String[] strings = scheduleStrings == null ? new String[0] : scheduleStrings;
        this.scheduleStringsSupplier = () -> strings;

        if (cronTimeZone != null && !cron().getTimeZone().getID().equals(cronTimeZone.getID())) {
            setCronTimeZone(cronTimeZone);
        }

        if (wasReady) {
            get().start();
        }

        scheduleTasks(getScheduleStrings());
    }

    public void refresh() {
        if (shuttingDown) return;

        resetSchedule(startOnServerStart, cron().getTimeZone(), getScheduleStrings());
    }

    public void stop() {
        stop(false);
    }

    public void stop(boolean clear) {
        LOGGER.info("Stopping the Scheduler and setting ready to false");
        ready = false;
        EventHandlerHelper.stop(clear);

        // stop this one last
        safeStopCron4j();

        if (clear) {
            createNewScheduler();
        }
    }

    public void start() {
        if (shuttingDown) return;

        LOGGER.info("Starting the schedulers and setting ready to true");
        safeStartCron4j();

        EventHandlerHelper.start();

        ready = true;
    }

    public void safeStartCron4j() {
        // start this one first
        if (!cron().isStarted()) {
            try {
                cron().start();
            } catch (Throwable e) {
                LOGGER.throwing(e);
            }
        }
    }

    public void safeStopCron4j() {
        if (cron4j != null) {
            if (cron4j.isStarted()) {
                LOGGER.info("Attempting to stop cron handler and jobs");
                cron4j.stop();
                LOGGER.info("Cron handler and jobs stopped");
            }
        }
    }

    public void registration(final IEventBus modEventBus, final IEventBus forgeEventBus) {
        forgeEventBus.register(CronmcLifecycleHandler.class);

        ScheduleTypeRegistry.register(modEventBus);
        TaskTypeRegistry.register(modEventBus);
    }

    private void scheduleTasks(@Nonnull String[] scheduleStrings) {
        for (String scheduleString : scheduleStrings) {
            ScheduledTask scheduledTask = new ScheduledTask(scheduleString);
            if (scheduledTask.isValid()) {
                ScheduleTypeRegistry.scheduleTask(scheduledTask);
            }
        }
    }

    private void setCronTimeZone(TimeZone timeZone) {
        if (shuttingDown) return;

        if (timeZone == null) {
            LOGGER.error("Cannot assign null TimeZone to cron");
            return;
        }
        LOGGER.info(String.format("Current cron timezone is '%s'", cron().getTimeZone().getID()));

        cron().setTimeZone(timeZone);

        LOGGER.info(String.format("Updated cron timezone to '%s'", cron().getTimeZone().getID()));
    }

    private void failIfNotReady() {
        if (!isReady()) {
            throw new IllegalStateException("Unsafe function called on Scheduler while not ready or shutting down");
        }
    }

    private static class CronmcLifecycleHandler {

        @SubscribeEvent
        public static void serverAboutToStart(FMLServerAboutToStartEvent event) {
            get().shuttingDown = false;
            if (get().startOnServerStart) {
                get().start();
            }
        }

        @SubscribeEvent
        public static void serverStopping(FMLServerStoppingEvent event) {
            get().shuttingDown = true;
            get().stop();
        }
    }
}
