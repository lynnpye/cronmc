package com.pyehouse.mcmod.cronmc.shared.util;

import net.minecraft.network.chat.Component;

public class TC {

    public static Component makeTC(String id, String... extra) {
        return Component.translatable(id, (Object[]) extra);
    }

    public static Component simpleTC(String fmt, Object... parms) {
        return Component.literal(String.format(fmt, parms));
    }
}
