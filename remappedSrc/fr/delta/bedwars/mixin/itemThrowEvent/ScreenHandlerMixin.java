package fr.delta.bedwars.mixin.itemThrowEvent;

import fr.delta.bedwars.event.ItemThrowEvent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.nucleoid.stimuli.EventInvokers;
import xyz.nucleoid.stimuli.Stimuli;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @Redirect(method = "internalOnSlotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/ItemEntity;"))
    private ItemEntity dropItem1(PlayerEntity player, ItemStack stack, boolean retainOwnership)
    {
        var result = player.dropItem(stack, retainOwnership);
        if(player instanceof ServerPlayerEntity)
        {
            invokeEvent((ServerPlayerEntity)player, stack);
        }
        return result;
    }

    @Redirect(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/ItemEntity;"))
    private ItemEntity dropItem2(PlayerEntity player, ItemStack stack, boolean retainOwnership)
    {
        var result = player.dropItem(stack, retainOwnership);
        if(player instanceof ServerPlayerEntity)
        {
            invokeEvent((ServerPlayerEntity)player, stack);
        }
        return result;
    }

    private void invokeEvent(ServerPlayerEntity player, ItemStack stack) {
            try (var invokers = Stimuli.select().forEntity(player)) {
                invokers.get(ItemThrowEvent.AFTER).afterThrowItem(player, stack);
            }
    }
}
