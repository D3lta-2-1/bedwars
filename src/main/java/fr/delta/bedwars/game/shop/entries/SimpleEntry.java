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
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleEntry extends ShopEntry {

    public static Codec<SimpleEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ITEM.getCodec().fieldOf("item").forGetter(SimpleEntry::getDisplay),
            Cost.CODEC.fieldOf("cost").forGetter(SimpleEntry::getCostNoArgument),
            Codec.INT.fieldOf("count").forGetter(SimpleEntry::getCount),
            RawEnchantmentData.CODEC.listOf().optionalFieldOf("enchantments", new ArrayList<>()).forGetter(SimpleEntry::getRawEnchantments),
            Codec.BOOL.optionalFieldOf("unbreakable", false).forGetter(SimpleEntry::isUnbreakable)
    ).apply(instance, SimpleEntry::new));

    record RawEnchantmentData(Identifier id, int level)
    {
        public static Codec<RawEnchantmentData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(RawEnchantmentData::id),
                Codec.INT.fieldOf("level").forGetter(RawEnchantmentData::level)
        ).apply(instance, RawEnchantmentData::new));
    }
    public SimpleEntry(Item item, Cost cost, int count, List<RawEnchantmentData> rawEnchantments, boolean isUnbreakable) {
        this.item = item;
        this.cost = cost;
        this.count = count;
        this.rawEnchantments = rawEnchantments;
        if(!rawEnchantments.isEmpty())
        {
            enchantments = new HashMap<>();
            rawEnchantments.forEach(rawEnchantmentData -> {
                var enchantment = Registries.ENCHANTMENT.get(rawEnchantmentData.id());
                if(enchantment != null)
                    enchantments.put(enchantment, rawEnchantmentData.level());
            });
        }
        else
        {
            enchantments = null;
        }
        this.isUnbreakable = isUnbreakable;
    }

    final private Item item;
    final private Cost cost;
    final private int count;
    final private List<RawEnchantmentData> rawEnchantments;
    final private Map<Enchantment, Integer> enchantments;

    final private boolean isUnbreakable;

    public boolean isUnbreakable() {
        return isUnbreakable;
    }


    public List<RawEnchantmentData> getRawEnchantments() {
        return rawEnchantments;
    }

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
