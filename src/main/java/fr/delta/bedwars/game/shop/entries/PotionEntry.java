package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.TextUtilities;
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
            Codec.INT.optionalFieldOf("amplifier", 1).forGetter((effectInstance) -> effectInstance.getAmplifier() - 1),
            Codec.BOOL.optionalFieldOf("ambient", false).forGetter(StatusEffectInstance::isAmbient),
            Codec.BOOL.optionalFieldOf("showParticles", true).forGetter(StatusEffectInstance::shouldShowParticles),
            Codec.BOOL.optionalFieldOf("showIcon", true).forGetter(StatusEffectInstance::shouldShowIcon)
    ).apply(instance, (type, duration, amplifier, ambient, showParticles, showIcon) -> new StatusEffectInstance(type, Math.round(duration * 20), amplifier - 1, ambient, showParticles, showIcon)));

    enum PotionType
    {
        normal,
        splash,
        lingering,
        arrow;

        public Item asItem()
        {
            return switch (this)
                    {
                        case normal -> Items.POTION;
                        case splash -> Items.SPLASH_POTION;
                        case lingering -> Items.LINGERING_POTION;
                        case arrow -> Items.TIPPED_ARROW;
                    };
        }
        public static Codec<PotionType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("type").forGetter(PotionType::name)
        ).apply(instance, PotionType::valueOf));
    }

    public static Codec<PotionEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Cost.CODEC.fieldOf("cost").forGetter(PotionEntry::getCostNoArguments),
            STATUS_EFFECT_INSTANCE_CODEC.listOf().fieldOf("effects").forGetter(PotionEntry::getEffects),
            PotionType.CODEC.optionalFieldOf("type", PotionType.normal).forGetter(PotionEntry::getPotionType),
            Codec.INT.optionalFieldOf("count", 1).forGetter(PotionEntry::count)
    ).apply(instance, PotionEntry::new));

    public Cost getCostNoArguments() {
        return cost;
    }

    public List<StatusEffectInstance> getEffects() {
        return effects;
    }

    public PotionType getPotionType() {
        return potionType;
    }
    public int count() {
        return count;
    }

    private final Cost cost;
    private final List<StatusEffectInstance> effects;
    private final ItemStack templatePotion;
    private final PotionType potionType;
    private final int count;

    public PotionEntry(Cost cost, List<StatusEffectInstance> effects, PotionType potionType, int count) {
        this.cost = cost;
        this.effects = effects;
        this.potionType = potionType;
        this.count = count;
        var stack = PotionUtil.setCustomPotionEffects(new ItemStack(potionType.asItem()), effects);
        stack.setCustomName(getName(null, null).setStyle(Style.EMPTY.withItalic(false)));
        assert stack.getNbt() != null;
        stack.getNbt().putInt("CustomPotionColor", PotionUtil.getColor(effects));
        this.templatePotion = stack;
    }

    @Override
    public MutableText getName(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        var text = Text.empty();
        for(var effect : effects)
        {
            text.append(Text.translatable(effect.getTranslationKey()));
            text.append(TextUtilities.SPACE);
        }
        text.append(Text.translatable("shop.bedwars.potion." + potionType.name()));
        return text;
    }

    public Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player) { return cost; }

    @Override
    public Item getDisplay(BedwarsActive bedwarsGame, ServerPlayerEntity player) {
        return Items.POTION;
    }

    @Override
    public int getCount() {
        return count;
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
