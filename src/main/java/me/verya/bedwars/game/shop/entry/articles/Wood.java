package me.verya.bedwars.game.shop.entry.articles;

import me.verya.bedwars.game.shop.entry.ShopEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class Wood  implements ShopEntry {
    public ShopEntry.Cost getCost()
    {
        return new ShopEntry.Cost(Items.GOLD_INGOT, 4);
    }
    public MutableText getTitle()
    {
        return Text.translatable("shop.bedwars.wood");
    }

    public Item getItem()
    {
        return Items.OAK_PLANKS;
    }
    public ItemStack onBuy(ServerPlayerEntity player)
    {
        return Items.OAK_PLANKS.getDefaultStack();
    }

    @Override
    public int getCount() {
        return 16;
    }
}
