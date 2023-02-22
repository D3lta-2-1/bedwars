package me.verya.bedwars.game.shop.Entry.Blocks;

import me.verya.bedwars.game.shop.Entry.ShopEntry;
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
        return Text.translatable("item.bedwars.Ladder");
    }

    public Item getItem()
    {
        return Items.LADDER;
    }
    public ItemStack onBuy(ServerPlayerEntity player)
    {
        var stack =  Items.LADDER.getDefaultStack();
        stack.setCount(16);
        return stack;
    }
}
