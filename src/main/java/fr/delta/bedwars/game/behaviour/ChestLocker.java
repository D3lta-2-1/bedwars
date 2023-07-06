package fr.delta.bedwars.game.behaviour;

import fr.delta.bedwars.game.teamComponent.TeamComponents;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;

import java.util.Map;

public class ChestLocker {
    private final Map<GameTeamKey, TeamComponents> teamComponentsMap;
    private final TeamManager teamManager;

    public ChestLocker(Map<GameTeamKey, TeamComponents> teamComponentsMap, TeamManager teamManager, GameActivity activity)
    {
        this.teamComponentsMap = teamComponentsMap;
        this.teamManager = teamManager;
        activity.listen(BlockUseEvent.EVENT, this::onChestOpen);
    }

    public ActionResult onChestOpen(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult)
    {
        var pos = hitResult.getBlockPos();
        if(player.getWorld().getBlockState(hitResult.getBlockPos()).isOf(Blocks.CHEST))
        {
            for(var entry : teamComponentsMap.entrySet())
            {
                if(entry.getValue().effectPool.getBounds().contains(pos))
                {
                    if(teamManager.teamFor(player) == entry.getKey())
                    {
                        return ActionResult.PASS;
                    }
                    else if(entry.getValue().bed.isBroken() && teamManager.allPlayersIn(entry.getKey()).isEmpty())
                    {
                        return ActionResult.PASS;
                    }
                    else
                    {
                        player.playSound(SoundEvents.BLOCK_CHEST_LOCKED, 1.0F, 1.0F);
                        player.sendMessage(Text.translatable("waring.bedwars.chestLocked").formatted(Formatting.RED));
                        return ActionResult.FAIL;
                    }

                }
            }
        }
        return ActionResult.PASS;
    }
}
