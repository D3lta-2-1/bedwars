package fr.delta.bedwars.game.shop.entries;

import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ForgeUpgradeEntry extends ShopEntry {

    public static ForgeUpgradeEntry INSTANCE = new ForgeUpgradeEntry();

    @Override
    public ShopEntry.Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var forge = bedwarsGame.getTeamComponentsFor(player).forge;
        var tier = forge.getNextTier();
        return tier == null ? null : tier.cost();
    }

    @Override
    public MutableText getName(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var forge = bedwarsGame.getTeamComponentsFor(player).forge;
        if(forge.isMaxed()) return Text.translatable("shop.bedwars.forgeMaxed").formatted(Formatting.RED);
        var nameKey = forge.getNextTier().nameKey();
        return nameKey != null ? Text.translatable(nameKey) : super.getName(bedwarsGame, player);
    }

    @Override
    public Item getDisplay(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        return Items.BLAST_FURNACE;
    }

    @Override
    public ShopEntry.BuyOffer canBeBough(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var forge = bedwarsGame.getTeamComponentsFor(player).forge;
        return new ShopEntry.BuyOffer(!forge.isMaxed(), Text.empty());
    }

    @Override
    public int displayCount(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var forge = bedwarsGame.getTeamComponentsFor(player).forge;
        return forge.getNextTierInt();
    }

    @Override
    public List<MutableText> getLore(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var forge = bedwarsGame.getTeamComponentsFor(player).forge;
        var tier = forge.getNextTier();
        if(tier == null) return null;
        var key = tier.descriptionKey();
        if(key == null) return null;
        return List.of(Text.translatable(key));
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var forge = bedwarsGame.getTeamComponentsFor(player).forge;
        forge.upgrade();
        return ItemStack.EMPTY;
    }
}
