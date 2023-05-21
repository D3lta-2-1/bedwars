package fr.delta.bedwars.game.behaviour;

import fr.delta.bedwars.codec.BedwarsConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import fr.delta.bedwars.Constants;
import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.game.map.BedwarsMap;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.block.FluidPlaceEvent;
import xyz.nucleoid.stimuli.event.world.ExplosionDetonatedEvent;

import java.util.ArrayList;
import java.util.List;

//this claim is based on the Template map to know what block can be break, modifying it is basically change claimed blocks
public class ClaimManager {
    List<BlockBounds> claimedRegions;
    final BedwarsMap map;
    final BedwarsConfig config;
    public ClaimManager(BedwarsMap map, BedwarsConfig config, GameActivity activity)
    {
        this.claimedRegions = new ArrayList<>();
        this.map = map;
        this.config = config;
        activity.listen(BlockBreakEvent.EVENT, this::blockBreakEvent);
        activity.listen(ExplosionDetonatedEvent.EVENT, this::onExplosionDetonated);
        activity.listen(BlockPlaceEvent.BEFORE, this::onPlace);
        activity.listen(FluidPlaceEvent.EVENT, this::onBucketUse);
    }


    public void addRegion(BlockBounds region)
    {
        this.claimedRegions.add(region);
    }

    public ActionResult blockBreakEvent(ServerPlayerEntity player, ServerWorld world, BlockPos pos)
    {
        var state = map.template().getBlockState(pos);
        if(isAirOrBreakableBlock(state))
            return ActionResult.PASS;
        player.sendMessage(Text.translatable("warning.bedwars.cannotBreakThisBlock"  ).setStyle(TextUtilities.WARNING));
        return ActionResult.FAIL;
    }

    public static boolean isAirOrBreakableBlock(BlockState state)
    {
        if(state.isAir())
            return true;
        else
            for(var block : Constants.BreakableBlocks)
            {
                if(state.isOf(block))
                {
                    return true;
                }
            }
        return false;
    }

    public void onExplosionDetonated(Explosion explosion, boolean particles)
    {
        var affectedBlocks = new ObjectArrayList<>(explosion.getAffectedBlocks());
        explosion.getAffectedBlocks().clear();
        for(var block : affectedBlocks)
        {
            if(isAirOrBreakableBlock(map.template().getBlockState(block)))
            {
                explosion.getAffectedBlocks().add(block);
            }
        }
    }

    public ActionResult onPlace(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemUsageContext context)
    {
        if(pos.getY() > config.highLimit())
        {
            player.sendMessage(Text.translatable("warning.bedwars.buildLimit").setStyle(TextUtilities.WARNING));
            return ActionResult.FAIL;
        }
        if(pos.getY() < config.downLimit())
        {
            player.sendMessage(Text.translatable("warning.bedwars.buildLimit").setStyle(TextUtilities.WARNING));
            return ActionResult.FAIL;
        }
        for(var region : claimedRegions)
        {
            if(region.contains(pos))
            {
                player.sendMessage(Text.translatable("warning.bedwars.cannotPlaceBlockHere").setStyle(TextUtilities.WARNING));
                return ActionResult.FAIL;
            }
        }
        if(!map.template().getBounds().contains(pos))
        {
            player.sendMessage(Text.translatable("warning.bedwars.buildLimit").setStyle(TextUtilities.WARNING));
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    public boolean isInMapLimits(BlockPos pos)
    {
        return pos.getY() < config.highLimit() && pos.getY() > config.downLimit() && map.template().getBounds().contains(pos);
    }

    public boolean isInClaimedRegion(BlockPos pos)
    {
        for(var region : claimedRegions)
        {
            if(region.contains(pos))
            {
                return true;
            }
        }
        return false;
    }

    //todo : make it work
    private ActionResult onBucketUse(ServerWorld world, BlockPos pos, @Nullable ServerPlayerEntity player, @Nullable BlockHitResult result)
    {
        for(var region : claimedRegions)
        {
            if(region.contains(pos))
            {
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }
}
