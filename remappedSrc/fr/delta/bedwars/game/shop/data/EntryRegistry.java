package fr.delta.bedwars.game.shop.data;

import com.mojang.serialization.Codec;
import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.game.shop.entries.*;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

public class EntryRegistry {

    //public record ShopCategory(MutableText Name, Item display, List<ShopEntry> Entries) {}
    static private void register(String name, Codec<? extends ShopEntry> codec)
    {
        SHOP_ENTRY_CODECS.register(new Identifier(Bedwars.ID, name), codec);
    }

    static public TinyRegistry<Codec<? extends ShopEntry>> SHOP_ENTRY_CODECS = TinyRegistry.create();
    static
    {
        register("simple_entry", SimpleEntry.CODEC);
        register("colored_wool", ColoredBlockEntry.WOOL_CODEC);
        register("colored_terracotta", ColoredBlockEntry.TERRACOTTA_CODEC);
        register("colored_glass", ColoredBlockEntry.GLASS_CODEC);
        register("sword_entry", SwordEntry.CODEC);
        register("armor_entry", ArmorEntry.CODEC);
        register("tool_entry", ToolEntry.CODEC);
        register("potion_item_entry", PotionEntry.CODEC);
        register("team_effect_entry", EffectEntry.CODEC);
        register("enchantment_entry", EnchantmentEntry.CODEC);
        register("effect_pool_entry", EffectPoolEntry.CODEC);
        register("trap_entry", TrapEntry.CODEC);
    }
}
