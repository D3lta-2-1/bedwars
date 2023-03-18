package fr.delta.bedwars.mixin;

import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Explosion.class)
public class ExplosionMixin {

    private static final int knockbackMultiplier = 4;

    @ModifyArg(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private float overwriteDamage(float damage)
    {
        return damage/4;
    }

    @ModifyArgs(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;"))
    private void overwriteVelocity(Args args)
    {
        var x = (double)args.get(0);
        var y = (double)args.get(1);
        var z = (double)args.get(2);
        args.setAll(x * knockbackMultiplier, y * 2, z * knockbackMultiplier);
    }
}
