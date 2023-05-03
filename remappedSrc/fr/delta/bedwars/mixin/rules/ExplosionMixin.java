package fr.delta.bedwars.mixin.rules;

import fr.delta.bedwars.GameRules;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow @Final
    private World world;
    private static final int knockbackMultiplier = 2;
    private static final float damageMultiplier = 0.25F;

    @ModifyArg(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private float overwriteDamage(float damage)
    {
        var gameSpace = GameSpaceManager.get().byWorld(world);
        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRules.REDUCED_EXPLOSION_DAMAGE) != ActionResult.SUCCESS) return damage;
        return damage * damageMultiplier;
    }

    @ModifyArg(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d overwriteVelocity(Vec3d vec3d)
    {
        var gameSpace = GameSpaceManager.get().byWorld(world);
        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRules.AMPLIFIED_EXPLOSION_KNOCKBACK) != ActionResult.SUCCESS) return vec3d;
        return vec3d.multiply(knockbackMultiplier);
    }
}
