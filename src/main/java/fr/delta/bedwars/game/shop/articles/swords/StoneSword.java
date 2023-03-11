package fr.delta.bedwars.game.shop.articles.swords;

import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.shop.articles.ShopEntry;
import fr.delta.notasword.item.OldSwords;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;


public class StoneSword implements ShopEntry {
    static public ShopEntry INSTANCE = new StoneSword();
    @Override
    public ShopEntry.Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return new ShopEntry.Cost(Items.IRON_INGOT, 10);
    }
    @Override
    public MutableText getName()
    {
        return Text.translatable(OldSwords.STONE_SWORD.getTranslationKey());
    }

    @Override
    public Item getDisplay()
    {
        return OldSwords.STONE_SWORD;
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        bedwarsGame.getDefaultSwordManager().remove(player);
        return ItemStackBuilder.of(OldSwords.STONE_SWORD).setUnbreakable().build();
    }
}
