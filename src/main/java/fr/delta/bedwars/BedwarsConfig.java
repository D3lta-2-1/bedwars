package fr.delta.bedwars;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.component.ForgeConfig;
import fr.delta.bedwars.game.map.BedwarsMapConfig;
import net.minecraft.util.Identifier;


public record BedwarsConfig(int teamSize, BedwarsMapConfig mapConfig, ForgeConfig forgeConfig, int highLimit, int downLimit, Identifier shopCategoriesId, Identifier shopEntriesId) {
    public static final Codec<BedwarsConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.INT.optionalFieldOf("team_size", 1).forGetter(BedwarsConfig::teamSize),
                BedwarsMapConfig.CODEC.fieldOf("map").forGetter(BedwarsConfig::mapConfig),
                ForgeConfig.CODEC.optionalFieldOf("forge", new ForgeConfig(ForgeConfig.IRON_SPAWN_TIME, ForgeConfig.GOLD_SPAWN_TIME, ForgeConfig.EMERALD_SPAWN_TIME)).forGetter(BedwarsConfig::forgeConfig),
                Codec.INT.optionalFieldOf("high_limit", 50).forGetter(BedwarsConfig::highLimit),
                Codec.INT.fieldOf("down_limit").forGetter(BedwarsConfig::downLimit),
                Identifier.CODEC.fieldOf("category_config").forGetter(BedwarsConfig::shopCategoriesId),
                Identifier.CODEC.fieldOf("entries_config").forGetter(BedwarsConfig::shopEntriesId)
        ).apply(instance, BedwarsConfig::new)
    );
}

