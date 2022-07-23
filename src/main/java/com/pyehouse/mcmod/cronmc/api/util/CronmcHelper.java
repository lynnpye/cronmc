package com.pyehouse.mcmod.cronmc.api.util;

import com.pyehouse.mcmod.cronmc.api.Cronmc;
import com.pyehouse.mcmod.cronmc.api.schedule.ICronTask;
import it.sauronsoftware.cron4j.*;
import net.minecraft.util.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public final class CronmcHelper {
    public static final String TUPLE_DELIMITER = "|";
    public static final String TYPE_DELIMITER = ":";
    private static final Logger LOGGER = LogManager.getLogger();

    private CronmcHelper() {}

    public static TaskCollector getCron4jMemoryCollector(@Nonnull Scheduler scheduler) {
        TaskCollector memoryCollector = null;

        try {
            Field mcField = scheduler.getClass().getDeclaredField("memoryTaskCollector");
            mcField.setAccessible(true);
            memoryCollector = (TaskCollector) mcField.get(scheduler);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.throwing(e);
        }
        return memoryCollector;
    }

    public static boolean isCronTimeZoneValid(Object tzString) {
        for (String availableId : TimeZone.getAvailableIDs()) {
            if (availableId.equals(tzString)) {
                return true;
            }
        }
        return false;
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

    /**
     * This will attempt to iterate all of the tasks cron4j is aware of and output
     * as much information as it can.
     *
     * For activity that this mod codes for, that means an @ICronTask derivative,
     * but it is possible that, given the exposure I readily provide for the cron4j
     * interface, I recognize that there may be non-ICronTask objects in the queue.
     *
     * In those cases, I will do the best I can to present something sensible.
     *
     * Fun fact, I hope folks decide to use this as a library mod.
     *
     * @return A non-null String array containing the Cronmc entries, as it understands them.
     */
    public static String[] getCronStrings() {
        List<String> strings = new ArrayList<String>();

        strings.add("Scheduled:");
        List<String> memStrings = getSchedulesInMemory();
        if (memStrings.size() > 0) {
            strings.addAll(memStrings);
        } else {
            strings.add("-None-");
        }

        strings.add("Running:");
        List<String> runStrings = getSchedulesExecuting();
        if (runStrings.size() > 0) {
            strings.addAll(runStrings);
        } else {
            strings.add("-None-");
        }

        return Arrays.copyOf(strings.toArray(), strings.size(), String[].class);
    }

    public static List<String> getSchedulesInMemory() {
        TaskCollector memoryTaskCollector = getCron4jMemoryCollector(Cronmc.cron());

        List<String> list = new ArrayList<String>();

        if (memoryTaskCollector == null) {
            // bark loudly
            return list;
        }

        TaskTable taskTable = memoryTaskCollector.getTasks();

        for (int i = 0; i < taskTable.size(); i++) {
            Task task = taskTable.getTask(i);
            list.add(getCronStringForTask(task));
        }
        return list;
    }

    public static List<String> getSchedulesExecuting() {
        List<String> list = new ArrayList<String>();

        for (TaskExecutor taskExecutor : Cronmc.cron().getExecutingTasks()) {
            list.add(getCronStringForTask(taskExecutor.getTask()));
        }

        return list;
    }

    public static String getCronStringForTask(Task task) {
        if (task instanceof ICronTask) {
            ICronTask cronTask = (ICronTask) task;
            return "[CronTask] " + cronTask.getCronString();
        } else {
            StringBuilder builder = new StringBuilder("[Default/Task]");
            Class<? extends Task> taskClass = task.getClass();
            for (Method declaredMethod : taskClass.getDeclaredMethods()) {
                if (
                        declaredMethod.isAccessible()
                                && declaredMethod.getParameterTypes().length == 0
                                && !declaredMethod.getReturnType().equals(Void.TYPE)
                ) {
                    builder.append(" M" + declaredMethod.getName() + "() = ");
                    try {
                        builder.append(declaredMethod.invoke(task));
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        String s = String.format("Despite precautions, this exception occurred: %s", e.getMessage());
                        LOGGER.warn(s);
                        builder.append(s);
                    } catch (Throwable t) {
                        String s = String.format("A throwable occurred: %s", t.getMessage());
                        LOGGER.warn(s);
                        builder.append(s);
                    }
                }
            }
            for (Field declaredField : taskClass.getDeclaredFields()) {
                if (
                        declaredField.isAccessible()
                                && !declaredField.getType().equals(Void.TYPE)
                ) {
                    builder.append(" F" + declaredField.getName() + "() = ");
                    try {
                        builder.append(declaredField.get(task));
                    } catch (IllegalAccessException e) {
                        String s = String.format("Despite precautions, this exception occurred: %s", e.getMessage());
                        LOGGER.warn(s);
                        builder.append(s);
                        e.printStackTrace();
                    } catch (Throwable t) {
                        String s = String.format("A throwable occurred: %s", t.getMessage());
                        LOGGER.warn(s);
                        builder.append(s);
                    }
                }
            }
            return builder.toString();
        }
    }
}
