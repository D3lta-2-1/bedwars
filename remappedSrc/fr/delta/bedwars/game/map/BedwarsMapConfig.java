package fr.delta.bedwars.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record BedwarsMapConfig(Identifier id) {
    public static final Codec<BedwarsMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(BedwarsMapConfig::id)
    ).apply(instance, BedwarsMapConfig::new));
}
