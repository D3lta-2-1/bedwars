package fr.delta.bedwars.mixin.rules;

import fr.delta.bedwars.GameRules;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

@Mixin(BedBlock.class)
public class BedBlockMixin {
    @Inject(method = "onUse",  at = @At("HEAD"), cancellable = true)
    public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> ci)
    {
        var gameSpace = GameSpaceManager.get().byWorld(world);

        if (gameSpace != null && gameSpace.getBehavior().testRule(GameRules.BED_INTERACTION) == ActionResult.FAIL) {
            ci.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
