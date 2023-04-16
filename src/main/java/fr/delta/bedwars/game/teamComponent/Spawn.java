package fr.delta.bedwars.game.teamComponent;

import fr.delta.bedwars.game.TeleporterLogic;
import fr.delta.bedwars.game.behaviour.ClaimManager;
import fr.delta.bedwars.game.event.BedwarsEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;

public class Spawn {
    private final BlockBounds bounds;
    private final ServerWorld world;
    private final GameActivity activity;
    private final float yaw;
    private final Vec3d respawnPos;
    public Spawn(BlockBounds bounds, ClaimManager claim, ServerWorld world, Vec3d bedPos, GameActivity activity)
    {
        this.bounds = bounds;
        this.world = world;
        this.activity = activity;
        var x1 = bounds.center().x;
        var y1 = bounds.center().z;
        var x2 = bedPos.x;
        var y2 = bedPos.z;
        var pos = bounds.centerBottom();
        this.yaw = ((float)Math.toDegrees(Math.atan2(y1 - y2, x1 - x2)) + 90);
        while (!world.getBlockState(asBlockPos(pos)).isAir())
        {
            pos = pos.add(0,1,0);
        }
        this.respawnPos = pos;
        claim.addRegion(this.bounds);
    }

    public BlockBounds getBounds()
    {
        return bounds;
    }
    public void spawnPlayer(ServerPlayerEntity player)
    {
        player.changeGameMode(GameMode.SURVIVAL);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;
        player.setHealth(20.0f);
        player.clearStatusEffects();
        TeleporterLogic.spawnPlayer(player, respawnPos, world, yaw, 0.F);
        activity.invoker(BedwarsEvents.PLAYER_RESPAWN).onRespawn(player);
    }

    private static BlockPos asBlockPos(Vec3d vec) {
        return new BlockPos((int)vec.getX(), (int)vec.getY(), (int)vec.getZ());
    }
}