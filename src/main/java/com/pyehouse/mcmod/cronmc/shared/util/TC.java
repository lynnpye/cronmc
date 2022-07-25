package com.pyehouse.mcmod.cronmc.shared.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TC {

    public static ITextComponent makeTC(String id, String... extra) {
        return new TranslationTextComponent(id, (Object[]) extra);
    }

    public static ITextComponent simpleTC(String fmt, Object... parms) {
        return new StringTextComponent(String.format(fmt, parms));
    }
}
