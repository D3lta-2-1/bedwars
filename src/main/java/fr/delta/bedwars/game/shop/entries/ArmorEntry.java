package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.player.PlayerArmorManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;

public class ArmorEntry extends ShopEntry {

    public static Codec<ArmorEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ITEM.getCodec().fieldOf("icon").forGetter(ArmorEntry::getDisplay),
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

    public String getLevel()
    {
        return level;
    }

    @Override
    public Cost getCost(BedwarsActive BedwarsGame, ServerPlayerEntity player) {
        return cost;
    }

    @Override
    public MutableText getName() {
        return Text.translatable("shop.bedwars." + level + "Armor");
    }

    @Override
    public Item getDisplay() {
        return icon;
    }

    @Override
    public BuyOffer canBeBough(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        if(!bedwarsGame.getInventoryManager().getArmorManager(player).getLevel().smallerThan(armorLevel))
            return new BuyOffer(false, Text.translatable("armor.bedwars.alreadyGotBetterArmor").setStyle(Style.EMPTY.withFormatting(Formatting.RED)));
        return new BuyOffer(true, null);
    }

    @Override
    public List<MutableText> getLore(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        return null;
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        bedwarsGame.getInventoryManager().getArmorManager(player).setLevel(armorLevel);
        return ItemStack.EMPTY;
    }

}
