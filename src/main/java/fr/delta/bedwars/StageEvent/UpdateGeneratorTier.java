package fr.delta.bedwars.StageEvent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public record UpdateGeneratorTier(int time, String internalId, int tier) implements StageEvent {
    public static final Codec<UpdateGeneratorTier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("time").forGetter(UpdateGeneratorTier::time),
            Codec.STRING.fieldOf("region_name").forGetter(UpdateGeneratorTier::internalId),
            Codec.INT.fieldOf("tier").forGetter(UpdateGeneratorTier::tier)
    ).apply(instance, UpdateGeneratorTier::new));

    @Override
    public int getTimeToWait() {
        return time;
    }

    @Override
    public Text getStageName(BedwarsActive game) {
        var generator = game.getGeneratorsMap().get(internalId).stream().findAny();
        if(generator.isEmpty()) return Text.literal("! no generators with id name found !").formatted(Formatting.RED);
        return TextUtilities.concatenate(Text.translatable(generator.get().getSpawnedItem().getTranslationKey()), //may need to be changed
                TextUtilities.SPACE,
                Text.translatable("generator.bedwars." + tier)
        );
    }

    @Override
    public void run(BedwarsActive game) {
        var generators = game.getGeneratorsMap().get(internalId);
        generators.forEach(generator -> generator.setTier(tier));
        var generator = generators.stream().findAny();
        if(generator.isEmpty()) return;

        var itemText = Text.translatable(generator.get().getSpawnedItem().getTranslationKey()).setStyle(Style.EMPTY.withColor(generator.get().getRgbColor()));
        var tierText = Text.translatable("generator.bedwars.tier", Text.translatable("generator.bedwars." + tier).formatted(Formatting.RED)).formatted(Formatting.YELLOW);
        var text = Text.translatable("generator.bedwars.hasBeenUpdatedTo", itemText, tierText);
        game.getPlayers().sendMessage(text);
    }
}