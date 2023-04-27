package fr.delta.bedwars.game.shop.ShopMenu;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.shop.entries.ShopEntry;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.registry.TinyRegistry;
import java.util.List;

public class TeamShopMenu extends ShopMenu {
    private final List<Identifier> teamUpgrade;
    private final List<Identifier> traps;
    private final TinyRegistry<ShopEntry> entries;
    public TeamShopMenu(BedwarsActive bedwarsActive, TinyRegistry<ShopEntry> entries, List<Identifier> teamUpgrade, List<Identifier> traps, GameActivity activity)
    {
        super(bedwarsActive, activity);
        this.teamUpgrade = teamUpgrade;
        this.traps = traps;
        this.entries = entries;
    }
    @Override
    public void open(ServerPlayerEntity player) {
        var gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false)
        {
            @Override
            public boolean canPlayerClose() {
                return true;
            }
        };
        gui.setAutoUpdate(false);
        gui.setTitle(Text.translatable("teamShop.bedwars.title"));
        buildListAt(gui, teamUpgrade, entries,1, 1, 3, 2);
        buildListAt(gui, traps, entries,5, 1, 3, 2);
        buildSeparator(gui, 3);
        buildTrapQueueView(gui);
        gui.open();
    }

    @Override
    protected void afterPurchase(SlotGuiInterface gui) {
        buildTrapQueueView(gui);
        buildListAt(gui, traps, entries, 5, 1, 3, 2);
    }

    private void buildTrapQueueView(SlotGuiInterface gui)
    {
        var player = gui.getPlayer();
        var trapHandler = getBedwarsGame().getTeamComponentsFor(player).trapHandler;
        var trapIterator = trapHandler.iterator();
        for(int i = 0; i < 3; i++)
        {
            GuiElementBuilder element;
            int slot = (3 + i) + 4 * 9;
            if(trapIterator.hasNext())
            {
                //build trap view
                var trap = trapIterator.next();
                element = new GuiElementBuilder(trap.getItem());
                element.setName(trap.getName());
                element.getOrCreateNbt().putByte("HideFlags", (byte) 127);
            }
            else
            {
                element = new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS);
                element.setName(Text.translatable("trap.bedwars.slot").append(Text.literal(String.valueOf(i + 1))));
            }
            element.setCount(i + 1); //0 offset
            gui.setSlot(slot, element);
        }
    }
}
