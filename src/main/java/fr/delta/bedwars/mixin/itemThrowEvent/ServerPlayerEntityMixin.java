package fr.delta.bedwars.mixin.itemThrowEvent;

import fr.delta.bedwars.event.ItemThrowEvent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.nucleoid.stimuli.Stimuli;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Redirect(method = "dropSelectedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;"))
    private ItemEntity dropItem(ServerPlayerEntity player, ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
        var result = player.dropItem(stack, throwRandomly, retainOwnership);
        try (var invokers = Stimuli.select().forEntity(player)) {
           invokers.get(ItemThrowEvent.AFTER).afterThrowItem(player, stack);
        }
        return result;
    }
}
