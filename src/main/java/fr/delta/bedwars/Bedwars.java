package fr.delta.bedwars;

import fr.delta.bedwars.game.BedwarsWaiting;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

public class Bedwars implements DedicatedServerModInitializer {
    public static final String ID = "bedwars";
    public static final GameRuleType BED_INTERACTION = GameRuleType.create();

    @Override
    public void onInitializeServer() {

        GameType.register(new Identifier(ID, "bedwars"), BedwarsConfig.CODEC, BedwarsWaiting::open);
    }
}
