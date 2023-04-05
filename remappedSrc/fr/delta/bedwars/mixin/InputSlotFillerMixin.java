package fr.delta.bedwars.mixin;

import fr.delta.bedwars.event.RecipeClickedEvent;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.stimuli.EventInvokers;
import xyz.nucleoid.stimuli.Stimuli;

@Mixin(InputSlotFiller.class)
public class InputSlotFillerMixin {
    @Inject(method = "fillInputSlots(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/recipe/Recipe;Z)V", at = @At("HEAD"), cancellable = true)
    void FillInputSlots(ServerPlayerEntity entity, Recipe<?> recipe, boolean craftAll, CallbackInfo ci) {
        try(var invokers = Stimuli.select().forEntity(entity)) {
            var result = invokers.get(RecipeClickedEvent.EVENT).onClicked(entity, recipe, craftAll);
            if(result == ActionResult.FAIL) ci.cancel();
        }
    }
}
