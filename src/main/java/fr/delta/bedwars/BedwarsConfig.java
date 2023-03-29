package fr.delta.bedwars;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.component.Forge;
import fr.delta.bedwars.game.map.BedwarsMapConfig;
import net.minecraft.util.Identifier;


public record BedwarsConfig(int teamSize, BedwarsMapConfig mapConfig, Forge.ForgeConfig forgeConfig, int highLimit, int downLimit, int voidHigh, Identifier shopCategoriesId, Identifier shopEntriesId) {
    public static final Codec<BedwarsConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.INT.optionalFieldOf("team_size", 1).forGetter(BedwarsConfig::teamSize),
                BedwarsMapConfig.CODEC.fieldOf("map").forGetter(BedwarsConfig::mapConfig),
                Forge.ForgeConfig.CODEC.optionalFieldOf("forge", Forge.ForgeConfig.defaulted()).forGetter(BedwarsConfig::forgeConfig),
                Codec.INT.optionalFieldOf("high_limit", 30).forGetter(BedwarsConfig::highLimit),
                Codec.INT.optionalFieldOf("down_limit" ,0).forGetter(BedwarsConfig::downLimit),
                Codec.INT.optionalFieldOf("void_high", 0).forGetter(BedwarsConfig::voidHigh),
                Identifier.CODEC.fieldOf("category_config").forGetter(BedwarsConfig::shopCategoriesId),
                Identifier.CODEC.fieldOf("entries_config").forGetter(BedwarsConfig::shopEntriesId)
        ).apply(instance, BedwarsConfig::new)
    );
}

