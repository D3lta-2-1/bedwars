package fr.delta.bedwars.game.teamComponent;

import fr.delta.bedwars.BedwarsConfig;
import fr.delta.bedwars.data.AdditionalDataLoader;
import fr.delta.bedwars.game.behaviour.ClaimManager;
import fr.delta.bedwars.game.behaviour.DeathManager;
import fr.delta.bedwars.game.map.BedwarsMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import java.util.Map;

public class TeamComponents
{
    public Spawn spawn;
    public Forge forge;
    public Bed bed;
    public StatusEffectHandler statusEffectHandler;
    public Map<Enchantment, Integer> enchantments;
    public StatusEffectPool effectPool;

    static public class Builder
    {
        private final TeamManager teamManager;
        private final GameActivity activity;
        private final ServerWorld world;
        private final ClaimManager claimManager;
        private final BedwarsConfig config;
        private final BedwarsMap gameMap;
        private final DeathManager deathManager;

        public Builder(TeamManager teamManager, GameActivity activity, ServerWorld world, ClaimManager claimManager, DeathManager deathManager, BedwarsConfig config, BedwarsMap gameMap)
        {
            this.teamManager = teamManager;
            this.activity =  activity;
            this.world = world;
            this.claimManager = claimManager;
            this.deathManager = deathManager;
            this.config = config;
            this.gameMap = gameMap;
        }

        public TeamComponents createFor(GameTeam team)
        {
            var components = new TeamComponents();
            var color = team.config().blockDyeColor();
            for (var rawData : gameMap.teamData())
            {
                if (rawData.color.equals(color))
                {
                    var forgeConfig = AdditionalDataLoader.FORGE_CONFIG_REGISTRY.get(config.forgeConfigId());
                    if(forgeConfig == null)
                        throw new NullPointerException(config.forgeConfigId().toString() + " forge does not exist");

                    components.bed = new Bed(rawData.bedLocation, gameMap, team, teamManager, activity);
                    components.spawn = new Spawn(rawData.spawnLocation, claimManager, world, rawData.bedLocation.center(),activity);
                    components.forge = new Forge(rawData.forge, claimManager, forgeConfig, world, teamManager ,activity);
                    components.statusEffectHandler = new StatusEffectHandler(team.key(), teamManager, deathManager, activity);
                    components.enchantments = new Object2IntArrayMap<>();
                    components.effectPool = new StatusEffectPool(rawData.effectPool, team.key(), teamManager, activity);
                }
            }
            return components;
        }
    }
}