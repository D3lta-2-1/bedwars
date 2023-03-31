package fr.delta.bedwars.game.map.resourceGenerator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.behaviour.ClaimManager;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;

import java.util.List;

public class GeneratorBuilder {

    public static Codec<Integer> RGB_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("red").forGetter(color -> (color >> 16) & 0xFF),
                    Codec.INT.fieldOf("green").forGetter(color -> (color >> 8) & 0xFF),
                    Codec.INT.fieldOf("blue").forGetter(color -> color & 0xFF)
            ).apply(instance, (red, green, blue) -> (red << 16) + (green << 8) + blue)
    );

    public static final Codec<GeneratorBuilder> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("region_name").forGetter(GeneratorBuilder::getInternal_id),
                    Registries.ITEM.getCodec().fieldOf("item").forGetter(GeneratorBuilder::getItem),
                    RGB_CODEC.fieldOf("rgb_color").forGetter(GeneratorBuilder::getRgbColor),
                    Codec.INT.listOf().fieldOf("time_to_generate_tiers").forGetter(GeneratorBuilder::getTimeToGenerateTierList),
                    Registries.BLOCK.getCodec().fieldOf("display_block").forGetter(GeneratorBuilder::getDisplayBlock)
            ).apply(instance, GeneratorBuilder::new)
    );
    private final String internal_id;
    private final Item item;
    private final List<Integer> timeToGenerateTierList;
    private final Block displayBlock;
    private final int rgbColor;

    public GeneratorBuilder(String name, Item item, int rgbColor, List<Integer> timeToGenerateTierList, Block displayBlock) {
        this.internal_id = name;
        this.item = item;
        this.rgbColor = rgbColor;
        this.timeToGenerateTierList = timeToGenerateTierList;
        this.displayBlock = displayBlock;
    }

    public String getInternal_id() {
        return internal_id;
    }

    public Item getItem() {
        return item;
    }

    public List<Integer> getTimeToGenerateTierList() {
        return timeToGenerateTierList;
    }

    public Block getDisplayBlock() {
        return displayBlock;
    }

    public int getRgbColor() {
        return rgbColor;
    }

    public void createGenerator(BlockBounds bounds, World world, ClaimManager claimManager, GameActivity activity) {
        new MiddleGenerator(bounds, this.item, rgbColor, this.displayBlock, world, this.timeToGenerateTierList, claimManager, activity);
    }
}
