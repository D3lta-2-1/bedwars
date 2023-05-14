package fr.delta.bedwars;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;

public class TextUtilities {
    public static final MutableText CHECKMARK = Text.literal("✔").setStyle(Style.EMPTY.withColor(Formatting.GREEN));
    public static final MutableText X = Text.literal("✘").setStyle(Style.EMPTY.withColor(Formatting.RED));
    public static final MutableText SPACE = Text.literal(" ");
    public static final MutableText DOTS = Text.literal(":");
    public static Style WARNING = Style.EMPTY.withColor(Formatting.RED);

    public static MutableText getFormattedPlayerName(Entity entity, TeamManager manager)
    {
        var entityName = entity.getName().copy();
        if(entity instanceof ServerPlayerEntity player)
        {
            var team = manager.teamFor(player);
            if(team == null) return entityName;
            return entityName.formatted(manager.getTeamConfig(team).chatFormatting());
        }
        return entityName;
    }

    static public MutableText concatenate(Text... texts)
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
