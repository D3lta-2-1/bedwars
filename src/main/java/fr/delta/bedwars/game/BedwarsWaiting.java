package fr.delta.bedwars.game;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import fr.delta.bedwars.BedwarsConfig;
import fr.delta.bedwars.game.map.BedwarsMap;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;

import java.util.*;


public class BedwarsWaiting {
    private final BedwarsMap map;
    private final ServerWorld world;
    private final GameActivity activity;
    private final GameSpace gameSpace;

    private final BedwarsConfig config;
    int teamSize;

    BedwarsWaiting(ServerWorld world, BedwarsMap map, GameActivity activity, BedwarsConfig config)
    {
        this.world = world;
        this.map = map;
        this.activity = activity;
        this.teamSize = config.teamSize();
        this.gameSpace = activity.getGameSpace();
        this.config = config;
        this.registerEvents();

        activity.listen(GameActivityEvents.REQUEST_START, this::requestStart);
    }
    static public GameOpenProcedure open(GameOpenContext<BedwarsConfig> context)
    {
        var config = context.config();
        var map = BedwarsMap.loadMap(config, context.server() );
        var worldConfig = map.asRuntimeWorldConfig();

        return context.openWithWorld(worldConfig, (activity, world) ->
            new BedwarsWaiting(world, map, activity, config));
    }
    private void registerEvents()
    {
        var maxPlayerCount = this.map.teamData().size() * teamSize;
        GameWaitingLobby.addTo(activity,new PlayerConfig(1, maxPlayerCount, maxPlayerCount - 1, PlayerConfig.Countdown.DEFAULT));
        activity.listen(GamePlayerEvents.OFFER, offer -> offer.accept(world, map.waiting().centerTop()));
    }

    private GameResult requestStart() {
        List<GameTeam> teams = new ArrayList<>();
        for (var mapTeamData : map.teamData()) {
            GameTeam team = new GameTeam(
                    new GameTeamKey("bedwars_team_" + mapTeamData.color.name().toLowerCase()),
                    GameTeamConfig.builder()
                            .setCollision(AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS)
                            .setFriendlyFire(true)
                            .setNameTagVisibility(AbstractTeam.VisibilityRule.ALWAYS)
                            .setColors(GameTeamConfig.Colors.from(mapTeamData.color))
                            .build()
            );
            teams.add(team);
        }

        //to a scoreboard ordered
        var teamsInOrder = new ArrayList<>(teams);
        //make teams... without TeamAllocator because it auto delete empty teams and I prefer have a key associated with null for empty team
        Multimap<GameTeam, ServerPlayerEntity> teamPlayers = HashMultimap.create();
        var players = this.gameSpace.getPlayers().iterator();
        Collections.shuffle(teams, new Random());
        for(var team : teams)
        {
            for(int i = 0; i < config.teamSize(); i++)
            {
                teamPlayers.put(team, (players.hasNext() ? players.next() : null));
            }
        }


        new BedwarsActive(this.activity.getGameSpace(), this.map, this.world, teamPlayers, teamsInOrder,config);
        return GameResult.ok();
    }
}

