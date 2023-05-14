package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.HashMap;
import java.util.Map;

public class TeamColoredEntry extends SimpleEntry{

    public static Codec<TeamColoredEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ITEM.getCodec().fieldOf("item").forGetter(SimpleEntry::getDisplayNoArgument),
            Cost.CODEC.fieldOf("cost").forGetter(SimpleEntry::getCostNoArgument),
            Codec.INT.fieldOf("count").forGetter(SimpleEntry::getCount),
            Codec.unboundedMap(Registries.ENCHANTMENT.getCodec(), Codec.INT).optionalFieldOf("enchantments", new HashMap<>()).forGetter(SimpleEntry::getEnchantments),
            Codec.BOOL.optionalFieldOf("unbreakable", false).forGetter(SimpleEntry::isUnbreakable)
    ).apply(instance, TeamColoredEntry::new));

    public TeamColoredEntry(Item item, Cost cost, int count, Map<Enchantment, Integer> enchantments, boolean isUnbreakable) {
        super(item, cost, count, enchantments, isUnbreakable);
    }

    @Override
    public void editNbt(NbtCompound nbt, BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        nbt.putInt("Color", bedwarsGame.getTeamForPlayer(player).config().blockDyeColor().getFireworkColor());
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        var stack = ItemStackBuilder.of(getDisplayNoArgument());
        if(getEnchantments() != null)
            getEnchantments().forEach(stack::addEnchantment);
        if(isUnbreakable())
            stack.setUnbreakable();
        var out = stack.build();
        out.getOrCreateNbt().putInt("Color", bedwarsGame.getTeamForPlayer(player).config().blockDyeColor().getFireworkColor()); //could be only added if the item supports it
        return out;
    }
}
