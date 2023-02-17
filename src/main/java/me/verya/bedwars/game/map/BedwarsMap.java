package me.verya.bedwars.game.map;

import me.verya.bedwars.mixin.BedwarsConfig;
import me.verya.bedwars.Constants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public record BedwarsMap (MapTemplate template, MinecraftServer server, BlockBounds waiting, List<RawTeamData> teamData) {
    public static BedwarsMap loadMap(BedwarsConfig config, MinecraftServer server) throws GameOpenException {
        //load map
        MapTemplate template;
        try {
            template = MapTemplateSerializer.loadFromResource(server, config.mapConfig().id());
        } catch (IOException e) {
            throw new GameOpenException(Text.literal("Failed to load map"));
        }
        //get waiting spawn
        var waitingSpawn = template.getMetadata().getFirstRegionBounds(Constants.WAITING_SPAWN);
        //get teamMetaData such as bed location and spawn location
        List<RawTeamData> dataList      = new ArrayList<>();
        for(var color : Constants.TEAM_COLORS)
        {
            var spawnKey = color.name().toLowerCase() + "_" + Constants.SPAWN;
            var spawn = template.getMetadata().getFirstRegionBounds(spawnKey);
            var bedKey = color.name().toLowerCase() + "_" + Constants.BED;
            var bed = template.getMetadata().getFirstRegionBounds(bedKey);
            var forgeKey = color.name().toLowerCase() + "_" + Constants.FORGE;
            var forge = template.getMetadata().getFirstRegionBounds(forgeKey);
            if(spawn == null || bed == null || forge == null) continue;
            dataList.add(new RawTeamData(color, spawn, bed, forge));
        }
        //check if correctly load the map
        if(dataList.isEmpty())
            throw new GameOpenException(Text.literal("no team spawn found"));
        if(waitingSpawn == null)
            throw new GameOpenException(Text.literal("no waiting spawn found"));

        return new BedwarsMap(template, server, waitingSpawn, dataList);
    }

    private ChunkGenerator asGenerator() {
        return new TemplateChunkGenerator(this.server, this.template);
    }

    public RuntimeWorldConfig asRuntimeWorldConfig() {
        return new RuntimeWorldConfig().setGenerator(this.asGenerator()).setGameRule(GameRules.DO_FIRE_TICK, false);
    }
}
