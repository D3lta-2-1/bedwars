package fr.delta.bedwars.game.shop.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;

public record ItemShopConfig(List<Category> categories) {
    public static final Codec<ItemShopConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Category.CODEC.listOf().fieldOf("categories").forGetter(ItemShopConfig::categories)
            ).apply(instance, ItemShopConfig::new)
    );

    public record Category(String name ,Item icon, List<Identifier> entries)
    {
        public static final Codec<Category> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Category::name),
                Registries.ITEM.getCodec().fieldOf("icon").forGetter(Category::icon),
                Identifier.CODEC.listOf().fieldOf("entries").forGetter(Category::entries)
                ).apply(instance, Category::new)
        );
    }
}
