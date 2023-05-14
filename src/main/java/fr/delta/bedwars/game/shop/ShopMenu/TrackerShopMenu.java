package fr.delta.bedwars.game.shop.ShopMenu;

import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.shop.entries.ShopEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.util.ColoredBlocks;

public class TrackerShopMenu extends ShopMenu{

    private static class TargetEntry extends ShopEntry{

        private final GameTeam targetTeam;

        TargetEntry(GameTeam targetTeam)
        {
            this.targetTeam = targetTeam;
        }

        @Override
        public MutableText getName(BedwarsActive bedwarsGame, ServerPlayerEntity player)
        {
            return TextUtilities.getTranslation("name", targetTeam.config().blockDyeColor().name()).formatted(targetTeam.config().chatFormatting()).append(Text.translatable("shop.bedwars.Tracker"));
        }

        //todo: add config for cost
        @Override
        public Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player)
        {
            return new Cost(Items.EMERALD, 2);
        }

        @Override
        public Item getDisplay(BedwarsActive bedwarsGame, ServerPlayerEntity player)
        {
            return ColoredBlocks.banner(targetTeam.config().blockDyeColor()).asItem();
        }

        @Override
        public BuyOffer canBeBough(BedwarsActive bedwarsGame, ServerPlayerEntity player)
        {
            var isEmpty = bedwarsGame.getTeamManager().playersIn(targetTeam.key()).isEmpty();
            var isBedBroken = bedwarsGame.getTeamComponentsFor(targetTeam).bed.isBroken();
            return new BuyOffer(!isEmpty
                    && isBedBroken,
                    (isBedBroken ? Text.translatable("shop.bedwars.teamEliminated")
                            : Text.translatable("shop.bedwars.teamAlive")
                    ).formatted(Formatting.RED));
        }

        @Override
        public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player)
        {
            var targetedPlayer = bedwarsGame.getTeamManager().playersIn(targetTeam.key()).stream().findAny();
            assert targetedPlayer.isPresent();
            bedwarsGame.getCompassManager().setTarget(player, targetTeam.key(), targetedPlayer.get());
            return ItemStack.EMPTY;
        }
    }

    public TrackerShopMenu(BedwarsActive bedwarsGame, GameActivity activity)
    {
        super(bedwarsGame, activity);
    }

    @Override
    protected void afterPurchase(SlotGuiInterface gui) {
        gui.close();
    }

    @Override
    public void open(ServerPlayerEntity player)
    {
        var gui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false)
        {
            @Override
            public boolean canPlayerClose() {
                return true;
            }
        };
        gui.setAutoUpdate(false);
        gui.setTitle(Text.translatable("trackerShop.bedwars.title"));

        var game = getBedwarsGame();
        int i = 0;
        for (var team : game.getTeamsInOrder())
        {
            if (team == game.getTeamForPlayer(player)) continue;
            setEntryInSlot(gui, new TargetEntry(team), i);
            i++;
        }
        gui.open();
    }

}
