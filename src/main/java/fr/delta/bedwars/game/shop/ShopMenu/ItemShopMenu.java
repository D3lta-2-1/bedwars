package fr.delta.bedwars.game.shop.ShopMenu;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.shop.articles.ShopEntry;
import fr.delta.bedwars.game.shop.categories.ShopRegistries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.GameActivity;

import java.util.List;

public class ItemShopMenu extends ShopMenu{
    public ItemShopMenu(BedwarsActive bedwarsActive, GameActivity activity)
    {
        super(bedwarsActive, activity);
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
        int slot = 0;
        for(var category : ShopRegistries.ItemShopCategories)
        {
            var builder = new GuiElementBuilder();
            builder.setItem(category.display());
            builder.setName(category.Name());
            builder.setCallback( click -> buildMenu(gui, category.Entries()));
            gui.setSlot(slot, builder);
            slot++;
        }
    }

    private void buildMenu(SimpleGui gui, List<ShopEntry> entries)
    {
        int x = 0;
        int y = 0;
        var iter = entries.iterator();
        while(x != 5 && y != 3)
        {
            if(iter.hasNext())
            {
                setEntryInSlot(gui, iter.next(), (1 + x) + (2 + y) * 9);
            }
            else
            {
                gui.clearSlot((1 + x) + (2 + y) * 9);
            }
            x++;
            if(x == 6)
            {
                x %= 7;
                y++;
            }
        }
    }
}
