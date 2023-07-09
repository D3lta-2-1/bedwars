package fr.delta.bedwars.game;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.codec.BedwarsConfig;
import fr.delta.bedwars.game.map.BedwarsMap;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.util.PlayerRef;

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
    }
    static public GameOpenProcedure open(GameOpenContext<BedwarsConfig> context)
    {
        var config = context.config();
        var map = BedwarsMap.loadMap(config, context.server());
        var worldConfig = map.asRuntimeWorldConfig();
        worldConfig.setTimeOfDay(config.timeOfDay());
        worldConfig.setGameRule(GameRules.DO_FIRE_TICK, true);
        worldConfig.setGameRule(GameRules.RANDOM_TICK_SPEED, 100);

        return context.openWithWorld(worldConfig, (activity, world) ->
            new BedwarsWaiting(world, map, activity, config));
    }
    private void registerEvents()
    {
        var maxPlayerCount = this.map.teamData().size() * teamSize;
        GameWaitingLobby.addTo(activity,new PlayerConfig(1, maxPlayerCount, maxPlayerCount - 1, PlayerConfig.Countdown.DEFAULT));
        activity.listen(GamePlayerEvents.OFFER, offer -> offer.accept(world, map.waiting().centerTop()));
        activity.listen(GameActivityEvents.REQUEST_START, this::requestStart);
    }

    private GameResult requestStart()
    {
        List<GameTeam> teams = new ArrayList<>();
        for (var mapTeamData : map.teamData())
        {
            var dyeColor = mapTeamData.color;
            var color = GameTeamConfig.Colors.from(dyeColor);
            var prefix = TextUtilities.getTranslation("prefix", dyeColor.name()).append(Text.of(" ")).formatted(color.chatFormatting());

            GameTeam team = new GameTeam(
                    new GameTeamKey("bedwars_team_" + dyeColor.name().toLowerCase()),
                    GameTeamConfig.builder()
                            .setPrefix(prefix)
                            .setCollision(AbstractTeam.CollisionRule.PUSH_OWN_TEAM) //inverted ??
                            .setFriendlyFire(false)
                            .setNameTagVisibility(AbstractTeam.VisibilityRule.ALWAYS)
                            .setColors(color)
                            .build()
            );
            teams.add(team);
        }

        //to a scoreboard ordered
        var teamsInOrder = new ArrayList<>(teams);
        //make teams... without TeamAllocator because it auto delete empty teams and I prefer have a key associated with null for empty team
        Multimap<GameTeam, PlayerRef> teamPlayers = HashMultimap.create();
        var players = this.gameSpace.getPlayers().iterator();
        Collections.shuffle(teams, new Random());
        for(var team : teams)
        {
            for(int i = 0; i < config.teamSize(); i++)
            {
                teamPlayers.put(team, (players.hasNext() ? PlayerRef.of(players.next()) : null));
            }
        }


        new BedwarsActive(this.activity.getGameSpace(), this.map, this.world, teamPlayers, teamsInOrder,config);
        return GameResult.ok();
    }
}

