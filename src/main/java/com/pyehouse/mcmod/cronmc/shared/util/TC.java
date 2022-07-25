package com.pyehouse.mcmod.cronmc.shared.util;

import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class TC {

    public static BaseComponent makeTC(String id, String... extra) {
        return new TranslatableComponent(id, (Object[]) extra);
    }

    public static BaseComponent simpleTC(String fmt, Object... parms) {
        return new TextComponent(String.format(fmt, parms));
    }
}
