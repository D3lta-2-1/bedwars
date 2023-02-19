package me.verya.bedwars.game.component;

import me.verya.bedwars.game.TeleporterLogic;
import me.verya.bedwars.game.behavior.ClaimManager;
import me.verya.bedwars.game.event.BedwarsEvents;
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
    public Spawn(BlockBounds bounds, ClaimManager claim, GameTeam team, ServerWorld world, GameActivity activity)
    {
        this.bounds = bounds;
        this.team = team;
        this.world = world;
        this.activity = activity;

        claim.addRegion(this.bounds);
    }
    public void spawnPlayer(ServerPlayerEntity player)
    {
        player.changeGameMode(GameMode.SURVIVAL);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;
        player.setHealth(20.0f);
        TeleporterLogic.spawnPlayer(player, bounds.center(), world);
        activity.invoker(BedwarsEvents.PLAYER_RESPAWN).onRespawn(player);
    }
}