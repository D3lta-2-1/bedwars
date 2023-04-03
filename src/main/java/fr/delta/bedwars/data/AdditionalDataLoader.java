package fr.delta.bedwars.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.resourceGenerator.GeneratorBuilder;
import fr.delta.bedwars.game.shop.data.ShopCategoriesConfig;
import fr.delta.bedwars.game.shop.data.ShopEntriesAndIDs;
import fr.delta.bedwars.game.shop.entries.ForgeUpgradeEntry;
import fr.delta.bedwars.game.shop.entries.ShopEntry;
import fr.delta.bedwars.game.teamComponent.Forge;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.io.IOException;
import java.util.List;

public class AdditionalDataLoader {
    public static final TinyRegistry<ShopCategoriesConfig> SHOP_CATEGORIES_REGISTRY = TinyRegistry.create();
    public static final TinyRegistry<TinyRegistry<ShopEntry>> SHOP_ENTRIES_REGISTRY = TinyRegistry.create();
    public static final TinyRegistry<List<Forge.Tier>> FORGE_CONFIG_REGISTRY = TinyRegistry.create();
    public static final TinyRegistry<GeneratorBuilder> GENERATOR_TYPE_REGISTRY = TinyRegistry.create();
    private static final String ENTRIES_PATH = "bedwars_entries";
    private static final String CATEGORY_PATH = "bedwars_categories";
    private static final String FORGE_PATH = "bedwars_forges";
    private static final String GENERATOR_PATH = "bedwars_generators";

    private static void loadEntries(ResourceManager manager, DynamicOps<JsonElement> ops)
    {
        SHOP_ENTRIES_REGISTRY.clear();
        manager.findResources(ENTRIES_PATH, path -> path.getPath().endsWith(".json")).forEach((path, resource) -> {
            try {
                try (var reader = resource.getReader()) {
                    JsonElement json = JsonParser.parseReader(reader);

                    Identifier identifier = identifierFromPath(ENTRIES_PATH, path);

                    DataResult<ShopEntriesAndIDs> result = ShopEntriesAndIDs.CODEC.parse(ops, json);

                    result.result().ifPresent(EntriesAndIDs -> {
                                TinyRegistry<ShopEntry> registry = TinyRegistry.create();
                                //add defaulted entries
                                registry.register(new Identifier(Bedwars.ID, "forge_upgrade"), ForgeUpgradeEntry.INSTANCE);

                                for(var pair : EntriesAndIDs.entries())
                                    registry.register(pair.getFirst(), pair.getSecond());
                                SHOP_ENTRIES_REGISTRY.register(identifier, registry);
                            }
                    );

                    result.error().ifPresent(error ->
                            Bedwars.LOGGER.error("Failed to parse game at {}: {}", path, error)
                    );
                }
            } catch (IOException e) {
                Bedwars.LOGGER.error("Failed to read configured game at {}", path, e);
            } catch (JsonParseException e) {
                Bedwars.LOGGER.error("Failed to parse game JSON at {}: {}", path, e);
            }
            Bedwars.LOGGER.info("entries files loaded :");
            SHOP_ENTRIES_REGISTRY.keySet().forEach(config -> Bedwars.LOGGER.info(config.toString()));
        });
    }

    private static void loadCategories(ResourceManager manager, DynamicOps<JsonElement> ops)
    {
        SHOP_CATEGORIES_REGISTRY.clear();
        manager.findResources(CATEGORY_PATH, path -> path.getPath().endsWith(".json")).forEach((path, resource) -> {
            try {
                try (var reader = resource.getReader()) {
                    JsonElement json = JsonParser.parseReader(reader);

                    Identifier identifier = identifierFromPath(CATEGORY_PATH, path);

                    DataResult<ShopCategoriesConfig> result = ShopCategoriesConfig.CODEC.parse(ops, json);

                    result.result().ifPresent(category ->
                            SHOP_CATEGORIES_REGISTRY.register(identifier, category)
                    );

                    result.error().ifPresent(error ->
                            Bedwars.LOGGER.error("Failed to parse game at {}: {}", path, error)
                    );
                }
            } catch (IOException e) {
                Bedwars.LOGGER.error("Failed to read configured game at {}", path, e);
            } catch (JsonParseException e) {
                Bedwars.LOGGER.error("Failed to parse game JSON at {}: {}", path, e);
            }
        });
        Bedwars.LOGGER.info("shop categories files loaded :");
        SHOP_CATEGORIES_REGISTRY.keySet().forEach(config -> Bedwars.LOGGER.info(config.toString()));
    }

    private static void loadForgeConfig(ResourceManager manager, DynamicOps<JsonElement> ops)
    {
        FORGE_CONFIG_REGISTRY.clear();
        manager.findResources(FORGE_PATH, path -> path.getPath().endsWith(".json")).forEach((path, resource) -> {
            try {
                try (var reader = resource.getReader()) {
                    JsonElement json = JsonParser.parseReader(reader);

                    Identifier identifier = identifierFromPath(FORGE_PATH, path);

                    DataResult<List<Forge.Tier>> result = Forge.CODEC.parse(ops, json);

                    result.result().ifPresent(tiers ->
                            FORGE_CONFIG_REGISTRY.register(identifier, tiers)
                    );

                    result.error().ifPresent(error ->
                            Bedwars.LOGGER.error("Failed to parse game at {}: {}", path, error)
                    );
                }
            } catch (IOException e) {
                Bedwars.LOGGER.error("Failed to read configured game at {}", path, e);
            } catch (JsonParseException e) {
                Bedwars.LOGGER.error("Failed to parse game JSON at {}: {}", path, e);
            }
        });
        Bedwars.LOGGER.info("forge config files loaded :");
        FORGE_CONFIG_REGISTRY.keySet().forEach(config -> Bedwars.LOGGER.info(config.toString()));
    }

    private static void loadGeneratorTypes(ResourceManager manager, DynamicOps<JsonElement> ops)
    {
        GENERATOR_TYPE_REGISTRY.clear();
        manager.findResources(GENERATOR_PATH, path -> path.getPath().endsWith(".json")).forEach((path, resource) -> {
            try {
                try (var reader = resource.getReader()) {
                    JsonElement json = JsonParser.parseReader(reader);

                    Identifier identifier = identifierFromPath(GENERATOR_PATH, path);

                    DataResult<GeneratorBuilder> result = GeneratorBuilder.CODEC.parse(ops, json);

                    result.result().ifPresent(builder ->
                            GENERATOR_TYPE_REGISTRY.register(identifier, builder)
                    );

                    result.error().ifPresent(error ->
                            Bedwars.LOGGER.error("Failed to parse game at {}: {}", path, error)
                    );
                }
            } catch (IOException e) {
                Bedwars.LOGGER.error("Failed to read configured game at {}", path, e);
            } catch (JsonParseException e) {
                Bedwars.LOGGER.error("Failed to parse game JSON at {}: {}", path, e);
            }
        });
    }

    public static void register()
    {
        ResourceManagerHelper serverData = ResourceManagerHelper.get(ResourceType.SERVER_DATA);
        serverData.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(Bedwars.ID, ENTRIES_PATH);
            }

            @Override
            public void reload(ResourceManager manager) {
                DynamicOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, (DynamicRegistryManager)null);
                loadEntries(manager, ops);
                loadCategories(manager, ops);
                loadForgeConfig(manager, ops);
                loadGeneratorTypes(manager, ops);
            }
        });
    }

    private static Identifier identifierFromPath(String path, Identifier location) {
        String fullPath = location.getPath();
        fullPath = fullPath.substring((path +"/").length(), fullPath.length() - ".json".length());
        return new Identifier(location.getNamespace(), fullPath);
    }

    public static void initialize(TinyRegistry<ShopEntry> entries, BedwarsActive game)
    {
        for(var entry : entries.values())
        {
            entry.setup(game);
        }

    }
}
