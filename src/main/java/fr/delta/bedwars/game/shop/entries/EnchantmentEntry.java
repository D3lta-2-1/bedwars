package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class EnchantmentEntry extends ShopEntry{

    public static Codec<EnchantmentEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ENCHANTMENT.getCodec().fieldOf("enchantment").forGetter(EnchantmentEntry::getEnchantment),
            Registries.ITEM.getCodec().fieldOf("icon").forGetter(EnchantmentEntry::getIcon),
            Cost.CODEC.listOf().fieldOf("costs").forGetter(EnchantmentEntry::getCosts)
    ).apply(instance, EnchantmentEntry::new));
    private final Enchantment enchantment;
    private final Item icon;
    private final List<Cost> costs;

    EnchantmentEntry(Enchantment enchantment,Item icon, List<Cost> costs) {
        this.enchantment = enchantment;
        this.icon = icon;
        this.costs = costs;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public Item getIcon() {
        return icon;
    }

    public List<Cost> getCosts() {
        return costs;
    }

    @Override
    public MutableText getName(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var level = bedwarsGame.getTeamComponentsFor(player).enchantments.getOrDefault(enchantment,0);
        if(level + 1 >= this.costs.size()) //a List begins at 0, but 0 is the first upgrade
            return TextUtilities.concatenate(Text.translatable(enchantment.getTranslationKey()),
                    TextUtilities.SPACE,
                    Text.translatable("generator.bedwars." + level));
        else
            return TextUtilities.concatenate(Text.translatable(enchantment.getTranslationKey()),
                    TextUtilities.SPACE,
                    Text.translatable("generator.bedwars." + (level + 1)));
    }

    @Override
    public Item getDisplay(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        return icon;
    }

    @Override
    public int displayCount(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var level = bedwarsGame.getTeamComponentsFor(player).enchantments.getOrDefault(enchantment,0);
        return  level < this.costs.size() ? level + 1 : level;
    }

    @Override
    public Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var level = bedwarsGame.getTeamComponentsFor(player).enchantments.getOrDefault(enchantment,0);
        return level < this.costs.size() ? costs.get(level) : null;
    }

    @Override
    public BuyOffer canBeBough(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var level = bedwarsGame.getTeamComponentsFor(player).enchantments.getOrDefault(enchantment,0);
        return new BuyOffer(level < this.costs.size(), Text.translatable("shop.bedwars.upgradeMaxed").formatted(Formatting.RED));
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity buyer) {
        var enchantments = bedwarsGame.getTeamComponentsFor(buyer).enchantments;
        var level = enchantments.getOrDefault(enchantment,0);
        enchantments.put(enchantment, level + 1);

        bedwarsGame.getPlayersInTeam(bedwarsGame.getTeamForPlayer(buyer)).forEach(player -> {
            bedwarsGame.getInventoryManager().getArmorManager(player).updateArmor(player);
            bedwarsGame.getDefaultSwordManager().updatePlayer(player, null);
        });
        return ItemStack.EMPTY;
    }

    @Override
    public void editNbt(NbtCompound nbt) {
        nbt.putByte("HideFlags", (byte) 127); //hide specificity
    }

    @Override
    public boolean shouldNotifyAllTeam() {
        return true;
    }
}
