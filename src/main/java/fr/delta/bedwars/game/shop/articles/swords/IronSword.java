package fr.delta.bedwars.game.shop.articles.swords;

import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.shop.articles.ShopEntry;
import fr.delta.notasword.item.OldSwords;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class IronSword implements ShopEntry {
    static public ShopEntry INSTANCE = new IronSword();
    @Override
    public ShopEntry.Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return new ShopEntry.Cost(Items.GOLD_INGOT, 7);
    }
    @Override
    public MutableText getName()
    {
        return Text.translatable(OldSwords.IRON_SWORD.getTranslationKey());
    }

    @Override
    public Item getDisplay()
    {
        return OldSwords.IRON_SWORD;
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        bedwarsGame.getDefaultSwordManager().remove(player);
        return ItemStackBuilder.of(OldSwords.IRON_SWORD).setUnbreakable().build();
    }
}
