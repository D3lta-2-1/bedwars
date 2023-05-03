package fr.delta.bedwars.mixin.rules;

import fr.delta.bedwars.GameRules;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

@Mixin(net.minecraft.entity.LivingEntity.class)
public class LivingEntityMixin
{
    @ModifyVariable(method = "damage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    float damage(float amount, DamageSource source)
    {
        var gameSpace = GameSpaceManager.get().byWorld(((LivingEntity)(Object)this).getWorld());
        if(source.isOf(DamageTypes.FALL) && gameSpace != null && gameSpace.getBehavior().testRule(GameRules.REDUCED_FALL_DAMAGE) == ActionResult.SUCCESS)
        {
            if(amount > 0)
                amount = Math.min(0, amount - 1); //reduce fall damage by 1
        }
        return amount;
    }
}
