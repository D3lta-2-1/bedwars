package fr.delta.bedwars.custom.items;

import eu.pb4.polymer.core.api.item.PolymerItem;
import fr.delta.bedwars.BedwarsActiveTracker;
import fr.delta.bedwars.custom.entities.BridgeEggEntity;
import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.util.ColoredBlocks;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class BridgeEgg extends Item implements PolymerItem {

    public BridgeEgg(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!world.isClient)
        {
            var game = getGame(user);
            var eggEntity = new BridgeEggEntity(world, user, getBlock(user, game).getDefaultState(), game);
            eggEntity.setItem(itemStack);
            eggEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
            world.spawnEntity(eggEntity);
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }

    private Block getBlock(PlayerEntity player, BedwarsActive game)
    {
        if(game == null) return Blocks.WHITE_WOOL;
        var team = game.getTeamForPlayer(PlayerRef.of(player));
        if (team == null) return Blocks.WHITE_WOOL;
        return ColoredBlocks.wool(team.config().blockDyeColor());
    }

    private BedwarsActive getGame(PlayerEntity player)
    {
        var space = GameSpaceManager.get().byPlayer(player);
        if(space == null) return null;
        return BedwarsActiveTracker.getInstance().getGame(space);
    }
    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.EGG;
    }
}
