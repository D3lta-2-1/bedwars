package fr.delta.bedwars.game.teamComponent;

import D;
import fr.delta.bedwars.game.TeleporterLogic;
import fr.delta.bedwars.game.behaviour.ClaimManager;
import fr.delta.bedwars.game.event.BedwarsEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;

public class Spawn {
    final BlockBounds bounds;
    final GameTeam team;
    final ServerWorld world;
    final GameActivity activity;
    final float yaw;
    public Spawn(BlockBounds bounds, ClaimManager claim, GameTeam team, ServerWorld world, Vec3d bedPos, GameActivity activity)
    {
        this.bounds = bounds;
        this.team = team;
        this.world = world;
        this.activity = activity;
        var x1 = bounds.center().x;
        var y1 = bounds.center().z;
        var x2 = bedPos.x;
        var y2 = bedPos.z;
        this.yaw = ((float)Math.toDegrees(Math.atan2(y2 - y1, x2 - x1)) + 270) % 360;
        claim.addRegion(this.bounds);
    }
    public void spawnPlayer(ServerPlayerEntity player)
    {
        player.changeGameMode(GameMode.SURVIVAL);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;
        player.setHealth(20.0f);
        player.clearStatusEffects();
        TeleporterLogic.spawnPlayer(player, bounds.center(), world, yaw, 0.F);
        activity.invoker(BedwarsEvents.PLAYER_RESPAWN).onRespawn(player);
    }
}