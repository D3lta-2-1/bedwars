package fr.delta.bedwars.game.map.resourceGenerator;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.game.behaviour.ClaimManager;
import net.minecraft.block.Block;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import java.util.List;

public class MiddleGenerator {

    private final Vec3d pos;

    private final List<Integer> timeToGenerateTierList;
    int currentTier = 0;
    long lastSpawnItemTime;
    private final Item spawnedItem;
    private final World world;
    private final BlockDisplayElement blockElement;
    private final TextDisplayElement typeText;
    private final TextDisplayElement countText;
    private final TextDisplayElement tierText;
    private final SinusAnimation rotationAnimation;
    private final SinusAnimation YAnimation;
    private ElementHolder holder = null;
    public MiddleGenerator(BlockBounds bounds, Item spawnedItem, int rgbColor, Block display, World world, List<Integer> timeToGenerateTierList, ClaimManager claimManager, GameActivity activity)
    {
        this.pos = bounds.centerBottom().add(0, 3.5,0);
        this.spawnedItem = spawnedItem;
        this.world = world;
        this.timeToGenerateTierList = timeToGenerateTierList;
        this.blockElement = new BlockDisplayElement(display.getDefaultState());
        this.tierText = createText(getTierText(), 1.85f);
        this.lastSpawnItemTime = world.getTime();
        this.typeText = createText(Text.translatable(spawnedItem.getTranslationKey()).setStyle(Style.EMPTY.withColor(rgbColor)), 1.55f);
        this.countText = createText(getCountText(lastSpawnItemTime + timeToGenerateTierList.get(currentTier) - world.getTime()), 1.25f);
        this.rotationAnimation = new SinusAnimation(Math.PI * 2, 300, world.getTime());
        this.YAnimation = new SinusAnimation(0.125, 300, world.getTime());
        claimManager.addRegion(bounds);
        activity.listen(GameActivityEvents.TICK, this::tick);
    }

    private TextDisplayElement createText(Text text, float y) {
        var textElement = new TextDisplayElement(text);
        textElement.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        textElement.setTransformation(new Matrix4f().translate(0, y, 0));
        return textElement;
    }

    private Text getTierText()
    {
        return Text.translatable("generator.bedwars.tier").formatted(Formatting.YELLOW).append(Text.translatable("generator.bedwars." + currentTier).formatted(Formatting.RED));
    }

    private Text getCountText(long time)
    {
        return TextUtilities.concatenate(Text.translatable("generator.bedwars.spawnIn").formatted(Formatting.YELLOW),
                Text.literal(String.valueOf(time / 20)).formatted(Formatting.RED),
                Text.translatable("generator.bedwars.seconds").formatted(Formatting.YELLOW));
    }

    void tick()
    {
        // restore the block if it was unloaded
        if(holder == null)
        {
            holder = new ElementHolder();
            holder.addElement(blockElement);
            holder.addElement(tierText);
            holder.addElement(typeText);
            holder.addElement(countText);
            var blockPos = new BlockPos(asInteger(pos.getX()), asInteger(pos.getY()), asInteger(pos.getZ()));
            new ChunkAttachment(this.holder, world.getWorldChunk(blockPos), pos, true);
        }
        // animate the block
        blockElement.setTransformation(new Matrix4f()
                .rotateY(rotationAnimation.get(world.getTime()))
                .translate(-0.5f, 0, -0.5f));
        blockElement.setOffset(new Vec3d(0, YAnimation.get(world.getTime()), 0));
        blockElement.setInterpolationDuration(1);
        blockElement.startInterpolation();
        // update the text counter
        var timeBeforeNextSpawn = lastSpawnItemTime + timeToGenerateTierList.get(currentTier) - world.getTime();
        if(timeBeforeNextSpawn <= 0)
        {
            spawnItem();
            tierText.setText(getTierText());
        }
        if(timeBeforeNextSpawn % 20 == 0)
        {
            countText.setText(getCountText(timeBeforeNextSpawn));
        }
    }

    private void spawnItem()
    {
        var itemStack = new ItemStack(this.spawnedItem);
        var itemEntity = new ItemEntity(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), itemStack, 0,0,0);
        this.world.spawnEntity(itemEntity);
        this.lastSpawnItemTime = this.world.getTime();
    }

    private int asInteger(double value) {
        return (int) Math.floor(value);
    }

    public record SinusAnimation(double amplitude, int fullPeriodTime, long startTime) {
        public float get(long time) {
            time -= this.startTime;
            time %= this.fullPeriodTime;
            double timeInPeriod = (time * 2 * Math.PI) / fullPeriodTime;
            return (float) (this.amplitude * Math.sin(timeInPeriod));
        }
    }
}
