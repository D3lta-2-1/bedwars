package fr.delta.bedwars.game.shop.articles.armors;

import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.player.PlayerArmorManager;
import fr.delta.bedwars.game.shop.articles.ShopEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collections;
import java.util.List;

public class ChainMailArmor implements ShopEntry {

    static public ShopEntry INSTANCE = new ChainMailArmor();
    @Override
    public Cost getCost(BedwarsActive BedwarsGame, ServerPlayerEntity player) {
        return new Cost(Items.IRON_INGOT, 30);
    }

    @Override
    public MutableText getName() {
        return Text.translatable("shop.bedwars.chainMailArmor");
    }

    @Override
    public Item getDisplay() {
        return Items.CHAINMAIL_BOOTS;
    }

    @Override
    public BuyOffer canBeBough(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        if(!bedwarsGame.getInventoryManager().getArmorManager(player).getLevel().smallerThan(PlayerArmorManager.ArmorLevel.CHAIN))
            return new BuyOffer(false, Text.translatable("armor.bedwars.alreadyGotBetterArmor").setStyle(Style.EMPTY.withFormatting(Formatting.RED)));
        return new BuyOffer(true, null);
    }

    @Override
    public List<MutableText> getLore(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        if(!bedwarsGame.getInventoryManager().getArmorManager(player).getLevel().smallerThan(PlayerArmorManager.ArmorLevel.CHAIN))
            return Collections.singletonList(Text.translatable("armor.bedwars.alreadyGotBetterArmor").setStyle(Style.EMPTY.withFormatting(Formatting.RED)));
        return null;
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        bedwarsGame.getInventoryManager().getArmorManager(player).setLevel(PlayerArmorManager.ArmorLevel.CHAIN);
        return ItemStack.EMPTY;
    }

}
