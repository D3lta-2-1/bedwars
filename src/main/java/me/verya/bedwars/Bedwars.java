package me.verya.bedwars;

import me.verya.bedwars.mixin.BedwarsConfig;
import me.verya.bedwars.game.BedwarsWaiting;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameType;
public class Bedwars implements ModInitializer {
    public static final String ID = "bedwars";

    @Override
    public void onInitialize() {
        var gameType = GameType.register(new Identifier(ID, "bedwars"),
                BedwarsConfig.CODEC, BedwarsWaiting::open);
    }
}
