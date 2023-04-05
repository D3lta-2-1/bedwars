package fr.delta.bedwars.game.teamComponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ForgeConfig (int ironSpawnTime, int goldSpawnTime, int emeraldSpawnTime)
{
    //default Value
    public static final int IRON_SPAWN_TIME = 40;
    public static final int GOLD_SPAWN_TIME = 15 * 20;
    public static final int EMERALD_SPAWN_TIME = 60 * 20;
    public static final Codec<ForgeConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("iron_spawn_time", IRON_SPAWN_TIME).forGetter(ForgeConfig::ironSpawnTime),
            Codec.INT.optionalFieldOf("gold_spawn_time", GOLD_SPAWN_TIME).forGetter(ForgeConfig::goldSpawnTime),
            Codec.INT.optionalFieldOf("iron_spawn_time", EMERALD_SPAWN_TIME).forGetter(ForgeConfig::emeraldSpawnTime)

    ).apply(instance, ForgeConfig::new));
}
