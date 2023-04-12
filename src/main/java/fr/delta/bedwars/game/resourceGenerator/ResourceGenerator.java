package fr.delta.bedwars.game.resourceGenerator;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import fr.delta.bedwars.Bedwars;
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
import org.joml.Vector3f;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import java.util.List;

public class ResourceGenerator {

    private final BlockBounds bounds;
    private final Vec3d pos;
    private final List<Integer> timeToGenerateTierList;
    int currentTier = 1;
    long lastSpawnItemTime;
    private final Item spawnedItem;
    private final World world;
    private final BlockDisplayElement blockElement;
    private final int maxItems;
    private final TextDisplayElement countText;
    private final TextDisplayElement tierText;
    private final SinusAnimation rotationAnimation;
    private final SinusAnimation YAnimation;

    private final int rgbColor;
    private final static float SCALE = 0.75f;



    public ResourceGenerator(BlockBounds bounds, Item spawnedItem, int rgbColor, Block display, World world, List<Integer> timeToGenerateTierList, ClaimManager claimManager, int maxItems, GameActivity activity)
    {
        this.bounds = bounds;
        this.pos = bounds.centerBottom().add(0, 3.5,0);
        this.spawnedItem = spawnedItem;
        this.world = world;
        this.timeToGenerateTierList = timeToGenerateTierList;
        this.blockElement = new BlockDisplayElement(display.getDefaultState());
        this.tierText = createText(getTierText(), 1.85f);
        this.lastSpawnItemTime = world.getTime();
        this.rgbColor = rgbColor;
        var typeText = createText(Text.translatable(spawnedItem.getTranslationKey()).setStyle(Style.EMPTY.withColor(rgbColor)), 1.55f);
        this.countText = createText(getCountText(lastSpawnItemTime + timeToGenerateTierList.get(currentTier) - world.getTime()), 1.25f);
        this.rotationAnimation = new SinusAnimation(Math.PI * 2, 300, world.getTime());
        this.YAnimation = new SinusAnimation(0.125, 300, world.getTime());
        this.maxItems = maxItems;
        claimManager.addRegion(bounds);
        activity.listen(GameActivityEvents.TICK, this::tick);

        //set up the virtual entity
        var holder = new ElementHolder();
        holder.addElement(blockElement);
        holder.addElement(tierText);
        holder.addElement(typeText);
        holder.addElement(countText);
        new ChunkAttachment(holder, world.getWorldChunk(asBlockPos(pos)), pos, true);
    }

    private TextDisplayElement createText(Text text, float y) {
        var textElement = new TextDisplayElement(text);
        textElement.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        textElement.setTranslation(new Vector3f(0, y, 0));
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

    public Item getSpawnedItem() {
        return spawnedItem;
    }

    public int getRgbColor() {
        return rgbColor;
    }

    public void setTier(int tier) {
        if(tier - 1< timeToGenerateTierList.size() && tier  >= 1)
        {
            currentTier = tier;
            tierText.setText(getTierText());
        }
        else
            Bedwars.LOGGER.warn("Invalid tier for middle generator: " + tier + ", ignoring...");
    }

    void tick()
    {
        // update the text counter
        var timeBeforeNextSpawn = lastSpawnItemTime + timeToGenerateTierList.get(currentTier - 1) - world.getTime();
        if(timeBeforeNextSpawn % 20 == 0)
        {
            countText.setText(getCountText(timeBeforeNextSpawn));
        }
        if(timeBeforeNextSpawn <= 0)
        {
            spawnItem();
            tierText.setText(getTierText());
        }
        // animate the block
        blockElement.setTransformation(new Matrix4f()
                .rotateY(rotationAnimation.get(world.getTime()))
                .translate(-0.5f * SCALE, 0, -0.5f * SCALE));
        blockElement.setScale(new Vector3f(SCALE, SCALE, SCALE));
        blockElement.setOffset(new Vec3d(0, YAnimation.get(world.getTime()), 0));
        blockElement.setInterpolationDuration(1);
        blockElement.startInterpolation();

    }

    private void spawnItem()
    {
        this.lastSpawnItemTime = this.world.getTime();
        if(isFullOf(this.spawnedItem, maxItems))
            return;
        var itemStack = new ItemStack(this.spawnedItem);
        var itemEntity = new ItemEntity(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), itemStack, 0,0,0);
        this.world.spawnEntity(itemEntity);
    }

    private boolean isFullOf(Item item, int maxCount)
    {
        int count = 0;
        var items = world.getEntitiesByClass(ItemEntity.class, bounds.asBox(), (itemEntity) -> {
            var stack = itemEntity.getStack();
            return stack.getItem().equals(item);
        });
        for(var itemEntity :items)
        {
            count += itemEntity.getStack().getCount();
        }
        return count >= maxCount;
    }

    private static BlockPos asBlockPos(Vec3d vec) {
        return new BlockPos((int)vec.getX(), (int)vec.getY(), (int)vec.getZ());
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
