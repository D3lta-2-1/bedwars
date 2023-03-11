package fr.delta.bedwars.game.shop.articles;

import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class GoldenApple implements ShopEntry {
    static public ShopEntry INSTANCE = new GoldenApple();
    @Override
    public ShopEntry.Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return new ShopEntry.Cost(Items.GOLD_INGOT, 3);
    }
    @Override
    public MutableText getName()
    {
        return Text.translatable(Items.GOLDEN_APPLE.getTranslationKey());
    }
    @Override
    public Item getDisplay()
    {
        return Items.GOLDEN_APPLE;
    }
    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return Items.GOLDEN_APPLE.getDefaultStack();
    }

    @Override
    public int getCount() {
        return 1;
    }
}
