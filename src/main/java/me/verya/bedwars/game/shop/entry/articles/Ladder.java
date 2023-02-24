package me.verya.bedwars.game.shop.entry.articles;

import me.verya.bedwars.game.shop.entry.ShopEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class Ladder implements ShopEntry {
    public ShopEntry.Cost getCost()
    {
        return new ShopEntry.Cost(Items.IRON_INGOT, 4);
    }
    public MutableText getTitle()
    {
        return Text.translatable(Items.LADDER.getTranslationKey());
    }

    public Item getItem()
    {
        return Items.LADDER;
    }
    public ItemStack onBuy(ServerPlayerEntity player)
    {
        return Items.LADDER.getDefaultStack();
    }

    @Override
    public int getCount() {
        return 16;
    }
}
