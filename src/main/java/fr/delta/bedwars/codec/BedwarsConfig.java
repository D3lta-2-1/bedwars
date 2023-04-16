package fr.delta.bedwars.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import java.util.List;

public record BedwarsConfig(int teamSize, Identifier mapId, Identifier forgeConfigId, List<Identifier> generatorTypeIdList, List<Identifier> events, Identifier shopCategoriesId, Identifier shopEntriesId, int highLimit, int downLimit, int voidHigh, long timeOfDay)
{
    public static final Codec<BedwarsConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.INT.optionalFieldOf("team_size", 1).forGetter(BedwarsConfig::teamSize),
                Identifier.CODEC.fieldOf("map").forGetter(BedwarsConfig::mapId),
                Identifier.CODEC.fieldOf("forge").forGetter(BedwarsConfig::forgeConfigId),
                Identifier.CODEC.listOf().optionalFieldOf("generator_types", List.of()).forGetter(BedwarsConfig::generatorTypeIdList),
                Identifier.CODEC.listOf().optionalFieldOf("game_events", List.of()).forGetter(BedwarsConfig::events),
                Identifier.CODEC.fieldOf("category_config").forGetter(BedwarsConfig::shopCategoriesId),
                Identifier.CODEC.fieldOf("entries_config").forGetter(BedwarsConfig::shopEntriesId),
                Codec.INT.optionalFieldOf("high_limit", 30).forGetter(BedwarsConfig::highLimit),
                Codec.INT.optionalFieldOf("down_limit" ,0).forGetter(BedwarsConfig::downLimit),
                Codec.INT.optionalFieldOf("void_high", 0).forGetter(BedwarsConfig::voidHigh),
                Codec.LONG.optionalFieldOf("time_of_day", 6000L).forGetter(BedwarsConfig::timeOfDay)
        ).apply(instance, BedwarsConfig::new)
    );
}

