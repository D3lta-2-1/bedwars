package fr.delta.bedwars.game.behavior;

import fr.delta.bedwars.game.event.BedwarsEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;

import java.util.List;

public class WinEventSender {
    final private List<GameTeam> teamInOrder;
    final private TeamManager teamManager;
    final private GameActivity activity;
    public WinEventSender(List<GameTeam> teamInOrder, TeamManager teamManager, GameActivity activity)
    {
        this.teamManager = teamManager;
        this.teamInOrder = teamInOrder;
        this.activity = activity;
        activity.listen(BedwarsEvents.AFTER_PLAYER_DEATH, this::onPlayerDeath);
    }

    void onPlayerDeath(ServerPlayerEntity player, DamageSource source, ServerPlayerEntity killer, boolean isFinal)
    {
        if(isFinal)
        {
            GameTeam lastTeam = null;
            for(var team : teamInOrder)
            {
                if(teamManager.playersIn(team.key()).size() !=0)
                {
                    if(lastTeam == null)
                        lastTeam = team;
                    else
                    {
                        return;
                    }
                }
            }
            activity.invoker(BedwarsEvents.TEAM_WIN).onTeamWin(lastTeam);
        }
    }
}
