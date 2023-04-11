package fr.delta.bedwars.game.teamComponent;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import fr.delta.bedwars.game.event.BedwarsEvents;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;

import java.util.Map;

public class StatusEffectPool {
    private final BlockBounds bounds;
    private final Map<StatusEffect, Integer> effects = new Object2IntArrayMap<>();
    private final Multimap<ServerPlayerEntity, StatusEffect> effectGivenToPlayer = HashMultimap.create();
    private final GameTeamKey team;
    private final TeamManager teamManager;

    public StatusEffectPool(BlockBounds bounds, GameTeamKey team, TeamManager teamManager, GameActivity activity) {
        this.bounds = bounds;
        this.teamManager = teamManager;
        this.team = team;
        activity.listen(GameActivityEvents.TICK, this::onTick);
        activity.listen(BedwarsEvents.AFTER_PLAYER_DEATH, this::onPlayerDeath);
    }

    void onTick()
    {
        for(var player : teamManager.playersIn(team)) //for each player in the team
        {
            if(bounds.contains(player.getBlockPos())) //if the player is in the pool region
            {
                for(var effect : effects.entrySet()) //for each effect in the pool
                {
                    if(!(player.hasStatusEffect(effect.getKey())||effectGivenToPlayer.containsEntry(player, effect.getKey())))
                    {
                        effectGivenToPlayer.put(player, effect.getKey());
                        player.addStatusEffect(new StatusEffectInstance(effect.getKey(), -1, effect.getValue(), false, false, false));
                    }
                }
            }
            else//if the player is not in the pool region
            {
                for(var effectToRemove : effectGivenToPlayer.get(player)) //for each effect given to the player
                {
                    player.removeStatusEffect(effectToRemove); //remove the effect
                }
                effectGivenToPlayer.clear();
            }
        }
    }

    public void setEffect(StatusEffect effect, int level)
    {
        effects.put(effect, level);
    }

    public int getAmplifier(StatusEffect effect)
    {
        return effects.getOrDefault(effect, -1);
    }

    void onPlayerDeath(ServerPlayerEntity player, DamageSource source, ServerPlayerEntity killer, boolean isFinalKill)
    {
        effectGivenToPlayer.clear(); //clear the effect given to the player when he dies, the effect will be removed by the death manager
    }
}
