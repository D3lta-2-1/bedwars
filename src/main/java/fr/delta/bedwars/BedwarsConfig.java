package fr.delta.bedwars;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.StageEvent.GameEvent;
import fr.delta.bedwars.StageEvent.GameEventConfig;
import fr.delta.bedwars.game.teamComponent.Forge;
import fr.delta.bedwars.game.resourceGenerator.GeneratorBuilder;
import net.minecraft.util.Identifier;
import java.util.List;

public record BedwarsConfig(int teamSize, Identifier mapConfig, List<Forge.Tier> forgeConfig, List<GeneratorBuilder> generatorTypeList, List<GameEvent> events, Identifier shopCategoriesId, Identifier shopEntriesId, int highLimit, int downLimit, int voidHigh) {
    public static final Codec<BedwarsConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.INT.optionalFieldOf("team_size", 1).forGetter(BedwarsConfig::teamSize),
                Identifier.CODEC.fieldOf("map").forGetter(BedwarsConfig::mapConfig),
                Forge.CODEC.optionalFieldOf("forge", Forge.defaulted()).forGetter(BedwarsConfig::forgeConfig),
                GeneratorBuilder.CODEC.listOf().optionalFieldOf("generator_types", List.of()).forGetter(BedwarsConfig::generatorTypeList),
                GameEventConfig.CODEC.listOf().optionalFieldOf("game_events", List.of()).forGetter(BedwarsConfig::events),
                Identifier.CODEC.fieldOf("category_config").forGetter(BedwarsConfig::shopCategoriesId),
                Identifier.CODEC.fieldOf("entries_config").forGetter(BedwarsConfig::shopEntriesId),
                Codec.INT.optionalFieldOf("high_limit", 30).forGetter(BedwarsConfig::highLimit),
                Codec.INT.optionalFieldOf("down_limit" ,0).forGetter(BedwarsConfig::downLimit),
                Codec.INT.optionalFieldOf("void_high", 0).forGetter(BedwarsConfig::voidHigh)
        ).apply(instance, BedwarsConfig::new)
    );
}

