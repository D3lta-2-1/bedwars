package fr.delta.bedwars.game.shop.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;

public record ShopCategoriesConfig(List<Category> ItemShopCategories, List<Identifier> teamUpgrade, List<Identifier> traps)
{
    public static final Codec<ShopCategoriesConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Category.CODEC.listOf().fieldOf("item_shop_categories").forGetter(ShopCategoriesConfig::ItemShopCategories),
            Identifier.CODEC.listOf().fieldOf("team_upgrade").forGetter(ShopCategoriesConfig::teamUpgrade),
            Identifier.CODEC.listOf().fieldOf("traps").forGetter(ShopCategoriesConfig::traps)
            ).apply(instance, ShopCategoriesConfig::new)
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
