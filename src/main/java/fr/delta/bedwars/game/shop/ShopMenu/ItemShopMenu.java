package fr.delta.bedwars.game.shop.ShopMenu;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.shop.entries.ShopEntry;
import fr.delta.bedwars.game.shop.data.ShopCategoriesConfig;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.util.List;

public class ItemShopMenu extends ShopMenu {
    final List<ShopCategoriesConfig.Category> categories;

    public ItemShopMenu(BedwarsActive bedwarsActive, TinyRegistry<ShopEntry> entries, List<ShopCategoriesConfig.Category> categories, GameActivity activity)
    {
        super(bedwarsActive, entries, activity);
        this.categories = categories;
    }
    public void open(ServerPlayerEntity player)
    {
        var gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false)
        {
            @Override
            public boolean canPlayerClose() {
                return true;
            }
        };
        gui.setAutoUpdate(false);
        gui.setTitle(Text.translatable("itemShop.bedwars.title"));
        addCategories(gui);
        buildSeparator(gui, 1);
        gui.open();
    }

    private void addCategories(SimpleGui gui)
    {
        int slot = 1;
        for(var category : categories)
        {
            var builder = new GuiElementBuilder();
            builder.setItem(category.icon());
            builder.setName(Text.translatable(category.name()));
            builder.getOrCreateNbt().putByte("HideFlags", (byte) 127);
            builder.setCallback( click -> buildListAt(gui, category.entries(), 1,2,7,3));
            gui.setSlot(slot, builder);
            slot++;
        }
    }
}
