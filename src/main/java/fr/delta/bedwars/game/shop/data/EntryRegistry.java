package fr.delta.bedwars.game.shop.data;

import com.mojang.serialization.Codec;
import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.game.shop.entries.ShopEntry;
import fr.delta.bedwars.game.shop.entries.SimpleEntry;
import fr.delta.bedwars.game.shop.entries.ArmorEntry;
import fr.delta.bedwars.game.shop.entries.WoolEntry;
import fr.delta.bedwars.game.shop.entries.SwordEntry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

public class EntryRegistry {

    //public record ShopCategory(MutableText Name, Item display, List<ShopEntry> Entries) {}


    static public TinyRegistry<Codec<? extends ShopEntry>> SHOP_ENTRY_CODECS = TinyRegistry.create();
    static
    {
        SHOP_ENTRY_CODECS.register(new Identifier(Bedwars.ID, "simple_entry"), SimpleEntry.CODEC);
        SHOP_ENTRY_CODECS.register(new Identifier(Bedwars.ID, "colored_wool"), WoolEntry.CODEC);
        SHOP_ENTRY_CODECS.register(new Identifier(Bedwars.ID, "sword_entry"), SwordEntry.CODEC);
        SHOP_ENTRY_CODECS.register(new Identifier(Bedwars.ID, "armor_entry"), ArmorEntry.CODEC);
    }
}
