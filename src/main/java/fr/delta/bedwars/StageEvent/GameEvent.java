package fr.delta.bedwars.StageEvent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

public interface GameEvent {
    TinyRegistry<Codec<? extends GameEvent>> REGISTRY = register();
    static private TinyRegistry<Codec<? extends GameEvent>> register() {
        TinyRegistry<Codec<? extends GameEvent>> registry = TinyRegistry.create();
        registry.register(new Identifier(Bedwars.ID, "change_generator_tier"), ChangeGeneratorTier.CODEC);
        return registry;
    }

    int getTimeToWait();
    Text getStageName(BedwarsActive game);
    void run(BedwarsActive game);

    record ChangeGeneratorTier(int time, String internalId, int tier) implements GameEvent {
        public static final Codec<ChangeGeneratorTier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("time").forGetter(ChangeGeneratorTier::time),
                Codec.STRING.fieldOf("region_name").forGetter(ChangeGeneratorTier::internalId),
                Codec.INT.fieldOf("tier").forGetter(ChangeGeneratorTier::tier)
        ).apply(instance, ChangeGeneratorTier::new));

        @Override
        public int getTimeToWait() {
            return time;
        }

        @Override
        public Text getStageName(BedwarsActive game) {
            var generator = game.getGeneratorsMap().get(internalId).stream().findAny();
            if(generator.isEmpty()) return Text.literal("! no generators with id name found !").formatted(Formatting.RED);
            return TextUtilities.concatenate(Text.translatable(generator.get().getSpawnedItem().getTranslationKey()),
                    TextUtilities.SPACE,
                    Text.translatable("generator.bedwars." + tier)
            );
        }

        @Override
        public void run(BedwarsActive game) {
            var generators = game.getGeneratorsMap().get(internalId);
            generators.forEach(generator -> generator.setTier(tier));
        }
    }
}
