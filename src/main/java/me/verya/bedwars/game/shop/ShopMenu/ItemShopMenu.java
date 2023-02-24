package me.verya.bedwars.game.shop.ShopMenu;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.verya.bedwars.game.behavior.DefaultSword;
import me.verya.bedwars.game.shop.entry.articles.*;
import me.verya.bedwars.game.shop.entry.ShopEntry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;

import java.util.ArrayList;
import java.util.List;

public class ItemShopMenu extends ShopMenu{
    final private List<ShopEntry> ItemsInShop = new ArrayList<>();
    public ItemShopMenu(TeamManager teamManager, List<GameTeam> teamsInOrder, DefaultSword defaultSwordManager, GameActivity activity)
    {
        super(activity);
        ItemsInShop.add(new Wool(teamManager, teamsInOrder));
        ItemsInShop.add(new Ladder());
        ItemsInShop.add(new Wood());
        ItemsInShop.add(new EndStone());
        ItemsInShop.add(new StoneSword(defaultSwordManager));
        ItemsInShop.add(new IronSword(defaultSwordManager));
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
        buildMainMenu(gui);
        gui.open();
    }

    private void buildMainMenu(SimpleGui gui)
    {
        int i = 0;
        for(var article : ItemsInShop)
        {
            this.add(gui, article, i);
            i++;
        }
    }

}
