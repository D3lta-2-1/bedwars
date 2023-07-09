package fr.delta.bedwars.game.map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.codec.BedwarsConfig;
import fr.delta.bedwars.Constants;
import fr.delta.bedwars.data.AdditionalDataLoader;
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

public record BedwarsMap (MapTemplate template, MinecraftServer server, BlockBounds waiting, List<RawTeamData> teamData, List<BlockBounds> itemShopKeepers, List<BlockBounds> teamShopKeepers, Multimap<String, BlockBounds> generatorsRegions) {
    public static BedwarsMap loadMap(BedwarsConfig config, MinecraftServer server) throws GameOpenException {
        //load map
        MapTemplate template;
        try {
            template = MapTemplateSerializer.loadFromResource(server, config.mapId());
        } catch (IOException e) {
            throw new GameOpenException(Text.literal("Failed to load map"));
        }

        //get waiting spawn
        var waitingSpawn = template.getMetadata().getFirstRegionBounds(Constants.WAITING_SPAWN);

        List<RawTeamData> dataList = new ArrayList<>();

        //get item_shopkeepers
        var itemShopkeepers = template.getMetadata().getRegionBounds(Constants.ITEM_SHOPKEEPER).toList();
        var teamShopkeepers = template.getMetadata().getRegionBounds(Constants.TEAM_SHOPKEEPER).toList();

        //get all team components
        var spawns = template.getMetadata().getRegionBounds(Constants.SPAWN).toList();
        var beds = template.getMetadata().getRegionBounds(Constants.BED).toList();
        var forges = template.getMetadata().getRegionBounds(Constants.FORGE).toList();

        //detect a team base and get all components by intersecting
        var baseList = new ArrayList<BlockBounds>(Constants.TEAM_COLORS.size());
        for(var color : Constants.TEAM_COLORS)
        {
            var base = template.getMetadata().getFirstRegionBounds(color.name().toLowerCase());
            if(base == null) continue;
            baseList.add(base);
            BlockBounds spawn = getByIntersect(spawns, base),
                    bed = getByIntersect(beds, base),
                    forge = getByIntersect(forges, base);

            if(spawn == null) throw new GameOpenException(Text.literal("no " + color.name().toLowerCase() + " spawn found"));
            if(bed == null) throw new GameOpenException(Text.literal("no " + color.name().toLowerCase() + " bed found"));
            if(forge == null) throw new GameOpenException(Text.literal("no " + color.name().toLowerCase() + " forge found"));

            dataList.add(new RawTeamData(color, spawn, bed, forge, base));
        }

        //check if correctly load the map
        if(waitingSpawn == null)
            throw new GameOpenException(Text.literal("no waiting spawn found"));
        if(dataList.isEmpty())
            throw new GameOpenException(Text.literal("no team found"));

        //warning
        warnIfDifferentVolumes(baseList, "all bases are not the same size");
        warnIfDifferentVolumes(spawns, "all spawns are not the same size");
        warnIfDifferentVolumes(beds, "all beds are not the same size");
        warnIfDifferentVolumes(forges, "all forges are not the same size");
        warnIfDifferentVolumes(itemShopkeepers, "all item shopkeepers are not the same size");
        warnIfDifferentVolumes(teamShopkeepers, "all team shopkeepers are not the same size");

        //get generators
        var generatorTypeList = config.generatorTypeIdList();
        Multimap<String, BlockBounds> generatorsRegions = ArrayListMultimap.create();
        for(var generatorTypeId : generatorTypeList)
        {
            var generatorType = AdditionalDataLoader.GENERATOR_TYPE_REGISTRY.get(generatorTypeId);
            if(generatorType == null) continue;
            var generatorRegions = template.getMetadata().getRegionBounds(generatorType.getInternalId().toLowerCase()).toList();
            if(generatorRegions.isEmpty()) continue;
            for(var generatorRegion : generatorRegions)
            {
                generatorsRegions.put(generatorType.getInternalId(), generatorRegion);
            }
        }

        return new BedwarsMap(template, server, waitingSpawn, dataList, itemShopkeepers, teamShopkeepers, generatorsRegions);
    }

    private ChunkGenerator asGenerator() {
        return new TemplateChunkGenerator(this.server, this.template);
    }

    public RuntimeWorldConfig asRuntimeWorldConfig() {
        return new RuntimeWorldConfig().setGenerator(this.asGenerator()).setGameRule(GameRules.DO_FIRE_TICK, false);
    }

    static private int getVolume(BlockBounds bounds) {
        var size = bounds.size();
        return (size.getX() + 1) * (size.getY() + 1) * (size.getZ() + 1); //size + 1 because it's the number of blocks
    }

    static private void warnIfDifferentVolumes(List<BlockBounds> bounds, String warn) {
        if(bounds.isEmpty()) return;
        int totalSpawnSize = 0;
        for(var bound : bounds)
            totalSpawnSize += getVolume(bound);
        if(totalSpawnSize % getVolume(bounds.get(0)) != 0)
            Bedwars.LOGGER.warn(warn);
    }

    static private BlockBounds getByIntersect(List<BlockBounds> bounds, BlockBounds base) {
        for(var bound : bounds)
            if(bound.intersects(base))
                return bound;
        return null;
    }

}
