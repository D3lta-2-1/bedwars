package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;

public class PotionEntry extends ShopEntry
{
    static Codec<StatusEffectInstance> STATUS_EFFECT_INSTANCE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.STATUS_EFFECT.getCodec().fieldOf("effect").forGetter(StatusEffectInstance::getEffectType),
            Codec.FLOAT.fieldOf("duration").forGetter((effectInstance) -> effectInstance.getAmplifier() / 20.f),
            Codec.INT.fieldOf("amplifier").forGetter((effectInstance) -> effectInstance.getAmplifier() - 1),
            Codec.BOOL.optionalFieldOf("ambient", false).forGetter(StatusEffectInstance::isAmbient),
            Codec.BOOL.optionalFieldOf("showParticles", true).forGetter(StatusEffectInstance::shouldShowParticles),
            Codec.BOOL.optionalFieldOf("showIcon", true).forGetter(StatusEffectInstance::shouldShowIcon)
    ).apply(instance, (type, duration, amplifier, ambient, showParticles, showIcon) -> new StatusEffectInstance(type, Math.round(duration * 20), amplifier - 1, ambient, showParticles, showIcon)));


    public static Codec<PotionEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Cost.CODEC.fieldOf("cost").forGetter(PotionEntry::getCostNoArguments),
            STATUS_EFFECT_INSTANCE_CODEC.listOf().fieldOf("effects").forGetter(PotionEntry::getEffects)
    ).apply(instance, PotionEntry::new));

    public Cost getCostNoArguments() {
        return cost;
    }

    public List<StatusEffectInstance> getEffects() {
        return effects;
    }

    private final Cost cost;
    private final List<StatusEffectInstance> effects;
    private final ItemStack templatePotion;

    public PotionEntry(Cost cost, List<StatusEffectInstance> effects) {
        this.cost = cost;
        this.effects = effects;
        var stack = PotionUtil.setCustomPotionEffects(new ItemStack(Items.POTION), effects);
        stack.setCustomName(getName(null, null).setStyle(Style.EMPTY.withItalic(false)));
        stack.getNbt().putInt("CustomPotionColor", PotionUtil.getColor(effects));
        this.templatePotion = stack;
    }

    @Override
    public MutableText getName(BedwarsActive BedwarsGame, ServerPlayerEntity player) {
        var text = Text.empty();
        for(var effect : effects)
        {
            text.append(Text.translatable(effect.getTranslationKey()));
            text.append(Text.literal(" "));
        }
        text.append(Text.translatable("shop.bedwars.potion"));
        return text;
    }

    public Cost getCost(BedwarsActive BedwarsGame, ServerPlayerEntity player) { return cost; }

    @Override
    public Item getDisplay(BedwarsActive BedwarsGame, ServerPlayerEntity player) {
        return Items.POTION;
    }

    @Override
    public void editNbt(NbtCompound nbt) {
       nbt.copyFrom(templatePotion.getNbt());
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        return templatePotion.copy();
    }
}
