package com.pyehouse.mcmod.cronmc.api.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeReference<T> {
    private final Type type;

    protected TypeReference() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof Class<?>) {
            throw new RuntimeException("Missing type parameter.");
        }
        this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return this.type;
    }

    @SuppressWarnings("unchecked")
    public Class<T> getTypeClass() throws ClassNotFoundException {
        String className = getType().getTypeName();

        return (Class<T>) Class.forName(className);
    }
}