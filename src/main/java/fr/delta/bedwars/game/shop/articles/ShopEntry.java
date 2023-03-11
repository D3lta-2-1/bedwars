package fr.delta.bedwars.game.shop.articles;

import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

public interface ShopEntry {

    record Cost(Item item, int count){}
    record BuyOffer(boolean isSuccess, Text errorMessage){}

    /**
     * set the cost of an entry
     * @param BedwarsGame the game were this item is displayed
     * @param player the player who opened the shop
     */
    default Cost getCost(BedwarsActive BedwarsGame, ServerPlayerEntity player) { return new Cost(Items.IRON_INGOT, 4); }

    /**
     * set the name of an entry
     */
    default MutableText getName() { return Text.literal("[unnamed]"); }

    /**
     * set the item display in the gui of the shopkeeper
     */
    default Item getDisplay() { return Items.STRUCTURE_VOID; }

    /**
     * set how many item should be display in the gui of the shopkeeper
     */
    default int displayCount() { return 1; }

    /**
     * set if the item should glint in the gui of the shopkeeper
     */
    default boolean hasGlint() { return false; }

    /**
     * set additobnal lore of an item
     * @param bedwarsGame the game were this item is displayed
     * @param player the player who opened the shop
     */
    default List<MutableText> getLore(BedwarsActive bedwarsGame, ServerPlayerEntity player) { return null; }

    default BuyOffer canBeBough(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return new BuyOffer(true, null);
    }
    default ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player) { return null; }

    default int getCount() { return 1; }
}
