package fr.delta.bedwars.mixin.rules;

import fr.delta.bedwars.GameRules;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

@Mixin(FireBlock.class)
public class FireBlockMixin {
    @Inject(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;trySpreadingFire(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/util/math/random/Random;I)V", ordinal = 0, shift = At.Shift.BEFORE), cancellable = true)
    void onFireSpread(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci)
    {
        var gameSpace = GameSpaceManager.get().byWorld(world);
        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRules.FIRE_SPREAD) == ActionResult.FAIL)
            ci.cancel();
    }
}
