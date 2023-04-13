package fr.delta.bedwars.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;

public class StatusEffectInstanceCodec {
    public static Codec<StatusEffectInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.STATUS_EFFECT.getCodec().fieldOf("effect").forGetter(StatusEffectInstance::getEffectType),
            Codec.FLOAT.fieldOf("duration").forGetter((effectInstance) -> effectInstance.getAmplifier() / 20.f),
            Codec.INT.optionalFieldOf("amplifier", 1).forGetter((effectInstance) -> effectInstance.getAmplifier() - 1),
            Codec.BOOL.optionalFieldOf("ambient", false).forGetter(StatusEffectInstance::isAmbient),
            Codec.BOOL.optionalFieldOf("showParticles", true).forGetter(StatusEffectInstance::shouldShowParticles),
            Codec.BOOL.optionalFieldOf("showIcon", true).forGetter(StatusEffectInstance::shouldShowIcon)
    ).apply(instance, (type, duration, amplifier, ambient, showParticles, showIcon) -> new StatusEffectInstance(type, Math.round(duration * 20), amplifier - 1, ambient, showParticles, showIcon)));
}
