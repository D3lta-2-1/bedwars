package fr.delta.bedwars.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface PotionDrankEvent {

    StimulusEvent<PotionDrankEvent> EVENT = StimulusEvent.create(PotionDrankEvent.class, ctx -> (stack, world, user) -> {
        try{
            for(var listener : ctx.getListeners())
            {
                stack = listener.onPotionDrank(stack, world, user);
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
        return stack;
    });

    ItemStack onPotionDrank(ItemStack stack, World world, LivingEntity user);

}
