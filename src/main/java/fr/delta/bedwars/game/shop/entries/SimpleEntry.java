package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class SimpleEntry extends ShopEntry {

    public static Codec<SimpleEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ITEM.getCodec().fieldOf("item").forGetter(SimpleEntry::getDisplay),
            Cost.CODEC.fieldOf("cost").forGetter(SimpleEntry::getCostNoArgument),
            Codec.INT.fieldOf("count").forGetter(SimpleEntry::getCount)
    ).apply(instance, SimpleEntry::new));
    public SimpleEntry(Item item, Cost cost, int count) {
        this.item = item;
        this.cost = cost;
        this.count = count;
    }

    final private Item item;
    final private Cost cost;
    final private int count;

    public Cost getCostNoArgument() {
        return cost;
    }

    @Override
    public Cost getCost(BedwarsActive BedwarsGame, ServerPlayerEntity player) {
        return cost;
    }

    @Override
    public MutableText getName()
    {
        return Text.translatable(item.getTranslationKey());
    }

    @Override
    public Item getDisplay()
    {
        return item;
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return item.getDefaultStack();
    }

    @Override
    public int getCount() {
        return count;
    }
}
