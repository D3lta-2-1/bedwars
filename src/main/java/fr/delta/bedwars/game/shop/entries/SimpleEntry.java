package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.HashMap;
import java.util.Map;

public class SimpleEntry extends ShopEntry {

    public static Codec<SimpleEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ITEM.getCodec().fieldOf("item").forGetter(SimpleEntry::getDisplayNoArgument),
            Cost.CODEC.fieldOf("cost").forGetter(SimpleEntry::getCostNoArgument),
            Codec.INT.fieldOf("count").forGetter(SimpleEntry::getCount),
            Codec.unboundedMap(Registries.ENCHANTMENT.getCodec(), Codec.INT).optionalFieldOf("enchantments", new HashMap<>()).forGetter(SimpleEntry::getEnchantments),
            Codec.BOOL.optionalFieldOf("unbreakable", false).forGetter(SimpleEntry::isUnbreakable)
    ).apply(instance, SimpleEntry::new));

    public SimpleEntry(Item item, Cost cost, int count, Map<Enchantment, Integer> enchantments, boolean isUnbreakable) {
        this.item = item;
        this.cost = cost;
        this.count = count;
        this.enchantments = enchantments;
        this.isUnbreakable = isUnbreakable;
    }

    final private Item item;
    final private Cost cost;
    final private int count;

    final private Map<Enchantment, Integer> enchantments;

    final private boolean isUnbreakable;

    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    public boolean isUnbreakable() {
        return isUnbreakable;
    }


    public Cost getCostNoArgument() {
        return cost;
    }
    public Item getDisplayNoArgument() {
        return item;
    }

    @Override
    public Cost getCost(BedwarsActive BedwarsGame, ServerPlayerEntity player) {
        return cost;
    }

    @Override
    public MutableText getName(BedwarsActive BedwarsGame, ServerPlayerEntity player)
    {
        return Text.translatable(item.getTranslationKey());
    }

    @Override
    public Item getDisplay(BedwarsActive BedwarsGame, ServerPlayerEntity player)
    {
        return item;
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        var stack = ItemStackBuilder.of(item);
        if(enchantments != null)
            enchantments.forEach(stack::addEnchantment);
        if(isUnbreakable)
            stack.setUnbreakable();
        return stack.build();
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Map<Enchantment, Integer> enchantment(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        return enchantments;
    }
}
