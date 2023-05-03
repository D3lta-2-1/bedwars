package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.BedwarsActive;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.Map;
import java.util.Map.Entry;

public class SwordEntry extends ShopEntry {

    public static Codec<SwordEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ITEM.getCodec().fieldOf("item").forGetter(SwordEntry::getDisplayNoArgument),
            Cost.CODEC.fieldOf("cost").forGetter(SwordEntry::getCostNoArgument)
    ).apply(instance, SwordEntry::new));
    public SwordEntry(Item item, Cost cost) {
        this.item = item;
        this.cost = cost;
    }

    @Override
    public Map<Enchantment, Integer> enchantment(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var enchantment = bedwarsGame.getTeamComponentsFor(player).enchantments;
        Map<Enchantment, Integer> suitableEnchantment = new Object2IntArrayMap<>();
        for(var entry : enchantment.entrySet())
        {
            if(entry.getKey().target == EnchantmentTarget.WEAPON)
                suitableEnchantment.put(entry.getKey(), entry.getValue());
        }
        return suitableEnchantment;
    }

    final private Item item;
    final private Cost cost;

    public Cost getCostNoArgument() {
        return cost;
    }
    public Item getDisplayNoArgument() {
        return item;
    }

    @Override
    public ShopEntry.Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return cost;
    }
    @Override
    public MutableText getName(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return Text.translatable(item.getTranslationKey());
    }

    @Override
    public Item getDisplay(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return item;
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        bedwarsGame.getDefaultSwordManager().removeDefaultSword(player);
        var builder = ItemStackBuilder.of(item).setUnbreakable();
        var enchantment = bedwarsGame.getTeamComponentsFor(player).enchantments;
        for(var entry : enchantment.entrySet())
        {
            if(entry.getKey().target == EnchantmentTarget.WEAPON)
                builder.addEnchantment(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }
}
