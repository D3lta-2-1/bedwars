package me.verya.bedwars;

import me.verya.bedwars.customItem.OldSword;
import me.verya.bedwars.game.BedwarsWaiting;
import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

public class Bedwars implements ModInitializer {
    public static final String ID = "bedwars";
    public static final GameRuleType BED_INTERACTION = GameRuleType.create();
    public static final GameRuleType ATTACK_SOUND = GameRuleType.create();
    public static final GameRuleType OLD_KNOCKBACK = GameRuleType.create();

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, new Identifier(ID, "wooden_sword"), OldSword.WOODEN_SWORD);
        Registry.register(Registries.ITEM, new Identifier(ID, "golden_sword"), OldSword.GOLDEN_SWORD);
        Registry.register(Registries.ITEM, new Identifier(ID, "stone_sword"), OldSword.STONE_SWORD);
        Registry.register(Registries.ITEM, new Identifier(ID, "iron_sword"), OldSword.IRON_SWORD);
        Registry.register(Registries.ITEM, new Identifier(ID, "diamond_sword"), OldSword.DIAMOND_SWORD);
        Registry.register(Registries.ITEM, new Identifier(ID, "netherite_sword"), OldSword.NETHERITE_SWORD);

        var gameType = GameType.register(new Identifier(ID, "bedwars"),
                BedwarsConfig.CODEC, BedwarsWaiting::open);
    }
}
