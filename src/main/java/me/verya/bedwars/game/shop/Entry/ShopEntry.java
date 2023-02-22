package me.verya.bedwars.game.shop.Entry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

public interface ShopEntry {
    public class Cost
    {
        public Cost(Item item, int count) { this.item = item; this.count = count; }
        public Item item;
        public int count;
    }

    Cost getCost();
    MutableText getTitle();
    Item getItem();
    ItemStack onBuy(ServerPlayerEntity player);
}
