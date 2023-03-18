package fr.delta.bedwars.game.component;

import fr.delta.bedwars.BedwarsConfig;
import fr.delta.bedwars.game.behavior.ClaimManager;
import fr.delta.bedwars.game.map.BedwarsMap;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;

public class TeamComponents
{
    public Spawn spawn;
    public Forge forge;
    public Bed bed;

    static public class Builder
    {
        private final TeamManager teamManager;
        private final GameActivity activity;
        private final ServerWorld world;
        private final ClaimManager claimManager;
        private final BedwarsConfig config;
        private final BedwarsMap gameMap;

        public Builder(TeamManager teamManager, GameActivity activity, ServerWorld world, ClaimManager claimManager, BedwarsConfig config, BedwarsMap gameMap)
        {
            this.teamManager = teamManager;
            this.activity =  activity;
            this.world = world;
            this.claimManager = claimManager;
            this.config = config;
            this.gameMap = gameMap;
        }

        public TeamComponents createFor(GameTeam team)
        {
            var components = new TeamComponents();
            var color = team.config().blockDyeColor();
            for (var rawData : gameMap.teamData()) {
                if (rawData.color.equals(color)) {
                    components.bed = new Bed(rawData.bedLocation, gameMap, team, teamManager, activity);
                    components.spawn = new Spawn(rawData.spawnLocation, claimManager, team, world, rawData.bedLocation.center(),activity);
                    components.forge = new Forge(rawData.forge, claimManager, config.forgeConfig(), world, teamManager ,activity);
                }
            }
            return components;
        }
    }
}