package fr.delta.bedwars.game.shop.ShopMenu;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.shop.entries.EmptyEntry;
import fr.delta.bedwars.game.shop.entries.ShopEntry;
import fr.delta.bedwars.game.shop.data.ItemShopConfig;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.util.List;

public class ItemShopMenu extends ShopMenu{
    final ItemShopConfig categories;
    final TinyRegistry<ShopEntry> entries;

    public ItemShopMenu(BedwarsActive bedwarsActive, TinyRegistry<ShopEntry> entries, ItemShopConfig categories, GameActivity activity)
    {
        super(bedwarsActive, activity);
        this.categories = categories;
        this.entries = entries;
    }
    public void open(ServerPlayerEntity player)
    {
        var gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false)
        {
            @Override
            public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                return super.onClick(index, type, action, element);
            }
            @Override
            public boolean canPlayerClose() {
                return true;
            }
        };
        gui.setAutoUpdate(false);
        addCategory(gui);
        gui.open();
    }

    private void addCategory(SimpleGui gui)
    {
        int slot = 1;
        for(var category : categories.categories())
        {
            var builder = new GuiElementBuilder();
            builder.setItem(category.icon());
            builder.setName(Text.translatable(category.name()));
            builder.setCallback( click -> buildMenu(gui, category.entries()));
            gui.setSlot(slot, builder);
            slot++;
        }
    }

    private void buildMenu(SimpleGui gui, List<Identifier> entriesIDs)
    {
        int x = 0;
        int y = 0;
        var iter = entriesIDs.iterator();
        while(x != 7)
        {
            if(iter.hasNext())
            {
                var entry = entries.get(iter.next());
                if(entry == null)
                    setEntryInSlot(gui, EmptyEntry.INSTANCE, (1 + x) + (2 + y) * 9);
                else
                    setEntryInSlot(gui, entry, (1 + x) + (2 + y) * 9);
            }
            else
            {
                gui.clearSlot((1 + x) + (2 + y) * 9);
            }
            x++;
            if(x == 7)
            {
                if(y != 3)
                {
                    x %= 7;
                    y++;
                }
            }
        }
    }
}
