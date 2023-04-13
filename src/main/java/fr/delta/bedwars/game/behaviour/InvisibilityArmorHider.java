package fr.delta.bedwars.game.behaviour;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import fr.delta.bedwars.event.StatusEffectEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpacePlayers;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerS2CPacketEvent;

import java.util.*;

public class InvisibilityArmorHider {

    private final GameSpacePlayers players;
    private final TeamManager teamManager;
    private final Set<ServerPlayerEntity> playerInvisible = new HashSet<>();

    public InvisibilityArmorHider(TeamManager teamManager, GameActivity activity) {
        this.players = activity.getGameSpace().getPlayers();
        this.teamManager = teamManager;
        activity.listen(StatusEffectEvent.ADD, this::onStatusEffectAdd);
        activity.listen(StatusEffectEvent.REMOVE, this::onStatusEffectRemove);
        activity.listen(PlayerDamageEvent.EVENT, this::onPlayerDamage);
        activity.listen(PlayerS2CPacketEvent.EVENT, this::onPacket);
    }

    void onStatusEffectAdd(LivingEntity entity, StatusEffectInstance effect, @Nullable Entity source) {
        if(entity instanceof ServerPlayerEntity AffectedPlayer)
        {
            if(effect.getEffectType() != StatusEffects.INVISIBILITY) return;
            var teamKey = teamManager.teamFor(AffectedPlayer);
            var equipmentList = getEmptyEquipmentList();
            if(teamKey == null) return;
            for(var player : this.players)
            {
                if(teamManager.teamFor(player) == teamKey) continue;
                player.networkHandler.sendPacket(new EntityEquipmentUpdateS2CPacket(AffectedPlayer.getId(), equipmentList));
                playerInvisible.add(player);
            }
        }
    }

    void onStatusEffectRemove(LivingEntity entity, StatusEffectInstance effect) {
        if(entity instanceof ServerPlayerEntity AffectedPlayer)
        {
            if(effect.getEffectType() != StatusEffects.INVISIBILITY) return;

            var teamKey = teamManager.teamFor(AffectedPlayer);
            var equipmentList = getEquipmentList(AffectedPlayer);
            if(teamKey == null) return;
            for(var player : this.players)
            {
                if(teamManager.teamFor(player) == teamKey) continue;
                playerInvisible.remove(player);
                player.networkHandler.sendPacket(new EntityEquipmentUpdateS2CPacket(AffectedPlayer.getId(), equipmentList));
            }
        }
    }

    ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        var attacker =source.getAttacker();
        if(attacker == null) return ActionResult.PASS;
        if(attacker instanceof ServerPlayerEntity)
        {
            player.removeStatusEffect(StatusEffects.INVISIBILITY);
        }
        return ActionResult.PASS;
    }

    ActionResult onPacket(ServerPlayerEntity player, Packet<?> packet)
    {
        if(packet instanceof EntityEquipmentUpdateS2CPacket equipmentPacket)
        {
            var entity = player.getWorld().getEntityById(equipmentPacket.getId());
            if(entity instanceof ServerPlayerEntity)
            {
                if(playerInvisible.contains(player)) return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }
    private List<Pair<EquipmentSlot, ItemStack>> getEquipmentList(ServerPlayerEntity player) {
        List<Pair<EquipmentSlot, ItemStack>> equipmentList = Lists.newArrayList();

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = player.getEquippedStack(equipmentSlot);
            equipmentList.add(Pair.of(equipmentSlot, itemStack.copy()));
        }
        return equipmentList;
    }

    private List<Pair<EquipmentSlot, ItemStack>> getEmptyEquipmentList() {
        List<Pair<EquipmentSlot, ItemStack>> equipmentList = Lists.newArrayList();

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = ItemStack.EMPTY;
            equipmentList.add(Pair.of(equipmentSlot, itemStack.copy()));
        }
        return equipmentList;
    }
}
