package fr.delta.bedwars.data;

import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.shop.entries.ForgeUpgradeEntry;
import fr.delta.bedwars.game.shop.entries.ShopEntry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShopEntryGetter {

    private final List<TinyRegistry<ShopEntry>> shopEntryPages;
    private static final TinyRegistry<ShopEntry> DEFAULT_ENTRIES = TinyRegistry.create();
    static
    {
        DEFAULT_ENTRIES.register(new Identifier(Bedwars.ID, "forge_upgrade"), ForgeUpgradeEntry.INSTANCE);
    }
    public ShopEntryGetter(TinyRegistry<TinyRegistry<ShopEntry>> shopEntriesRegistryRegistry, List<Identifier> entryPages, BedwarsActive game)
    {
        var shopEntries = new ArrayList<TinyRegistry<ShopEntry>>(entryPages.size());
        for (var entryPage : entryPages)
        {
            var page = shopEntriesRegistryRegistry.get(entryPage);
            if(page == null) throw new NullPointerException(entryPage.toString() + " entry Page does not exist");
            shopEntries.add(page);
        }
        shopEntries.add(DEFAULT_ENTRIES);
        this.shopEntryPages = shopEntries;
        initialize(game);
    }

    /**
     * Get a shop entry from its id
     * @param entryId the id of the entry
     * @return the shop entry or null if it does not exist, if more than one entry has the same id, the one from the first page which contain this id will be returned
     */

    public ShopEntry get(Identifier entryId)
    {
        for(var shopEntry : shopEntryPages)
        {
            var entry = shopEntry.get(entryId);
            if(entry != null) return entry;
        }
        return null;
    }

    private void initialize(BedwarsActive game) //not super optimized, but it's not like it's called every tick
    {
        var alreadyAddedEntry = new ArrayList<Identifier>();

        for(var page : shopEntryPages)
        {
            for(var entry : page.keySet())
            {
                if(alreadyAddedEntry.contains(entry)) continue;
                alreadyAddedEntry.add(entry);
                Objects.requireNonNull(page.get(entry)).setup(game);
            }
        }

    }
}
