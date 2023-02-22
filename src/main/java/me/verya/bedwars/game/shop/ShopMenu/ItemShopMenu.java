package me.verya.bedwars.game.shop.ShopMenu;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.verya.bedwars.game.shop.Entry.Blocks.Ladder;
import me.verya.bedwars.game.shop.Entry.Blocks.Wool;
import me.verya.bedwars.game.shop.Entry.ShopEntry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;

import java.util.ArrayList;
import java.util.List;

public class ItemShopMenu implements ShopMenu{
    final private List<ShopEntry> ItemsInShop = new ArrayList<>();
    final private GameActivity activity;

    public ItemShopMenu(TeamManager teamManager, List<GameTeam> teamsInOrder, GameActivity activity)
    {
        ItemsInShop.add(new Wool(teamManager, teamsInOrder));
        ItemsInShop.add(new Ladder());
        this.activity = activity;
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
            var guiElement = new GuiElementBuilder();
            guiElement.setItem(article.getItem());
            guiElement.setName(article.getTitle());
            var lore = new ArrayList<Text>();
            lore.add(Text.literal("cost: " + article.getCost().count + " ").append(Text.translatable(article.getCost().item.getTranslationKey())).setStyle(Style.EMPTY.withFormatting(Formatting.GRAY)));
            lore.add(Text.empty());
            lore.add(Text.literal("Click to purchase").setStyle(Style.EMPTY.withFormatting(Formatting.YELLOW)));
            guiElement.setLore(lore);
            guiElement.setCallback( (index, type, action, guiInterface) -> purchase(article, type, guiInterface, activity));
            gui.setSlot(i, guiElement);
            i++;
        }
    }

}
