package me.verya.bedwars.mixin.old_knocback;

import me.verya.bedwars.Bedwars;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin{
    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V"))
    public void handleTakeKnockback(LivingEntity livingEntity, double speed, double xMovement, double zMovement)
    {
        var playerEntity = (PlayerEntity)(Object)this;
        var gameSpace = GameSpaceManager.get().byWorld(playerEntity.world);

        if (gameSpace != null && gameSpace.getBehavior().testRule(Bedwars.OLD_KNOCKBACK) == ActionResult.SUCCESS) {
            speed = (float) ((double)speed * (1.0D - livingEntity.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).getValue()));
            livingEntity.addVelocity(-(xMovement * speed), 0.1D, -(zMovement * speed));
        }
        else
        {
            livingEntity.takeKnockback(speed, xMovement, zMovement);
        }

    }
}
