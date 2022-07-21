package com.pyehouse.mcmod.cronmc.api;

import com.pyehouse.mcmod.cronmc.api.registry.ScheduleTypeRegistry;
import com.pyehouse.mcmod.cronmc.api.registry.TaskTypeRegistry;
import com.pyehouse.mcmod.cronmc.api.schedule.CronHandler;
import com.pyehouse.mcmod.cronmc.api.schedule.EventHandlerHelper;
import net.minecraft.util.Tuple;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.function.Supplier;

public final class Cronmc {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String TUPLE_DELIMITER = "|";
    public static final String TYPE_DELIMITER = ":";

    private static Cronmc INSTANCE = new Cronmc();

    public static Cronmc get() {
        return INSTANCE;
    }

    private Cronmc() {

    }

    private boolean startOnServerStart = true;
    private boolean ready = false;
    private Supplier<String[]> scheduleStringsSupplier = () -> new String[0];

    public boolean isReady() { return ready; }
    public void setReady(boolean ready) { this.ready = ready; }

    public boolean isStartOnServerStart() { return this.startOnServerStart; }

    public void failIfNotReady() {
        if (!isReady()) {
            throw new IllegalStateException("Unsafe function called on Scheduler while not ready");
        }
    }

    public void setScheduleStringsSupplier(Supplier<String[]> scheduleStringsSupplier) {
        this.scheduleStringsSupplier = scheduleStringsSupplier;
    }

    public String[] getScheduleStrings() {
        return this.scheduleStringsSupplier.get();
    }

    // BELOW HERE - these functions are NOT safe when Scheduler is not ready
    public void schedule(ScheduledTask scheduledTask) {
        failIfNotReady();

        if (scheduledTask == null) {
            LOGGER.warn("Tried to schedule a null task");
            return;
        }

        ScheduleTypeRegistry.scheduleTask(scheduledTask);
    }

    public void perform(ScheduledTask scheduledTask) {
        failIfNotReady();

        if (scheduledTask == null) {
            LOGGER.warn("Tried to perform a null task");
            return;
        }

        TaskTypeRegistry.performTask(scheduledTask);
    }

    // ABOVE HERE - these functions are NOT safe when Scheduler is not ready
    // BELOW HERE - these functions are safe when Scheduler is not ready

    /**
     * Stops current jobs, resets the supplier to use the provided schedules if the
     * list is not null (even if it is zero length, i.e. no jobs), and resets the
     * cron timezone if it is not null.
     *
     * @param cronTimeZone
     * @param scheduleStrings
     */
    public void resetSchedule(boolean startOnServerStart, TimeZone cronTimeZone, final String[] scheduleStrings) {

        boolean wasReady = isReady();
        LOGGER.info(String.format("Resetting schedule for Cronmc and retaining ready status '%s'", wasReady));

        stop();

        // reset our Supplier
        final String[] strings = scheduleStrings == null ? new String[0] : scheduleStrings;
        Cronmc.get().setScheduleStringsSupplier(() -> strings);

        if (cronTimeZone != null) {
            setCronTimeZone(cronTimeZone);
        }

        if (wasReady) {
            start();
        }
    }

    public void scheduleTasks(@Nonnull String[] scheduleStrings) {
        for (String scheduleString : scheduleStrings) {
            ScheduledTask scheduledTask = new ScheduledTask(scheduleString);
            if (scheduledTask.isValid()) {
                schedule(scheduledTask);
            }
        }
    }

    public void stop() {
        LOGGER.info("Stopping the Scheduler and setting ready to false");
        setReady(false);
        CronHandler.stop();
        EventHandlerHelper.stop();
    }

    public void start() {
        LOGGER.info("Starting the Scheduler and setting ready to true");
        CronHandler.start();
        EventHandlerHelper.start();
        setReady(true);
    }

    public void refresh() {
        boolean wasReady = INSTANCE.isReady();
        LOGGER.info(String.format("Resetting Cronmc and retaining ready status '%s'", wasReady));

        resetSchedule(isStartOnServerStart(), CronHandler.cron().getTimeZone(), getScheduleStrings());
    }

    public void registration(final IEventBus modEventBus, final IEventBus forgeEventBus) {
        forgeEventBus.register(CronmcLifecycleHandler.class);

        ScheduleTypeRegistry.register(modEventBus);
        TaskTypeRegistry.register(modEventBus);
        EventHandlerHelper.register(modEventBus, forgeEventBus);
    }

    public boolean isCronTimeZoneValid(String tzString) {
        return CronHandler.isTimeZoneValid(tzString);
    }

    public void setCronTimeZone(TimeZone timeZone) {
        CronHandler.setCronTimeZone(timeZone);
    }

    public static Tuple<Tuple<String, String>, Tuple<String, String>> splitScheduledTask(String scheduledTask) {
        if (scheduledTask == null) {
            LOGGER.warn("Cannot process null scheduledTask");
            return null;
        }

        String trimmed = scheduledTask.trim();
        int delimiterIndex = trimmed.indexOf(TUPLE_DELIMITER);
        if (delimiterIndex == -1) {
            LOGGER.warn(String.format("Missing tuple delimiter '%s' for scheduledTask '%s'", TUPLE_DELIMITER, scheduledTask));
            return null;
        }

        if (delimiterIndex == 0) {
            LOGGER.warn(String.format("No schedule (tuple delimiter at index 0) for scheduledTask '%s'", scheduledTask));
            return null;
        }

        if (delimiterIndex + 1 >= trimmed.length()) {
            LOGGER.warn(String.format("No task (tuple delimiter at index %d of %d) for scheduledTask '%s'", delimiterIndex, trimmed.length() - 1, scheduledTask));
            return null;
        }

        String newSchedule = trimmed.substring(0, delimiterIndex);
        String newTask = trimmed.substring(delimiterIndex + 1);

        String[] newScheduleBits = newSchedule.split(TYPE_DELIMITER, 2);
        String[] newTaskBits = newTask.split(TYPE_DELIMITER, 2);

        if (newScheduleBits.length != 2) {
            LOGGER.warn(String.format("Malformed schedule (missing type delimiter '%s') for scheduledTask '%s'", TYPE_DELIMITER, scheduledTask));
            return null;
        }

        if (newTaskBits.length != 2) {
            LOGGER.warn(String.format("Malformed task (missing type delimiter '%s') for scheduledTask '%s'", TYPE_DELIMITER, scheduledTask));
            return null;
        }

        for(int i = 0; i < 2; i++) {
            newScheduleBits[i] = newScheduleBits[i].trim();
            newTaskBits[i] = newTaskBits[i].trim();
        }

        if (newScheduleBits[0].isEmpty()) {
            LOGGER.warn(String.format("Malformed schedule (missing type) for scheduledTask '%s'", scheduledTask));
            return null;
        }

        if (newScheduleBits[1].isEmpty()) {
            LOGGER.warn(String.format("Malformed schedule (missing data) for scheduledTask '%s'", scheduledTask));
            return null;
        }

        if (newTaskBits[0].isEmpty()) {
            LOGGER.warn(String.format("Malformed task (missing type) for scheduledTask '%s'", scheduledTask));
            return null;
        }

        if (newTaskBits[1].isEmpty()) {
            LOGGER.warn(String.format("Malformed task (missing data) for scheduledTask '%s'", scheduledTask));
            return null;
        }

        return new Tuple<>(new Tuple<>(newScheduleBits[0], newScheduleBits[1]), new Tuple<>(newTaskBits[0], newTaskBits[1]));
    }

    public static boolean isValidSchedule(Object scheduledTaskObj) {
        if (scheduledTaskObj == null) return false;
        return splitScheduledTask(scheduledTaskObj.toString()) != null;
    }

    public static class CronmcLifecycleHandler {

        @SubscribeEvent
        public static void serverAboutToStart(FMLServerAboutToStartEvent event) {
            if (Cronmc.get().isStartOnServerStart()) {
                Cronmc.get().start();
            }
        }

        @SubscribeEvent
        public static void serverStopping(FMLServerStoppingEvent event) {
            Cronmc.get().stop();
        }
    }
}
