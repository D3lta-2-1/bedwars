package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.player.PlayerArmorManager;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;
import java.util.Map;

public class ArmorEntry extends ShopEntry {

    public static Codec<ArmorEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ITEM.getCodec().fieldOf("icon").forGetter(ArmorEntry::getDisplayNoArgument),
            Cost.CODEC.fieldOf("cost").forGetter(ArmorEntry::getCostNoArgument),
            Codec.STRING.fieldOf("level").forGetter(ArmorEntry::getLevel)
    ).apply(instance, ArmorEntry::new));
    public ArmorEntry(Item icon, Cost cost, String level) {
        this.icon = icon;
        this.cost = cost;
        this.level = level;
        this.armorLevel = PlayerArmorManager.ArmorLevel.createFromString(level);
    }

    final private Item icon;
    final private String level;
    final private Cost cost;
    final private PlayerArmorManager.ArmorLevel armorLevel;

    public Cost getCostNoArgument() {
        return cost;
    }
    public Item getDisplayNoArgument() {
        return icon;
    }

    public String getLevel()
    {
        return level;
    }

    @Override
    public Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        return cost;
    }

    @Override
    public MutableText getName(BedwarsActive BedwarsGame, ServerPlayerEntity player) {
        return Text.translatable("shop.bedwars." + level + "Armor");
    }

    @Override
    public Item getDisplay(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        return icon;
    }

    @Override
    public BuyOffer canBeBough(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        if(!bedwarsGame.getInventoryManager().getArmorManager(player).getLevel().smallerThan(armorLevel))
            return new BuyOffer(false, Text.translatable("armor.bedwars.alreadyGotBetterArmor").setStyle(Style.EMPTY.withFormatting(Formatting.RED)));
        return new BuyOffer(true, null);
    }

    @Override
    public Map<Enchantment, Integer> enchantment(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var enchantment = bedwarsGame.getTeamComponentsFor(player).enchantments;
        Map<Enchantment, Integer> suitableEnchantment = new Object2IntArrayMap<>();
        for(var entry : enchantment.entrySet())
        {
            if(entry.getKey().target == EnchantmentTarget.ARMOR)
                suitableEnchantment.put(entry.getKey(), entry.getValue());
        }
        return suitableEnchantment;
    }

    @Override
    public List<MutableText> getLore(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        return null;
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        bedwarsGame.getInventoryManager().getArmorManager(player).setLevel(player, armorLevel);
        return ItemStack.EMPTY;
    }

    @Override
    public void editNbt(NbtCompound nbt, BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        nbt.putByte("HideFlags", (byte) 127); //hide specificity
    }
}
