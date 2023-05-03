package fr.delta.bedwars.mixin.rules;

import fr.delta.bedwars.GameRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;

@Mixin(EnderPearlEntity.class)
public class EnderPearlEntityMixin {
	@Redirect(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private boolean applyEnderPearlDamageGameRule(Entity entity, DamageSource source, float amount) {
		if (source == entity.getDamageSources().fall()) {
			ManagedGameSpace gameSpace = GameSpaceManager.get().byWorld(entity.world);
			if (gameSpace != null && gameSpace.getBehavior().testRule(GameRules.ENDER_PEARL_DAMAGE) == ActionResult.FAIL) {
				return false;
			}
		}
		return entity.damage(source, amount);
	}
}
