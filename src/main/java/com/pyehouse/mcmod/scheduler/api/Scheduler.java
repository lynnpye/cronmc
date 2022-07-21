package com.pyehouse.mcmod.scheduler.api;

import com.pyehouse.mcmod.scheduler.api.registry.ScheduleTypeRegistry;
import com.pyehouse.mcmod.scheduler.api.registry.TaskTypeRegistry;
import com.pyehouse.mcmod.scheduler.api.schedule.CronHandler;
import com.pyehouse.mcmod.scheduler.api.schedule.EventHandlerHelper;
import net.minecraft.util.Tuple;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.TimeZone;

public final class Scheduler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String TUPLE_DELIMITER = "|";
    public static final String TYPE_DELIMITER = ":";

    private static boolean ready = false;

    private static Scheduler INSTANCE = null;

    private Scheduler() {

    }

    public static boolean isReady() { return ready; }
    public static void setReady(boolean ready) { Scheduler.ready = ready; }

    public static void failIfNotReady() {
        if (!isReady()) {
            throw new IllegalStateException("Unsafe function called on Scheduler while not ready");
        }
    }

    // BELOW HERE - these functions are NOT safe when Scheduler is not ready
    public static void schedule(ScheduledTask scheduledTask) {
        failIfNotReady();

        if (scheduledTask == null) {
            LOGGER.warn("Tried to schedule a null task");
            return;
        }

        ScheduleTypeRegistry.scheduleTask(scheduledTask);
    }

    public static void perform(ScheduledTask scheduledTask) {
        failIfNotReady();

        if (scheduledTask == null) {
            LOGGER.warn("Tried to perform a null task");
            return;
        }

        TaskTypeRegistry.performTask(scheduledTask);
    }

    public static void scheduleTasks(@Nonnull String[] scheduleStrings) {
        failIfNotReady();

        for (String scheduleString : scheduleStrings) {
            ScheduledTask scheduledTask = new ScheduledTask(scheduleString);
            if (scheduledTask.isValid()) {
                schedule(scheduledTask);
            }
        }
    }

    // ABOVE HERE - these functions are NOT safe when Scheduler is not ready
    // BELOW HERE - these functions are safe when Scheduler is not ready

    public static void stop() {
        LOGGER.info("Stopping the Scheduler and setting ready to false");
        setReady(false);
        CronHandler.stop();
        EventHandlerHelper.stop();
    }

    public static void start() {
        LOGGER.info("Starting the Scheduler and setting ready to true");
        CronHandler.start();
        EventHandlerHelper.start();
        setReady(true);
    }

    public static void registration(IEventBus modEventBus, IEventBus forgeEventBus) {
        ScheduleTypeRegistry.register(modEventBus);
        TaskTypeRegistry.register(modEventBus);
        forgeEventBus.register(EventHandlerHelper.class);
        EventHandlerHelper.register(modEventBus, forgeEventBus);
    }

    public static void setCronTimeZone(TimeZone timeZone) {
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
}
