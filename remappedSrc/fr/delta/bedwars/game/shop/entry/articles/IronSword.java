package fr.delta.bedwars.game.shop.entry.articles;

import fr.delta.bedwars.game.behaviour.DefaultSword;
import fr.delta.bedwars.game.shop.entry.ShopEntry;
import fr.delta.notasword.item.OldSwords;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class IronSword implements ShopEntry {
    final DefaultSword defaultSwordManager;
    public IronSword(DefaultSword defaultSwordManager)
    {
        this.defaultSwordManager = defaultSwordManager;
    }
    public ShopEntry.Cost getCost()
    {
        return new ShopEntry.Cost(Items.GOLD_INGOT, 7);
    }
    public MutableText getTitle()
    {
        return Text.translatable(OldSwords.IRON_SWORD.getTranslationKey());
    }

    public Item getItem()
    {
        return OldSwords.IRON_SWORD;
    }

    public ItemStack onBuy(ServerPlayerEntity player)
    {
        defaultSwordManager.remove(player);
        return ItemStackBuilder.of(OldSwords.IRON_SWORD).setUnbreakable().build();
    }
}
