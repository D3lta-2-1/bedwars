package me.verya.bedwars.game;

import com.google.common.collect.Multimap;
import eu.pb4.sidebars.api.Sidebar;
import me.verya.bedwars.Bedwars;
import me.verya.bedwars.BedwarsConfig;
import me.verya.bedwars.game.component.TeamComponents;
import me.verya.bedwars.game.ui.BedwarsSideBar;
import me.verya.bedwars.game.behavior.ClaimManager;
import me.verya.bedwars.game.behavior.DeathManager;
import me.verya.bedwars.game.map.BedwarsMap;

import me.verya.bedwars.game.ui.Messager;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

import java.util.*;

public class BedwarsActive {
    final GameSpace gameSpace;
    final BedwarsMap gameMap;
    final BedwarsConfig config;
    final ServerWorld world;
    GameActivity activity;
    final TeamManager teamManager;
    final Map<GameTeamKey, TeamComponents> teamComponentsMap;
    final ClaimManager claim;
    final DeathManager deathManager;
    final Messager messager;
    final Sidebar sidebar;

    BedwarsActive(GameSpace gameSpace, BedwarsMap gameMap, ServerWorld world, Multimap<GameTeam, ServerPlayerEntity> teamPlayers, List<GameTeam> teamsInOrder, BedwarsConfig config)
    {
        this.gameSpace = gameSpace;
        this.gameMap = gameMap;
        this.config = config;
        this.world = world;
        gameSpace.setActivity(gameActivity -> this.activity = gameActivity);
        setupGameRules();
        this.claim = new ClaimManager(gameMap, config, activity);
        this.teamManager = TeamManager.addTo(activity);
        setupTeam(teamPlayers);
        this.teamComponentsMap = makeTeamComponents(); //forge bed, spawn ect
        this.deathManager = new DeathManager(teamComponentsMap, teamManager, world, gameMap, activity);
        this.sidebar = BedwarsSideBar.build(teamComponentsMap, teamManager, teamsInOrder);
        addPlayerToSideBar();
        this.messager = new Messager(teamManager, activity);
        destroySpawn();
        startGame();
    }
    private void setupGameRules()
    {
        //set gameRules
        activity.deny(GameRuleType.CRAFTING);
        activity.deny(GameRuleType.PORTALS);
        activity.allow(GameRuleType.PVP);
        activity.deny(GameRuleType.HUNGER);
        activity.allow(GameRuleType.FALL_DAMAGE);
        activity.allow(GameRuleType.BLOCK_DROPS);
        activity.allow(GameRuleType.THROW_ITEMS);
        activity.deny(GameRuleType.UNSTABLE_TNT);
        activity.allow(GameRuleType.PLAYER_PROJECTILE_KNOCKBACK);
        activity.allow(GameRuleType.TRIDENTS_LOYAL_IN_VOID);
        activity.deny(Bedwars.BED_INTERACTION);

    }

    private void setupTeam(Multimap<GameTeam, ServerPlayerEntity> teamPlayers)
    {
        for (GameTeam team : teamPlayers.keySet()) {
            //add players to their team
            teamManager.addTeam(team);
            for (var player : teamPlayers.get(team)) {
                if(player == null) continue;
                teamManager.addPlayerTo(player, team.key());
            }
        }
    }

    private Map<GameTeamKey, TeamComponents> makeTeamComponents()
    {
        var builder = new TeamComponents.Builder(teamManager, activity, world, claim, config, gameMap);
        var teamComponentsMap = new HashMap<GameTeamKey, TeamComponents>();
        for(var team : teamManager)
        {
            teamComponentsMap.put(team.key(), builder.createFor(team));
        }
        return teamComponentsMap;
    }

    private void addPlayerToSideBar()
    {
        for(var player : activity.getGameSpace().getPlayers())
            sidebar.addPlayer(player);
        sidebar.show();
    }

    private void destroySpawn()
    {
        for(var pos : gameMap.waiting())
        {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            gameMap.template().setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }
    private void startGame() {
        for(var team : teamManager) {
            var spawn = teamComponentsMap.get(team.key()).spawn;
            for (var player : teamManager.playersIn(team.key())) {
                spawn.spawnPlayer(player);
            }
        }
    }
}
