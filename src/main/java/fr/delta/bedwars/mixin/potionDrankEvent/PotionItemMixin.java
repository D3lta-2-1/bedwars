package fr.delta.bedwars.mixin.potionDrankEvent;


import fr.delta.bedwars.event.PotionDrankEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.stimuli.Stimuli;

@Mixin(PotionItem.class)
public class PotionItemMixin {

    @Inject(at = @At("RETURN"), method = "finishUsing", cancellable = true)
    void finishUsing(ItemStack ignoredStack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir)
    {
        ItemStack stack = cir.getReturnValue();
        try (var invokers = Stimuli.select().forEntity(user)) {
           stack = invokers.get(PotionDrankEvent.EVENT).onPotionDrank(stack, world, user);
        }
        cir.setReturnValue(stack);
    }
}
