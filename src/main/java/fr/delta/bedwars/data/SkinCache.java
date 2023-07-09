package fr.delta.bedwars.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.Bedwars;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import xyz.nucleoid.plasmid.Plasmid;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SkinCache {

    private static final Path PATH = Paths.get("bedwars/skin_cache.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Codec<Property> PROPERTY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(Property::getName),
                    Codec.STRING.fieldOf("value").forGetter(Property::getValue),
                    Codec.STRING.fieldOf("signature").forGetter(Property::getSignature)
            ).apply(instance, Property::new));

    public static final Codec<Map<Identifier, Property>> CODEC = Codec.unboundedMap(Identifier.CODEC, PROPERTY_CODEC);

    public static HashMap<Identifier, Property> load() {
        if (Files.exists(PATH)) {
            try (var input = Files.newInputStream(PATH)) {
                var json = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonObject();
                var dataResult = CODEC.decode(JsonOps.INSTANCE, json).map(Pair::getFirst);
                var result = dataResult.result();
                return result.map(HashMap::new).orElseGet(HashMap::new);
            } catch (IOException e) {
                Plasmid.LOGGER.warn("failed to load last cache", e);
            }
        }
        return new HashMap<>();
    }

    public static void save(Map<Identifier, Property> cache)
    {
        try {
            Path parentDir = PATH.getParent();
            if (!Files.exists(parentDir))
                Files.createDirectories(parentDir);

            var output = Files.newOutputStream(PATH);
            var result = CODEC.encodeStart(JsonOps.INSTANCE, cache).result();
            if (result.isPresent()) {
                var json = result.get();
                IOUtils.write(GSON.toJson(json), output, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            Bedwars.LOGGER.warn("failed to cache skins", e);
        }
    }
}
