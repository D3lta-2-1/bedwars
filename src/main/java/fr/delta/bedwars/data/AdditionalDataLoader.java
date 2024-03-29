package fr.delta.bedwars.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.StageEvent.StageEvent;
import fr.delta.bedwars.StageEvent.GameEventConfig;
import fr.delta.bedwars.game.resourceGenerator.GeneratorBuilder;
import fr.delta.bedwars.game.shop.data.ShopCategoriesConfig;
import fr.delta.bedwars.game.shop.data.ShopEntryConfig;
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
    public static final TinyRegistry<StageEvent> GAME_EVENT_REGISTRY = TinyRegistry.create();
    public static final TinyRegistry<Property> SKIN_REGISTRY = TinyRegistry.create();
    private static final String ENTRIES_PATH = getConfigPath("entries");
    private static final String CATEGORY_PATH = getConfigPath("categories");
    private static final String FORGE_PATH = getConfigPath("forges");
    private static final String GENERATOR_PATH = getConfigPath("generators");
    private static final String GAME_EVENTS_PATH = getConfigPath("events");
    private static final String SKINS_PATH = getConfigPath("skins");

    private static void loadEntries(ResourceManager manager, DynamicOps<JsonElement> ops)
    {
        SHOP_ENTRIES_REGISTRY.clear();
        manager.findResources(ENTRIES_PATH, path -> path.getPath().endsWith(".json")).forEach((path, resource) -> {
            try {
                try (var reader = resource.getReader()) {
                    JsonElement json = JsonParser.parseReader(reader);

                    Identifier identifier = identifierFromPath(ENTRIES_PATH, path);

                    DataResult<List<Pair<Identifier, ShopEntry>>> result = ShopEntryConfig.CODEC.listOf().parse(ops, json);

                    result.result().ifPresent(EntriesAndIDs -> {
                                TinyRegistry<ShopEntry> registry = TinyRegistry.create();
                                for(var pair : EntriesAndIDs)
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

    private static void loadGameEvent(ResourceManager manager, DynamicOps<JsonElement> ops)
    {
        GAME_EVENT_REGISTRY.clear();
        manager.findResources(GAME_EVENTS_PATH, path -> path.getPath().endsWith(".json")).forEach((path, resource) -> {
            try {
                try (var reader = resource.getReader()) {
                    JsonElement json = JsonParser.parseReader(reader);

                    Identifier identifier = identifierFromPath(GAME_EVENTS_PATH, path);

                    DataResult<StageEvent> result = GameEventConfig.CODEC.parse(ops, json);

                    result.result().ifPresent(event ->
                            GAME_EVENT_REGISTRY.register(identifier, event)
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

    private static void loadSkin(ResourceManager manager)
    {
        SKIN_REGISTRY.clear();
        var skinCache = SkinCache.load();

        manager.findResources(SKINS_PATH, path -> path.getPath().endsWith(".png")).forEach((path, resource) -> {
            try {
                var identifier = identifierFromPath(SKINS_PATH, path);

                var property = skinCache.get(identifier);

                if(property == null){
                    var input = resource.getInputStream();
                    property = SkinFetcher.setSkinFromFile(identifier, input);

                    if(property != null)
                        skinCache.put(identifier, property);
                }

                if(property != null){
                    SKIN_REGISTRY.register(identifier, property);
                }
            } catch (IOException e) {
                Bedwars.LOGGER.error("Failed to load skin at {}", path, e);
            }
        });
        SkinCache.save(skinCache);
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
                loadGameEvent(manager, ops);
                loadSkin(manager);
            }
        });
    }

    public static String getConfigPath(String path) {
        return Bedwars.ID + "/" + path;
    }

    private static Identifier identifierFromPath(String path, Identifier location) {
        String fullPath = location.getPath();
        var pointPos = fullPath.lastIndexOf('.');
        pointPos = pointPos > -1 ? pointPos : fullPath.length();
        fullPath = fullPath.substring((path +"/").length(), pointPos);
        return new Identifier(location.getNamespace(), fullPath);
    }
}
