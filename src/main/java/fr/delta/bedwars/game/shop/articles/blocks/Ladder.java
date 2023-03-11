package fr.delta.bedwars.game.shop.articles.blocks;

import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.shop.articles.ShopEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class Ladder implements ShopEntry {
    static public ShopEntry INSTANCE = new Ladder();
    @Override
    public ShopEntry.Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return new ShopEntry.Cost(Items.IRON_INGOT, 4);
    }
    @Override
    public MutableText getName()
    {
        return Text.translatable(Items.LADDER.getTranslationKey());
    }

    @Override
    public Item getDisplay()
    {
        return Items.LADDER;
    }
    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return Items.LADDER.getDefaultStack();
    }

    @Override
    public int getCount() {
        return 16;
    }
}
