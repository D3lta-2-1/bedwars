package fr.delta.bedwars.game.traps;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;

import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;

import java.util.List;

public class Trap {

    private final Item icon;
    private final Text name;
    private final int cooldown;
    private final int playAlarmDuration;
    private final List<StatusEffectInstance> effectsForTrigger;
    private final List<StatusEffectInstance> effectsForOwner;
    private final List<StatusEffect> effectToRemoveForTrigger;

    public Trap(Item icon, Text name, int cooldown, int playAlarmDuration, List<StatusEffectInstance> effectsForTrigger, List<StatusEffectInstance> effectsForOwner, List<StatusEffect> effectToRemoveForTrigger)
    {
        this.icon = icon;
        this.name = name;
        this.cooldown = cooldown;
        this.playAlarmDuration = playAlarmDuration;
        this.effectsForTrigger = effectsForTrigger;
        this.effectsForOwner = effectsForOwner;
        this.effectToRemoveForTrigger = effectToRemoveForTrigger;
    }
    public int getCooldown() {
        return cooldown;
    }

    public Item getItem() {
        return icon;
    }

    public Text getName() {
        return name;
    }

    public Item getIcon() {
        return icon;
    }

    public int trigger(TeamManager teamManager, GameTeamKey owner, ServerPlayerEntity triggerer)
    {
        for(StatusEffectInstance effect : effectsForTrigger)
        {
            triggerer.addStatusEffect(effect);
        }
        for(StatusEffect effect : effectToRemoveForTrigger)
        {
            triggerer.removeStatusEffect(effect);
        }
        for(ServerPlayerEntity player : teamManager.playersIn(owner))
        {
            for(StatusEffectInstance effect : effectsForOwner)
            {
                player.addStatusEffect(effect);
            }
        }
        return playAlarmDuration;
    }

}
