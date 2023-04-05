package fr.delta.bedwars.game.shop.entry.articles;

import fr.delta.bedwars.game.shop.entry.ShopEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class GoldenApple implements ShopEntry {
    public ShopEntry.Cost getCost()
    {
        return new ShopEntry.Cost(Items.GOLD_INGOT, 3);
    }
    public MutableText getTitle()
    {
        return Text.translatable(Items.GOLDEN_APPLE.getTranslationKey());
    }

    public Item getItem()
    {
        return Items.GOLDEN_APPLE;
    }
    public ItemStack onBuy(ServerPlayerEntity player)
    {
        return Items.GOLDEN_APPLE.getDefaultStack();
    }

    @Override
    public int getCount() {
        return 1;
    }
}
