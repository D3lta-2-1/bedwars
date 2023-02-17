package me.verya.bedwars.game.component;

import me.verya.bedwars.game.TeleporterLogic;
import me.verya.bedwars.game.behavior.ClaimManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;

public class Spawn {
    BlockBounds bounds;
    GameTeam team;
    ServerWorld world;
    public Spawn(BlockBounds bounds, ClaimManager claim, GameTeam team, ServerWorld world, GameActivity activity)
    {
        this.bounds = bounds;
        this.team = team;
        this.world = world;

        claim.addRegion(this.bounds);
    }
    public void spawnPlayer(ServerPlayerEntity entity)
    {
        TeleporterLogic.spawnPlayer(entity, bounds.center(), world);
    }
}
