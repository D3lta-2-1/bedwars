package fr.delta.bedwars.custom.blocks;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class Launchpad extends Block implements BlockEntityProvider, PolymerBlock {
    private final Block virtualBlock;

    public Launchpad(Settings settings, Block virtualBlock) {
        super(settings);
        this.virtualBlock = virtualBlock;
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return this.virtualBlock;
    }

    @Override
    @Deprecated
    public void onEntityCollision(BlockState state,
                                  World world,
                                  BlockPos pos,
                                  Entity entity){
        var blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof LaunchPadBlockEntity launchPad) {
            tryLaunch(entity, entity, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, launchPad.getPitch(), launchPad.getPower());
        }

        super.onEntityCollision(state, world, pos, entity);
    }

    public static boolean tryLaunch(Entity entity, Entity source, SoundEvent sound, SoundCategory category, float pitch, float power) {
        if (entity.isOnGround() && !(entity instanceof ArmorStandEntity)) {
            entity.setVelocity(getVector(pitch, source.getYaw(0)).multiply(power));
            if (entity instanceof ServerPlayerEntity player) {
                player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(entity));
                playLaunchSound(player, sound, category);
            }
            if (source != entity && source instanceof ServerPlayerEntity player) {
                playLaunchSound(player, sound, category);
            }

            return true;
        }

        return false;
    }

    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        return hasTopRim(world, blockPos) || sideCoversSmallSquare(world, blockPos, Direction.UP);
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.DESTROY;
    }

    public static void playLaunchSound(ServerPlayerEntity player, SoundEvent sound, SoundCategory category) {
        player.playSound(sound, category, 0.5f, 1);
    }

    private static Vec3d getVector(float pitch, float yaw) {
        double pitchRad = Math.toRadians(pitch);
        double yawRad = Math.toRadians(yaw);

        double horizontal = -Math.cos(pitchRad);
        return new Vec3d(
                Math.sin(yawRad) * horizontal,
                Math.sin(pitchRad),
                -Math.cos(yawRad) * horizontal
        );
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LaunchPadBlockEntity(pos, state);
    }
}