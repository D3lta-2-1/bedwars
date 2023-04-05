package fr.delta.bedwars.game.teamComponent;

import fr.delta.bedwars.game.behaviour.DeathManager;
import fr.delta.bedwars.game.event.BedwarsEvents;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import java.util.Map;

public class StatusEffectHandler {
    private final Map<StatusEffect, Integer> statusEffects = new Object2IntArrayMap<>();
    private final GameTeamKey team;
    private final TeamManager teamManager;
    private final DeathManager deathManager;


    public StatusEffectHandler(GameTeamKey team, TeamManager teamManager, DeathManager deathManager, GameActivity activity) {
        this.team = team;
        this.teamManager = teamManager;
        this.deathManager = deathManager;
        activity.listen(BedwarsEvents.PLAYER_RESPAWN, this::onPlayerRespawn);
    }

    private void onPlayerRespawn(ServerPlayerEntity player) {
        if(teamManager.teamFor(player) != team) return;
        for (var effect : statusEffects.entrySet()) {
            player.addStatusEffect(new StatusEffectInstance(effect.getKey(), -1, effect.getValue(), false, false, true));
        }
    }

    public void setEffect(StatusEffect effect, int amplifier) {
        statusEffects.put(effect, amplifier);
        teamManager.playersIn(team).forEach(player ->{
            if(!deathManager.isAlive(player)) return;
            player.addStatusEffect(new StatusEffectInstance(effect, -1, amplifier, false, false, true));
        });
    }

    public int getAmplifier(StatusEffect effect) {
        if(statusEffects.containsKey(effect))
            return statusEffects.get(effect);
        return -1;
    }

}
