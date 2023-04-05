package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class EffectPoolEntry extends ShopEntry{

    public static Codec<EffectPoolEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ITEM.getCodec().fieldOf("icon").forGetter(EffectPoolEntry::getIcon),
            Registries.STATUS_EFFECT.getCodec().fieldOf("effect").forGetter(EffectPoolEntry::getEffect),
            Cost.CODEC.listOf().fieldOf("costs").forGetter(EffectPoolEntry::getAvailableTiers)
    ).apply(instance, EffectPoolEntry::new));
    private final Item icon;
    private final StatusEffect effect;
    private final List<Cost> availableTiers;

    Item getIcon() {
        return icon;
    }

    StatusEffect getEffect() {
        return effect;
    }

    List<Cost> getAvailableTiers() {
        return availableTiers;
    }

    EffectPoolEntry(Item icon, StatusEffect effect, List<Cost> availableTiers) {
        this.icon = icon;
        this.effect = effect;
        this.availableTiers = availableTiers;
    }

    @Override
    public Item getDisplay(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        return icon;
    }

    @Override
    public Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var handler = bedwarsGame.getTeamComponentsFor(player).effectPool;
        var level = handler.getAmplifier(effect);
        if(level + 1 >= availableTiers.size())
            return null;
        return availableTiers.get(level + 1);
    }

    @Override
    public int displayCount(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var handler = bedwarsGame.getTeamComponentsFor(player).effectPool;
        var level = handler.getAmplifier(effect);
        if(level + 1 >= availableTiers.size())
            return level + 1;
        return level + 2;
    }

    @Override
    public MutableText getName(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var handler = bedwarsGame.getTeamComponentsFor(player).effectPool;
        var level = handler.getAmplifier(effect);
        if(level + 1 >= availableTiers.size())
        {
            return TextUtilities.concatenate(Text.translatable(effect.getTranslationKey()),
                    TextUtilities.SPACE,
                    Text.translatable("shop.bedwars.poolUpgrade"),
                    TextUtilities.SPACE,
                    Text.translatable("generator.bedwars." + (level + 1)));
        }
        return TextUtilities.concatenate(Text.translatable(effect.getTranslationKey()),
                TextUtilities.SPACE,
                Text.translatable("generator.bedwars." + (level + 2))); //level + 2 because level -1 is level 0, and we want to display level 1 as the next level available
    }

    @Override
    public BuyOffer canBeBough(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var handler = bedwarsGame.getTeamComponentsFor(player).effectPool;
        var level = handler.getAmplifier(effect);
        return new BuyOffer(level + 1 < availableTiers.size(), Text.translatable("shop.bedwars.upgradeMaxed").formatted(Formatting.RED));
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var handler = bedwarsGame.getTeamComponentsFor(player).effectPool;
        var level = handler.getAmplifier(effect);
        handler.setEffect(effect, level + 1);
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
