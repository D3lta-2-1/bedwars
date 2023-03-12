package fr.delta.bedwars.game.shop.data;

import com.mojang.serialization.Codec;
import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.game.shop.articles.ShopEntry;
import fr.delta.bedwars.game.shop.articles.SimpleEntry;
import fr.delta.bedwars.game.shop.articles.armors.ArmorEntry;
import fr.delta.bedwars.game.shop.articles.blocks.Wool;
import fr.delta.bedwars.game.shop.articles.swords.SwordEntry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

public class EntryRegistry {

    //public record ShopCategory(MutableText Name, Item display, List<ShopEntry> Entries) {}


    static public TinyRegistry<Codec<? extends ShopEntry>> SHOP_ENTRY_CODECS = TinyRegistry.create();
    static
    {
        SHOP_ENTRY_CODECS.register(new Identifier(Bedwars.ID, "simple_entry"), SimpleEntry.CODEC);
        SHOP_ENTRY_CODECS.register(new Identifier(Bedwars.ID, "colored_wool"), Wool.CODEC);
        SHOP_ENTRY_CODECS.register(new Identifier(Bedwars.ID, "sword_entry"), SwordEntry.CODEC);
        SHOP_ENTRY_CODECS.register(new Identifier(Bedwars.ID, "armor_entry"), ArmorEntry.CODEC);
    }
}
