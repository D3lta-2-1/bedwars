package me.verya.bedwars.game.component;

import me.verya.bedwars.game.behavior.ClaimManager;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.stimuli.event.item.ItemPickupEvent;

public class Forge {
    static final String SPLITTABLE_KEY = "splittable";
    final BlockBounds bounds;
    final ServerWorld world;
    final TeamManager teamManager;

    //default time in tick, set in the config
    double timeBeforeNextIronSpawn;
    double timeBeforeNextGoldSpawn;
    double timeBeforeNextEmeraldSpawn;
    final ForgeConfig config;

    int tier; // 0 to 4, that correspond to 50, 100, 150, 200% faster resources

    public Forge(BlockBounds bounds, ClaimManager claim, ForgeConfig config, ServerWorld world, TeamManager teamManager, GameActivity activity)
    {
        this.bounds = bounds;
        this.world = world;
        this.teamManager = teamManager;
        this.config = config;
        this.tier = 0;
        timeBeforeNextIronSpawn = 0;
        timeBeforeNextGoldSpawn = 0;
        timeBeforeNextEmeraldSpawn = 0;

        claim.addRegion(this.bounds);
        activity.listen(GameActivityEvents.TICK, this::tick);
        activity.listen(ItemPickupEvent.EVENT, this::onPickupItem);
    }

    //utilities for spawning new items
    private double X(){ return bounds.center().x; }
    private double Y(){ return bounds.center().y; }
    private double Z(){ return bounds.center().z; }
    private ItemStack getSplittableIronIngot()
    {
        var ingot = new ItemStack(Items.IRON_INGOT);
        var nbt = new NbtCompound();
        nbt.putBoolean(SPLITTABLE_KEY, true);
        ingot.setNbt(nbt);
        return ingot;
    }

    private ItemStack getSplittableGoldIngot()
    {
        var ingot = new ItemStack(Items.GOLD_INGOT);
        var nbt = new NbtCompound();
        nbt.putBoolean(SPLITTABLE_KEY, true);
        ingot.setNbt(nbt);
        return ingot;
    }

    private ItemStack getSplittableEmerald()
    {
        var emerald = new ItemStack(Items.EMERALD);
        var nbt = new NbtCompound();
        nbt.putBoolean(SPLITTABLE_KEY, true);
        emerald.setNbt(nbt);
        return emerald;

    }

    private double getTimeToWaitForTier(double time, int tier)
    {
        var speedupPercent = 1  + tier * 0.5;
        return time / speedupPercent;
    }

    private boolean isFullOf(Item item, int maxCount)
    {
        int count = 0;
        var items = world.getEntitiesByClass(ItemEntity.class, bounds.asBox(), (itemEntity) -> {
            var stack = itemEntity.getStack();
            if(!stack.getItem().equals(item)) return false;
            if(!stack.hasNbt()) return false;
            var nbt = stack.getNbt();
            return (nbt.contains(SPLITTABLE_KEY) && nbt.getBoolean(SPLITTABLE_KEY));
        });

        for(var itemEntity :items)
        {
            count += itemEntity.getStack().getCount();
        }

        return count >= maxCount;
    }

    private void tickIron()
    {
        if(timeBeforeNextIronSpawn <= 0)
        {
            if(!isFullOf(Items.IRON_INGOT, 48))
                this.world.spawnEntity(new ItemEntity(world, X(), Y(), Z(), getSplittableIronIngot(), 0,0,0));
            timeBeforeNextIronSpawn += getTimeToWaitForTier(config.ironSpawnTime(), tier);
        }
        timeBeforeNextIronSpawn--;
    }

    private void tickGold()
    {
        if(timeBeforeNextGoldSpawn <= 0)
        {
            if(!isFullOf(Items.GOLD_INGOT, 16))
                this.world.spawnEntity(new ItemEntity(world, X(), Y(), Z(), getSplittableGoldIngot(),0,0,0));
            timeBeforeNextGoldSpawn += getTimeToWaitForTier(config.goldSpawnTime(), tier);
        }
        timeBeforeNextGoldSpawn--;
    }

    private void tickEmerald()
    {
        if(tier < 2) return;
        if(timeBeforeNextEmeraldSpawn <= 0)
        {
            this.world.spawnEntity(new ItemEntity(world, X(), Y(), Z(), getSplittableEmerald(),0,0,0));
            timeBeforeNextEmeraldSpawn += getTimeToWaitForTier(config.emeraldSpawnTime(), tier - 3); //to only get 50% faster when tier 4
        }
        timeBeforeNextEmeraldSpawn--;
    }

    public void tick()
    {
        tickIron();
        tickGold();
        tickEmerald();
    }

    private ActionResult onPickupItem(ServerPlayerEntity player, ItemEntity entity, ItemStack stack)
    {
        var forgeBox = bounds.asBox();
        //check if the item was picked up in this forge
        if(!player.getBoundingBox().intersects(forgeBox)) return ActionResult.PASS;
        if(!stack.hasNbt()) return ActionResult.PASS;
        //remove the splittable_tag
        var nbt = stack.getNbt();
        if(!nbt.contains(SPLITTABLE_KEY) || !nbt.getBoolean(SPLITTABLE_KEY) ) return ActionResult.PASS;
        stack.removeSubNbt(SPLITTABLE_KEY);
        //get teamMates
        var team = teamManager.teamFor(player);
        if(team == null) return ActionResult.PASS;
        for(var teamMate : teamManager.playersIn(team) )
        {
            if(teamMate.equals(player)) continue;
            var teamMateBox = teamMate.getBoundingBox();
            if(forgeBox.intersects(teamMateBox))
            {
                teamMate.giveItemStack(stack.copy());
            }

        }
        return ActionResult.PASS;
    }
}
