package me.verya.bedwars;

import me.verya.bedwars.game.BedwarsWaiting;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

public class Bedwars implements ModInitializer {
    public static final String ID = "bedwars";
    public static final GameRuleType BED_INTERACTION = GameRuleType.create();
    public static final GameRuleType SATURATED_REGENERATION = GameRuleType.create();

    @Override
    public void onInitialize() {
        var gameType = GameType.register(new Identifier(ID, "bedwars"),
                BedwarsConfig.CODEC, BedwarsWaiting::open);
    }
}
