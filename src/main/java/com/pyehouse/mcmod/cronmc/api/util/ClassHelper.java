package com.pyehouse.mcmod.cronmc.api.util;

public final class ClassHelper {
    private ClassHelper() {}

    public static String getUsefulEventName(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Cannot derive useful class name from null class");
        }

        String name = clazz.getName();

        if (!name.contains("$")) {
            return clazz.getSimpleName();
        }

        String supername = clazz.getSuperclass().getSimpleName();

        return String.format("%s.%s", supername, name);
    }
}
