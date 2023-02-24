package me.verya.bedwars.game.shop.entry.articles;

import me.verya.bedwars.customItem.OldSword;
import me.verya.bedwars.game.behavior.DefaultSword;
import me.verya.bedwars.game.shop.entry.ShopEntry;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;


public class StoneSword implements ShopEntry {
    final DefaultSword defaultSwordManager;
    public StoneSword(DefaultSword defaultSwordManager)
    {
        this.defaultSwordManager = defaultSwordManager;
    }
    public ShopEntry.Cost getCost()
    {
        return new ShopEntry.Cost(Items.IRON_INGOT, 10);
    }
    public MutableText getTitle()
    {
        return Text.translatable(OldSword.STONE_SWORD.getTranslationKey());
    }

    public Item getItem()
    {
        return OldSword.STONE_SWORD;
    }

    public ItemStack onBuy(ServerPlayerEntity player)
    {
        defaultSwordManager.remove(player);
        return ItemStackBuilder.of(OldSword.STONE_SWORD).setUnbreakable().build();
    }
}
