package me.verya.bedwars;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.verya.bedwars.game.component.ForgeConfig;
import me.verya.bedwars.game.map.BedwarsMapConfig;


public record BedwarsConfig(int teamSize, BedwarsMapConfig mapConfig, ForgeConfig forgeConfig, int highLimit, int downLimit) {
    public static final Codec<BedwarsConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.INT.optionalFieldOf("team_size", 1).forGetter(BedwarsConfig::teamSize),
                BedwarsMapConfig.CODEC.fieldOf("map").forGetter(BedwarsConfig::mapConfig),
                ForgeConfig.CODEC.optionalFieldOf("forge", new ForgeConfig(ForgeConfig.IRON_SPAWN_TIME, ForgeConfig.GOLD_SPAWN_TIME, ForgeConfig.EMERALD_SPAWN_TIME)).forGetter(BedwarsConfig::forgeConfig),
                Codec.INT.optionalFieldOf("high_limit", 0).forGetter(BedwarsConfig::highLimit),
                Codec.INT.fieldOf("down_limit").forGetter(BedwarsConfig::downLimit)
        ).apply(instance, BedwarsConfig::new);
    });
}

