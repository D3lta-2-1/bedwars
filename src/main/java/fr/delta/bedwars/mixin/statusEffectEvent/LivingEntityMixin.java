package fr.delta.bedwars.mixin.statusEffectEvent;

import fr.delta.bedwars.event.StatusEffectEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.stimuli.Stimuli;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "onStatusEffectApplied", at = @At("HEAD"))
    void onStatusEffectApplied(StatusEffectInstance effect, Entity source, CallbackInfo ci)
    {
        try(var invokers = Stimuli.select().forEntity((LivingEntity) (Object) this)) {
            invokers.get(StatusEffectEvent.ADD).onApplied((LivingEntity) (Object) this, effect, source);
        }
    }

    @Inject(method = "onStatusEffectRemoved", at = @At("HEAD"))
    void onStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci)
    {
        try(var invokers = Stimuli.select().forEntity((LivingEntity) (Object) this)) {
            invokers.get(StatusEffectEvent.REMOVE).onRemove((LivingEntity) (Object) this, effect);
        }
    }
}
