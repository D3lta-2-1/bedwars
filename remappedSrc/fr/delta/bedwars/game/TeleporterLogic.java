package fr.delta.bedwars.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;


public class TeleporterLogic {
    public static void spawnPlayer(ServerPlayerEntity player, Vec3d pos, ServerWorld world, float yaw, float pitch) {
        player.teleport(world, pos.getX(), pos.getY(), pos.getZ(), yaw, pitch);
        player.setOnGround(true);
    }

    public static void spawnPlayer(ServerPlayerEntity player, Vec3d pos, ServerWorld world) { spawnPlayer(player, pos, world, 0.F,0.F);}

}
