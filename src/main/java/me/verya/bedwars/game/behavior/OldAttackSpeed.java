package me.verya.bedwars.game.behavior;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;

import java.util.Objects;

public class OldAttackSpeed {
    public OldAttackSpeed(GameActivity activity)
    {
        activity.listen(GamePlayerEvents.JOIN, this::onPlayerJoin);
        activity.listen(GamePlayerEvents.LEAVE, this::onPlayerLeave);
        var players = activity.getGameSpace().getPlayers();
        for(var player : players)
            onPlayerJoin(player);
    }

    private void onPlayerJoin(ServerPlayerEntity player)
    {
        EntityAttributeInstance attackSpeedInstance = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
        if (attackSpeedInstance != null) {
            attackSpeedInstance.setBaseValue(20D);
        }
    }

    private void onPlayerLeave(ServerPlayerEntity player)
    {
        if (player.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_SPEED) && player.getAttributes().getBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED) == 20D) {
            Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED)).setBaseValue(4D);
        }
    }
}
