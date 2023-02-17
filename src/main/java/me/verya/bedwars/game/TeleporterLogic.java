package me.verya.bedwars.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;


public class TeleporterLogic {
    public static void spawnPlayer(ServerPlayerEntity player, Vec3d pos, ServerWorld world) {
        player.teleport(world, pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
        player.setOnGround(true);
    }

}
