package fr.delta.bedwars.game.behaviour;

import fr.delta.bedwars.BedwarsConfig;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
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
    }


    public void addRegion(BlockBounds region)
    {
        this.claimedRegions.add(region);
    }

    public ActionResult blockBreakEvent(ServerPlayerEntity player, ServerWorld world, BlockPos pos)
    {
        var state = map.template().getBlockState(pos);
        if(state.isAir())
            return ActionResult.PASS;
        else
            for(var block : Constants.BreakableBlocks)
            {
                if(state.isOf(block))
                {
                    return ActionResult.PASS;
                }
            }
        player.sendMessage(Text.translatable("warning.bedwars.cannotBreakThisBlock"  ).setStyle(TextUtilities.WARNING));
        return ActionResult.FAIL;
    }

    public void onExplosionDetonated(Explosion explosion, boolean particles)
    {
        var affectedBlocks = new ObjectArrayList<>(explosion.getAffectedBlocks());
        explosion.getAffectedBlocks().clear();
        for(var block : affectedBlocks)
        {
            if(map.template().getBlockState(block).isAir())
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
}
