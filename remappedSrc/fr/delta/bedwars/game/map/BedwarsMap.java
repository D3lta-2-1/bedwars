package fr.delta.bedwars.game.map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import fr.delta.bedwars.codec.BedwarsConfig;
import fr.delta.bedwars.Constants;
import fr.delta.bedwars.data.AdditionalDataLoader;
import fr.delta.bedwars.game.resourceGenerator.GeneratorBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
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
        //get teamMetaData such as bed location and spawn location
        List<RawTeamData> dataList = new ArrayList<>();
        for(var color : Constants.TEAM_COLORS)
        {
            var spawn = getBoundsFor(color, Constants.SPAWN, template);
            var bed = getBoundsFor(color, Constants.BED, template);
            var forge = getBoundsFor(color, Constants.FORGE, template);
            var effectPool = getBoundsFor(color, Constants.EFFECT_POOL, template);
            if(spawn == null || bed == null || forge == null) continue;
            dataList.add(new RawTeamData(color, spawn, bed, forge, effectPool));
        }
        //get item_shopkeepers
        var itemShopkeepers = template.getMetadata().getRegionBounds(Constants.ITEM_SHOPKEEPER).toList();
        var teamShopkeepers = template.getMetadata().getRegionBounds(Constants.TEAM_SHOPKEEPER).toList();
        //check if correctly load the map
        if(dataList.isEmpty())
            throw new GameOpenException(Text.literal("no team spawn found"));
        if(waitingSpawn == null)
            throw new GameOpenException(Text.literal("no waiting spawn found"));

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

    private static String getKeyFor(DyeColor color, String type) {
        return color.name().toLowerCase() + "_" + type;
    }

    private static BlockBounds getBoundsFor(DyeColor color, String type, MapTemplate template) {
        return template.getMetadata().getFirstRegionBounds(getKeyFor(color, type));
    }
}
