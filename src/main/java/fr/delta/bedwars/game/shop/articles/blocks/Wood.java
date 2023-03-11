package fr.delta.bedwars.game.shop.articles.blocks;

import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.shop.articles.ShopEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class Wood  implements ShopEntry {
    static public ShopEntry INSTANCE = new Wood();
    public ShopEntry.Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return new ShopEntry.Cost(Items.GOLD_INGOT, 4);
    }
    public MutableText getName()
    {
        return Text.translatable("shop.bedwars.wood");
    }

    public Item getDisplay()
    {
        return Items.OAK_PLANKS;
    }
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return Items.OAK_PLANKS.getDefaultStack();
    }

    @Override
    public int getCount() {
        return 16;
    }
}
