package fr.delta.bedwars;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.component.Forge;
import fr.delta.bedwars.game.map.BedwarsMapConfig;
import fr.delta.bedwars.game.resourceGenerator.GeneratorBuilder;
import net.minecraft.util.Identifier;

import java.util.List;


public record BedwarsConfig(int teamSize, BedwarsMapConfig mapConfig, Forge.ForgeConfig forgeConfig, List<GeneratorBuilder> generatorTypeList, Identifier shopCategoriesId, Identifier shopEntriesId, int highLimit, int downLimit, int voidHigh) {
    public static final Codec<BedwarsConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.INT.optionalFieldOf("team_size", 1).forGetter(BedwarsConfig::teamSize),
                BedwarsMapConfig.CODEC.fieldOf("map").forGetter(BedwarsConfig::mapConfig),
                Forge.ForgeConfig.CODEC.optionalFieldOf("forge", Forge.ForgeConfig.defaulted()).forGetter(BedwarsConfig::forgeConfig),
                GeneratorBuilder.CODEC.listOf().optionalFieldOf("generator_types", List.of()).forGetter(BedwarsConfig::generatorTypeList),
                Identifier.CODEC.fieldOf("category_config").forGetter(BedwarsConfig::shopCategoriesId),
                Identifier.CODEC.fieldOf("entries_config").forGetter(BedwarsConfig::shopEntriesId),
                Codec.INT.optionalFieldOf("high_limit", 30).forGetter(BedwarsConfig::highLimit),
                Codec.INT.optionalFieldOf("down_limit" ,0).forGetter(BedwarsConfig::downLimit),
                Codec.INT.optionalFieldOf("void_high", 0).forGetter(BedwarsConfig::voidHigh)
        ).apply(instance, BedwarsConfig::new)
    );
}

