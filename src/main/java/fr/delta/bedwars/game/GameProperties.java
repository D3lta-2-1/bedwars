package fr.delta.bedwars.game;

import fr.delta.bedwars.event.PotionDrankEvent;
import fr.delta.bedwars.event.SlotInteractionEvent;
import net.minecraft.block.Blocks;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.world.event.GameEvent;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;

public class GameProperties {
    public static void add(GameActivity activity) {
        //avoid empty bottles
        activity.listen(PotionDrankEvent.EVENT, (stack, world, user) -> ItemStack.EMPTY);

        //avoid crafting
        activity.listen(SlotInteractionEvent.BEFORE, (player, handler, slotIndex, button, actionType)-> {
            var screenHandler = handler instanceof AbstractRecipeScreenHandler ? (AbstractRecipeScreenHandler<?>)handler : null;
            if(screenHandler == null) return ActionResult.PASS;
            if(slotIndex >= screenHandler.getCraftingResultSlotIndex() && slotIndex <= screenHandler.getCraftingSlotCount()) return ActionResult.FAIL;
            return ActionResult.PASS;
        });

        //auto ignite tnt
        activity.listen(BlockPlaceEvent.AFTER, (igniter, world, pos, state) -> {
            if (state.getBlock() == Blocks.TNT) {
                TntEntity tntEntity = new TntEntity(world, (double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, igniter);
                tntEntity.setFuse(60);
                tntEntity.setYaw(igniter.getYaw());
                world.spawnEntity(tntEntity);
                world.playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent(igniter, GameEvent.PRIME_FUSE, pos);
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        });
    }
}
