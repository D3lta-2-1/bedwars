package me.verya.bedwars.game.component;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.verya.bedwars.game.map.BedwarsMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.world.ExplosionDetonatedEvent;

public class Bed {
    private final BlockBounds bounds;
    private boolean isBroken;
    private final GameTeam owner;
    private final TeamManager teamManager;

    public Bed(BlockBounds bounds, BedwarsMap map, GameTeam owner, TeamManager teamManager, GameActivity activity)
    {
        this.bounds = bounds;
        this.isBroken = false;
        this.owner = owner;
        this.teamManager = teamManager;
        //replace all "bed blocks", this could be any block by air block in the template, like this the claim manager would allow them to be broken
        for(var blocks : bounds)
        {
            map.template().setBlockState(blocks, Blocks.AIR.getDefaultState());
        }
        activity.listen(BlockBreakEvent.EVENT, this::blockBreakEvent);
        activity.listen(ExplosionDetonatedEvent.EVENT, this::onExplosionDetonated);
    }

    public void breakIt(ServerWorld world)
    {
        this.isBroken = true;
        for(var blocks : bounds)
        {
            world.setBlockState(blocks, Blocks.AIR.getDefaultState(), Block.FORCE_STATE | Block.SKIP_DROPS);
        }
    }

    public boolean isBroken()
    {
        return isBroken;
    }
    public ActionResult blockBreakEvent(ServerPlayerEntity player, ServerWorld world, BlockPos pos)
    {
        if(isBroken) return ActionResult.PASS;
        if(!bounds.contains(pos)) return ActionResult.PASS;
        if(teamManager.teamFor(player) == owner.key())
        {
            player.sendMessage(Text.translatable("warning.bedwars.cannotBreakOwnBed").setStyle(Style.EMPTY.withColor(Formatting.RED)));
            return ActionResult.FAIL;
        }
        this.breakIt(world);
        return ActionResult.FAIL;
    }

    public void onExplosionDetonated(Explosion explosion, boolean particles)
    {
        var affectedBlocks = new ObjectArrayList<>(explosion.getAffectedBlocks());
        explosion.getAffectedBlocks().clear();
        for(var pos : affectedBlocks)
        {
            if(!bounds.contains(pos))
            {
                explosion.getAffectedBlocks().add(pos);
            }
        }
    }
}
