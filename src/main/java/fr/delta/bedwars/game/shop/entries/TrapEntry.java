package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.codec.StatusEffectInstanceCodec;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.traps.Trap;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.ArrayList;
import java.util.List;

public class TrapEntry extends ShopEntry{

    public static final Codec<TrapEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ITEM.getCodec().fieldOf("icon").forGetter(TrapEntry::getIcon),
            Cost.CODEC.fieldOf("cost").forGetter(TrapEntry::getCostNoArgument),
            Codec.STRING.fieldOf("translation_key").forGetter(TrapEntry::getTranslationKey),
            Codec.INT.fieldOf("cooldown").forGetter(TrapEntry::getCooldown),
            Codec.INT.optionalFieldOf("alarm_duration", 0).forGetter(TrapEntry::getPlayAlarmDuration),
            StatusEffectInstanceCodec.CODEC.listOf().optionalFieldOf("effects_for_trigger", List.of()).forGetter(TrapEntry::getEffectsForTrigger),
            StatusEffectInstanceCodec.CODEC.listOf().optionalFieldOf("effects_for_owner", List.of()).forGetter(TrapEntry::getEffectsForOwner),
            Registries.STATUS_EFFECT.getCodec().listOf().optionalFieldOf("effect_to_remove_for_trigger", List.of()).forGetter(TrapEntry::getEffectToRemoveForTrigger)
    ).apply(instance, TrapEntry::new));
    private final Item icon;
    private final Cost cost;
    private final String translationKey;
    private final int cooldown;
    private final int playAlarmDuration;
    private final List<StatusEffectInstance> effectsForTrigger;
    private final List<StatusEffectInstance> effectsForOwner;
    private final List<StatusEffect> effectToRemoveForTrigger;

    public TrapEntry(Item icon, Cost cost, String translationKey, int cooldown, int playAlarmDuration, List<StatusEffectInstance> effectsForTrigger, List<StatusEffectInstance> effectsForOwner, List<StatusEffect> effectToRemoveForTrigger)
    {
        this.icon = icon;
        this.cost = cost;
        this.cooldown = cooldown;
        this.translationKey = translationKey;
        this.playAlarmDuration = playAlarmDuration;
        this.effectsForTrigger = effectsForTrigger;
        this.effectsForOwner = effectsForOwner;
        this.effectToRemoveForTrigger = effectToRemoveForTrigger;
    }

    public Item getIcon()
    {
        return icon;
    }

    public Cost getCostNoArgument()
    {
        return cost;
    }

    public String getTranslationKey()
    {
        return translationKey;
    }

    public int getCooldown()
    {
        return cooldown;
    }

    public int getPlayAlarmDuration() {
        return playAlarmDuration;
    }

    public List<StatusEffectInstance> getEffectsForTrigger() {
        return effectsForTrigger;
    }

    public List<StatusEffectInstance> getEffectsForOwner() {
        return effectsForOwner;
    }

    public List<StatusEffect> getEffectToRemoveForTrigger() {
        return effectToRemoveForTrigger;
    }

    @Override
    public Item getDisplay(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return icon;
    }

    @Override
    public MutableText getName(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        return translationKey.isEmpty() ? super.getName(bedwarsGame, player) : Text.translatable(translationKey);
    }

    @Override
    public Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        var trapHandler = bedwarsGame.getTeamComponentsFor(player).trapHandler;
        if(trapHandler.isTrapQueueFull()) return null;
        return new Cost( cost.item(), cost.count() + trapHandler.getTrapCount());
    }

    @Override
    public BuyOffer canBeBough(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        var trapHandler = bedwarsGame.getTeamComponentsFor(player).trapHandler;
        return new BuyOffer(!trapHandler.isTrapQueueFull(), Text.translatable("shop.bedwars.trapQueueFull").formatted(Formatting.RED));
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        var trapHandler = bedwarsGame.getTeamComponentsFor(player).trapHandler;
        trapHandler.addTrap(new Trap(icon, Text.translatable(translationKey), cooldown, playAlarmDuration, copyList(effectsForTrigger),  copyList(effectsForOwner), effectToRemoveForTrigger));
        return ItemStack.EMPTY;
    }

    @Override
    public void editNbt(NbtCompound nbt) {
        nbt.putByte("HideFlags", (byte) 127); //hide specificity
    }

    private List<StatusEffectInstance> copyList(List<StatusEffectInstance> list) {
        var newList = new ArrayList<StatusEffectInstance>();
        for (StatusEffectInstance statusEffectInstance : list)
            newList.add(new StatusEffectInstance(statusEffectInstance));
        return newList;
    }

}
