package fr.delta.bedwars.game.teamComponent;

import fr.delta.bedwars.game.behaviour.ClaimManager;
import fr.delta.bedwars.game.behaviour.DeathManager;
import fr.delta.bedwars.game.map.BedwarsMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;

import java.util.List;
import java.util.Map;

public class TeamComponents
{
    public Spawn spawn;
    public Forge forge;
    public Bed bed;
    public StatusEffectHandler statusEffectHandler;
    public Map<Enchantment, Integer> enchantments;
    public StatusEffectPool effectPool;
    public TrapHandler trapHandler;

    static public class Builder
    {
        private final TeamManager teamManager;
        private final GameActivity activity;
        private final ServerWorld world;
        private final ClaimManager claimManager;
        private final BedwarsMap gameMap;
        private final DeathManager deathManager;

        private final List<Forge.Tier> forgeConfig;

        public Builder(TeamManager teamManager, GameActivity activity, ServerWorld world, ClaimManager claimManager, DeathManager deathManager, List<Forge.Tier> forgeConfig, BedwarsMap gameMap)
        {
            this.teamManager = teamManager;
            this.activity =  activity;
            this.world = world;
            this.claimManager = claimManager;
            this.deathManager = deathManager;
            this.forgeConfig = forgeConfig;
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
                    components.bed = new Bed(rawData.bedLocation, gameMap, team, teamManager, activity);
                    components.spawn = new Spawn(rawData.spawnLocation, claimManager, world, rawData.bedLocation.center(),activity);
                    components.forge = new Forge(rawData.forge, claimManager, forgeConfig, world, teamManager, deathManager, activity);
                    components.statusEffectHandler = new StatusEffectHandler(team.key(), teamManager, deathManager, activity);
                    components.enchantments = new Object2IntArrayMap<>();
                    components.effectPool = new StatusEffectPool(rawData.effectPool, team.key(), teamManager, activity);
                    components.trapHandler = new TrapHandler(TrapHandler.createFromBedBlockBound(rawData.bedLocation), world, teamManager, team, deathManager,activity);
                }
            }
            return components;
        }
    }
}