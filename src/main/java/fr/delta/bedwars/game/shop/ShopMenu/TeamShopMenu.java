package fr.delta.bedwars.game.shop.ShopMenu;

import eu.pb4.sgui.api.gui.SimpleGui;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.shop.entries.ShopEntry;
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
    public TeamShopMenu(BedwarsActive bedwarsActive, TinyRegistry<ShopEntry> entries, List<Identifier> teamUpgrade, List<Identifier> traps, GameActivity activity)
    {
        super(bedwarsActive, entries, activity);
        this.teamUpgrade = teamUpgrade;
        this.traps = traps;
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
        buildListAt(gui, teamUpgrade, 1, 1, 3, 2);
        buildListAt(gui, traps, 5, 1, 3, 2);
        buildSeparator(gui, 3);
        gui.open();
    }


}
