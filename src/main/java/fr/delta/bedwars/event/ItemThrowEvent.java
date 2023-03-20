package fr.delta.bedwars.event;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface ItemThrowEvent {
     StimulusEvent<ItemThrowEvent> AFTER = StimulusEvent.create(ItemThrowEvent.class, ctx -> (player, stack) -> {
        try {
            for (var listener : ctx.getListeners())
            {
                listener.afterThrowItem(player, stack);
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
    });

    void afterThrowItem(ServerPlayerEntity player, ItemStack stack);
}
