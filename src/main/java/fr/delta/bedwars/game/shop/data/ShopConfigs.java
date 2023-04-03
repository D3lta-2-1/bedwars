package fr.delta.bedwars.game.shop.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.shop.entries.ForgeUpgradeEntry;
import fr.delta.bedwars.game.shop.entries.ShopEntry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.io.IOException;

public class ShopConfigs {
    public static final TinyRegistry<ShopCategoriesConfig> CATEGORIES_REGISTRY = TinyRegistry.create();
    public static final TinyRegistry<TinyRegistry<ShopEntry>> ENTRIES_REGISTRY = TinyRegistry.create();
    private static final String entries_path = "bedwars_entries";
    private static final String category_path = "bedwars_shop_configs";

    public static void register() {
        ResourceManagerHelper serverData = ResourceManagerHelper.get(ResourceType.SERVER_DATA);
        serverData.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(Bedwars.ID, entries_path);
            }

            @Override
            public void reload(ResourceManager manager) {
                DynamicOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, (DynamicRegistryManager)null);
                CATEGORIES_REGISTRY.clear();
                ENTRIES_REGISTRY.clear();

                manager.findResources(category_path, path -> path.getPath().endsWith(".json")).forEach((path, resource) -> {
                    try {
                        try (var reader = resource.getReader()) {
                            JsonElement json = JsonParser.parseReader(reader);

                            Identifier identifier = identifierFromPath(category_path, path);

                            DataResult<ShopCategoriesConfig> result = ShopCategoriesConfig.CODEC.parse(ops, json);

                            result.result().ifPresent(category ->
                                CATEGORIES_REGISTRY.register(identifier, category)
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

                manager.findResources(entries_path, path -> path.getPath().endsWith(".json")).forEach((path, resource) -> {
                    try {
                        try (var reader = resource.getReader()) {
                            JsonElement json = JsonParser.parseReader(reader);

                            Identifier identifier = identifierFromPath(entries_path, path);

                            DataResult<ShopEntriesAndIDs> result = ShopEntriesAndIDs.CODEC.parse(ops, json);

                            result.result().ifPresent(EntriesAndIDs -> {
                                TinyRegistry<ShopEntry> registry = TinyRegistry.create();
                                //add defaulted entries
                                registry.register(new Identifier(Bedwars.ID, "forge_upgrade"), ForgeUpgradeEntry.INSTANCE);

                                for(var pair : EntriesAndIDs.entries())
                                    registry.register(pair.getFirst(), pair.getSecond());
                                ENTRIES_REGISTRY.register(identifier, registry);
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
                Bedwars.LOGGER.info("shop ItemShopCategories loaded :");
                CATEGORIES_REGISTRY.keySet().forEach(config -> Bedwars.LOGGER.info(config.toString()));
                Bedwars.LOGGER.info("entries configs loaded :");
                ENTRIES_REGISTRY.keySet().forEach(config -> Bedwars.LOGGER.info(config.toString()));
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
