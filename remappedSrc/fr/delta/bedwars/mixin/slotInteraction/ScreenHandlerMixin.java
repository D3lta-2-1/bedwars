package fr.delta.bedwars.mixin.slotInteractionEvent;

import fr.delta.bedwars.event.SlotInteractionEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.stimuli.EventInvokers;
import xyz.nucleoid.stimuli.Stimuli;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    @Inject(method = "internalOnSlotClick",at = @At("HEAD"), cancellable = true)
    void onSlotClickHead(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci)
    {
        if(!(player instanceof ServerPlayerEntity)) return;
        try (var invokers = Stimuli.select().forEntity(player)){
           var result= invokers.get(SlotInteractionEvent.BEFORE).onInteract((ServerPlayerEntity)player, (ScreenHandler)(Object)this, slotIndex, button, actionType);
           if(result == ActionResult.FAIL) ci.cancel();
        }
        //

    }

    @Inject(method = "internalOnSlotClick",at = @At("RETURN"), cancellable = true)
    void onSlotClickReturn(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci)
    {
        if(!(player instanceof ServerPlayerEntity)) return;
        try (var invokers = Stimuli.select().forEntity(player)){
            var result= invokers.get(SlotInteractionEvent.AFTER).onInteract((ServerPlayerEntity)player, (ScreenHandler)(Object)this, slotIndex, button, actionType);
            if(result == ActionResult.FAIL) ci.cancel();
        }
    }
}
