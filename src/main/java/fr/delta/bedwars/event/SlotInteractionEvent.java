package fr.delta.bedwars.event;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface SlotInteractionEvent {

    StimulusEvent<SlotInteractionEvent> BEFORE = StimulusEvent.create(SlotInteractionEvent.class, ctx -> (player, handler, slotIndex, button, actionType) -> {
        try{
            for(var listener : ctx.getListeners())
            {
                var result = listener.onInteract(player, handler, slotIndex, button, actionType);
                if(result == ActionResult.SUCCESS || result == ActionResult.FAIL) return result;
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
        return ActionResult.SUCCESS;
    });

    StimulusEvent<SlotInteractionEvent> AFTER = StimulusEvent.create(SlotInteractionEvent.class, ctx -> (player, handler, slotIndex, button, actionType) -> {
        try{
            for(var listener : ctx.getListeners())
            {
                var result = listener.onInteract(player, handler, slotIndex, button, actionType);
                if(result == ActionResult.SUCCESS || result == ActionResult.FAIL) return result;
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
        return ActionResult.SUCCESS;
    });

    ActionResult onInteract(ServerPlayerEntity player, ScreenHandler handler, int slotIndex, int button, SlotActionType actionType);
}
