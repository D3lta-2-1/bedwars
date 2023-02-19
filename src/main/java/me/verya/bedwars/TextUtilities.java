package me.verya.bedwars;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextUtilities {
    public static final MutableText CHECKMARK = Text.literal("✔").setStyle(Style.EMPTY.withColor(Formatting.GREEN));
    public static final MutableText X = Text.literal("✘").setStyle(Style.EMPTY.withColor(Formatting.RED));
    public static final MutableText SPACE = Text.literal(" ");
    public static final MutableText DOTS = Text.literal(":");
    public static final MutableText GENERAL_PREFIX = Text.literal(">");
    public static Style WARNING = Style.EMPTY.withColor(Formatting.RED);

    static public MutableText concatenate(MutableText... texts)
    {
        var result = Text.empty();
        for(var text : texts)
        {
            result.append(text);
        }
        return result;
    }
    static public MutableText getTranslation(String path, String namespace)
    {
        return Text.translatable(path + '.' + Bedwars.ID + '.' + namespace);
    }
}
