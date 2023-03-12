package fr.delta.bedwars.game.shop.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import fr.delta.bedwars.Bedwars;
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
    public static final TinyRegistry<ItemShopConfig> CONFIGS = TinyRegistry.create();
    private static final String path = "bedwars_shops";

    public static void register() {
        ResourceManagerHelper serverData = ResourceManagerHelper.get(ResourceType.SERVER_DATA);
        serverData.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(Bedwars.ID, path);
            }

            @Override
            public void reload(ResourceManager manager) {
                CONFIGS.clear();
                DynamicOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, (DynamicRegistryManager)null);
                manager.findResources(path, path -> path.getPath().endsWith(".json")).forEach((path, resource) -> {
                    try {
                        try (var reader = resource.getReader()) {
                            JsonElement json = JsonParser.parseReader(reader);

                            Identifier identifier = identifierFromPath(path);

                            DataResult<ItemShopConfig> result = ItemShopConfig.CODEC.parse(ops, json);

                            result.result().ifPresent(game ->
                                CONFIGS.register(identifier, game)
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
                Bedwars.LOGGER.info("shop categories loaded :");
                CONFIGS.keySet().forEach(config -> Bedwars.LOGGER.info(config.toString()));
            }
        });
    }

    private static Identifier identifierFromPath(Identifier location) {
        String fullPath = location.getPath();
        fullPath = fullPath.substring((path +"/").length(), fullPath.length() - ".json".length());
        return new Identifier(location.getNamespace(), fullPath);
    }
}
