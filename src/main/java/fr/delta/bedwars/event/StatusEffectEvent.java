package fr.delta.bedwars.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public class StatusEffectEvent {
    public static StimulusEvent<Add> ADD = StimulusEvent.create(Add.class, ctx -> (entity, effect, source) -> {
        try{
            for(var listener : ctx.getListeners())
                listener.onApplied(entity, effect, source);
        } catch (Throwable t) {
            ctx.handleException(t);
        }
    });

    public static StimulusEvent<Remove> REMOVE = StimulusEvent.create(Remove.class, ctx -> (entity, effect) -> {
        try{
            for(var listener : ctx.getListeners())
                listener.onRemove(entity, effect);
        } catch (Throwable t) {
            ctx.handleException(t);
        }
    });

    public interface Add
    {
        void onApplied(LivingEntity entity, StatusEffectInstance effect, @Nullable Entity source);
    }

    public interface Remove
    {
        void onRemove(LivingEntity entity,StatusEffectInstance effect);
    }
}
