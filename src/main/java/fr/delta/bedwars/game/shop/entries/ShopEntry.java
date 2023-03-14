package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import java.util.List;

public abstract class ShopEntry {
    public record Cost(Item item, int count)
    {
        public static final Codec<Cost> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Registries.ITEM.getCodec().fieldOf("item").forGetter(Cost::item),
                    Codec.INT.fieldOf("count").forGetter(Cost::count)
                            ).apply(instance, Cost::new));
    }
    public record BuyOffer(boolean isSuccess, Text errorMessage){}

    /**
     * set the cost of an entry
     * @param BedwarsGame the game were this item is displayed
     * @param player the player who opened the shop
     */
    public Cost getCost(BedwarsActive BedwarsGame, ServerPlayerEntity player) { return new Cost(Items.IRON_INGOT, 4); }

    /**
     * set the name of an entry
     */
    public MutableText getName() { return Text.literal("Unnamed"); }

    /**
     * set the item display in the gui of the shopkeeper
     */
    public Item getDisplay() { return Items.STRUCTURE_VOID; }

    /**
     * set how many item should be display in the gui of the shopkeeper
     */
    public int displayCount() { return 1; }

    /**
     * set if the item should glint in the gui of the shopkeeper
     */
    public boolean hasGlint() { return false; }

    /**
     * set additobnal lore of an item
     * @param bedwarsGame the game were this item is displayed
     * @param player the player who opened the shop
     */
    public List<MutableText> getLore(BedwarsActive bedwarsGame, ServerPlayerEntity player) { return null; }

    public BuyOffer canBeBough(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return new BuyOffer(true, null);
    }
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player) { return null; }

    public int getCount() { return 1; }
}
