package fr.delta.bedwars.game.shop.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.shop.articles.ShopEntry;

import java.util.List;

public record ShopConfig(List<ShopEntry> configs) {
    public static final Codec<ShopConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ShopEntryConfig.CODEC.listOf().fieldOf("entries").forGetter(ShopConfig::configs)
            ).apply(instance, ShopConfig::new)
    );
}
