package com.pyehouse.mcmod.cronmc.api.schedule;

import static com.pyehouse.mcmod.cronmc.api.schedule.EventHandlerHelper.*;

import com.pyehouse.mcmod.cronmc.api.util.TypeReference;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

import java.util.function.Supplier;

public enum HandledEvent {
    serverStarted(FMLServerStartedEvent.class, FORGE, () -> new TypeReference<FMLServerStartedEvent>(){});

    private Class<?> eventClass;
    private String bus;
    private Supplier<TypeReference<? extends Event>> supplier;

    HandledEvent(Class<?> clazz, String bus, Supplier<TypeReference<? extends Event>> supplier) {
        this.eventClass = clazz;
        this.bus = bus;
        this.supplier = supplier;
    }

    public boolean handlesEvent(Class<?> otherClass) {
        return eventClass.equals(otherClass);
    }

    public String getBus() {
        return bus;
    }

    public TypeReference<? extends Event> getTypeReference() {
        return this.supplier.get();
    }
}
