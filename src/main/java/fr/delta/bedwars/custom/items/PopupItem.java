package fr.delta.bedwars.custom.items;

import com.google.common.collect.AbstractIterator;
import eu.pb4.polymer.core.api.item.PolymerItem;
import fr.delta.bedwars.BedwarsActiveTracker;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.behaviour.ClaimManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.util.ColoredBlocks;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.plasmid.util.Scheduler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Function;

public class PopupItem extends Item implements PolymerItem {

    private final Identifier template;
    private final Function<BlockBounds, Iterable<BlockPos>> getIterator;

    public static MapTemplate getTemplate(MinecraftServer server, Identifier structureId) throws IOException {
        var resourceManager = server.getResourceManager();
        var id = new Identifier(structureId.getNamespace(), "structure_templates/" + structureId.getPath() + ".nbt");
        var resource = resourceManager.getResource(id);

        if (resource.isEmpty()) {
            throw new IOException("No resource found for " + id);
        }

        return MapTemplateSerializer.loadFrom(resource.get().getInputStream());
    }

    public PopupItem(Settings settings, Identifier structureId, Function<BlockBounds, Iterable<BlockPos>> getIterator)
    {
        super(settings);
        this.template = structureId;
        this.getIterator = getIterator;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player)
    {
        return Items.CHEST;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context)
    {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();

        assert player != null;
        if (!player.getAbilities().allowModifyWorld || world.isClient()) {
            return ActionResult.PASS;
        }

        Direction facing = context.getSide();
        BlockPos centerPos = context.getBlockPos().offset(facing, 1);

        if (this.tryPlace(player, (ServerWorld) world, centerPos, context.getHorizontalPlayerFacing()))
        {

            if (!player.getAbilities().creativeMode) {
                context.getStack().decrement(1);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public void playSound(World world, PlayerEntity player, BlockPos pos)
    {
        world.playSound(player, pos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 1, 1);
    }

    private boolean tryPlace(PlayerEntity player, ServerWorld world, BlockPos pos, Direction facing)
    {
        var game = GameSpaceManager.get().byPlayer(player);
        var bedwarsActive = BedwarsActiveTracker.getInstance().getGame(game);

        if(isProtected(world, pos, bedwarsActive))
        {
            player.sendMessage(Text.translatable("warning.bedwars.cannotPlaceBlockHere").formatted(Formatting.RED));
            return false;
        }

        try {
            MapTemplate template = getTemplate(world.getServer(), this.template);
            switch (facing)
            {
                case NORTH:
                    break; // no rotation
                case EAST:
                    template = template.rotate(BlockRotation.CLOCKWISE_90);
                    break;
                case SOUTH:
                    template = template.rotate(BlockRotation.CLOCKWISE_180);
                    break;
                case WEST:
                    template = template.rotate(BlockRotation.COUNTERCLOCKWISE_90);
                    break;
            }
            this.placeTemplate(player, world, pos, template, bedwarsActive);
            return true;
        } catch (IOException e) {
            player.sendMessage(Text.literal(e.getMessage()).formatted(Formatting.RED));
            e.printStackTrace();
            return false;
        }
    }

    private void placeTemplate(PlayerEntity player, ServerWorld world, BlockPos pos, MapTemplate template, BedwarsActive bedwarsActive)
    {

        DyeColor color = null;
        if(bedwarsActive != null)
        {
            var team = bedwarsActive.getTeamForPlayer(PlayerRef.of(player));
            color = team.config().blockDyeColor();
        }

        var bounds = template.getBounds();
        var iterator = this.getIterator.apply(bounds).iterator();

        tryToPlaceNextBlock(iterator, template, world, pos, bedwarsActive, color, player, null, null);

    }

    public void tryToPlaceNextBlock(Iterator<BlockPos> iterator,
                                    MapTemplate template,
                                    ServerWorld world,
                                    BlockPos pos,
                                    BedwarsActive bedwarsActive,
                                    DyeColor color,
                                    PlayerEntity player,
                                    BlockState previousBlockState,
                                    BlockPos previousBlockPos)
    {
        //first, we place the block of the previous iteration
        if(tryPlaceAt(world, previousBlockPos, previousBlockState, bedwarsActive, color)) this.playSound(world, player, pos);

        BlockPos worldPos;
        BlockState blockState;

        do { //find the next block by skipping air blocks
            if(!iterator.hasNext()) return; //if there are no more blocks to place, we return
            var blockPos = iterator.next();
            worldPos = blockPos.add(pos);
            blockState = template.getBlockState(blockPos);
        } while (blockState.isAir());

        //push every entity at this location
        world.getEntitiesByClass(Entity.class, new Box(worldPos), ($) -> true).forEach(entity -> {
            var velocity = entity.getVelocity();
            entity.setVelocity(velocity.getX(), 0.5, velocity.getZ());
            if(entity instanceof ServerPlayerEntity serverPlayerEntity)
                serverPlayerEntity.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(entity));
            entity.velocityDirty = true;
        });

        //pass every thing to the next iteration
        BlockState finalBlockState = blockState;
        BlockPos finalWorldPos = worldPos;
        Scheduler.INSTANCE.submit((MinecraftServer $) -> tryToPlaceNextBlock(iterator, template, world, pos, bedwarsActive, color, player, finalBlockState, finalWorldPos), 1);
    }


    private boolean tryPlaceAt(ServerWorld world, BlockPos pos, BlockState state, BedwarsActive bedwarsActive, DyeColor color)
    {
        if(state !=null && pos != null && !isProtected(world, pos, bedwarsActive)) //set the block if possible
        {
            if(color != null && state.isOf(Blocks.WHITE_WOOL)) //change white wool to the team color
                state = ColoredBlocks.wool(color).getDefaultState();
            world.setBlockState(pos, state);
            return true;
        }
        return false;
    }

    static boolean isProtected(ServerWorld world, BlockPos pos, BedwarsActive bedwarsActive)
    {
        if(bedwarsActive != null)
        {
            if(!bedwarsActive.couldABlockBePlacedHere(pos))
            {
                return true;
            }
        }
        var state = world.getBlockState(pos);
        return !ClaimManager.isAirOrBreakableBlock(state);
    }

    public static Iterable<BlockPos> LinearIterable(BlockBounds bounds)
    {
        var min = bounds.min();
        var max = bounds.max();
        int sizeX = max.getX() - min.getX() + 1; //+1 because we want to include the max pos
        int sizeY = max.getY() - min.getY() + 1;
        int sizeZ = max.getZ() - min.getZ() + 1;
        int total = sizeX * sizeY * sizeZ;
        return () ->
                new AbstractIterator<>() {
                    private final BlockPos.Mutable pos = new BlockPos.Mutable();
                    private int index;

                    protected BlockPos computeNext() {
                        if (this.index == total) {
                            return this.endOfData();
                        } else {
                            int ix = this.index % sizeX;
                            int iz = this.index / sizeX % sizeZ;
                            int iy = this.index / sizeX / sizeZ;

                            ++this.index;
                            return this.pos.set(min.getX() + ix, min.getY() + iy, min.getZ() + iz);
                        }
                    }
                };
    }

    public static Iterable<BlockPos> CircularIterable(BlockBounds bounds)
    {
        var min = bounds.min();
        var max = bounds.max();
        int sizeX = max.getX() - min.getX() + 1; //+1 because we want to include the max pos
        int sizeY = max.getY() - min.getY() + 1;
        int sizeZ = max.getZ() - min.getZ() + 1;
        int totalLayer = sizeX * sizeZ;
        int total = totalLayer * sizeY;


        var listOfBlockPos = new ArrayList<BlockPos>(total);

        for(int iy = 0; iy < sizeY; iy++)
        {
            //add all the block pos of the layer
            var layerListOfBlockPos = new ArrayList<BlockPos>(sizeX * sizeZ);
            for(int i = 0; i < totalLayer; i++)
            {
                int ix = i % sizeX;
                int iz = i / sizeX % sizeZ;
                var x = min.getX() + ix;
                var y = min.getY() + iy;
                var z = min.getZ() + iz;

                layerListOfBlockPos.add(new BlockPos(x, y, z));
            }
            //sort the layer
            layerListOfBlockPos.sort((pos1, pos2) -> {
                var z1 = pos1.getX() * Math.tan(Math.sqrt(pos1.getX() * pos1.getX() + pos1.getZ() * pos1.getZ()));
                var z2 = pos2.getX() * Math.tan(Math.sqrt(pos2.getX() * pos2.getX() + pos2.getZ() * pos2.getZ()));
                return (int) (z1 - z2);
            });

            //add the layer to the list
            listOfBlockPos.addAll(layerListOfBlockPos);
        }

        return listOfBlockPos;
    }

}
