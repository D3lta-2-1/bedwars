package fr.delta.bedwars.game.teamComponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.behaviour.ClaimManager;
import fr.delta.bedwars.game.shop.entries.ShopEntry;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.stimuli.event.item.ItemPickupEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Forge {

    public record SpawnData(int spawnTime, int maxInForge, boolean splittable)
    {
        public static final Codec<SpawnData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("spawnTime").forGetter(SpawnData::spawnTime),
                Codec.INT.fieldOf("maxInForge").forGetter(SpawnData::maxInForge),
                Codec.BOOL.optionalFieldOf("splittable", true).forGetter(SpawnData::splittable)
        ).apply(instance, SpawnData::new));
    }

    public record Tier(ShopEntry.Cost cost, String nameKey, String descriptionKey, Map<Item, SpawnData> itemsToSpawn)
    {
        public static final Codec<Tier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ShopEntry.Cost.CODEC.optionalFieldOf("cost", new ShopEntry.Cost(Items.DIAMOND, 4)).forGetter(Tier::cost),
                Codec.STRING.optionalFieldOf("name_key", null).forGetter(Tier::descriptionKey),
                Codec.STRING.optionalFieldOf("description_key", null).forGetter(Tier::descriptionKey),
                Codec.unboundedMap(Registries.ITEM.getCodec(), SpawnData.CODEC).fieldOf("items_to_spawn").forGetter(Tier::itemsToSpawn)
        ).apply(instance, Tier::new));
    }

    static public List<Tier> defaulted()
    {
        return List.of(
                new Tier(null,
                        null,
                        null,
                        Map.of(
                                Items.IRON_INGOT, new SpawnData(100, 64, true),
                                Items.GOLD_INGOT, new SpawnData(200, 16, true)
                )),
                new Tier(new ShopEntry.Cost(Items.DIAMOND, 2),
                        "shop.bedwars.ironForge",
                        null,
                        Map.of(
                                Items.IRON_INGOT, new SpawnData(65, 96, true),
                                Items.GOLD_INGOT, new SpawnData(134, 20, true)
                )),
                new Tier(new ShopEntry.Cost(Items.DIAMOND, 4),
                        "shop.bedwars.goldenForge",
                        null,
                        Map.of(
                                Items.IRON_INGOT, new SpawnData(50, 128, true),
                                Items.GOLD_INGOT, new SpawnData(100, 24, true)
                )),
                new Tier(new ShopEntry.Cost(Items.DIAMOND, 8),
                        null,
                        "shop.bedwars.emerald",
                        Map.of(
                                Items.IRON_INGOT, new SpawnData(50, 128, true),
                                Items.GOLD_INGOT, new SpawnData(100, 24, true),
                                Items.EMERALD, new SpawnData(1200, 4, false)
                )),
                new Tier(new ShopEntry.Cost(Items.DIAMOND, 16),
                        null,
                        "shop.bedwars.moltenForge",
                        Map.of(
                                Items.IRON_INGOT, new SpawnData(40, 96, true),
                                Items.GOLD_INGOT, new SpawnData(80, 32, true),
                                Items.EMERALD, new SpawnData(600, 4, false)
                        ))
        );
    }


    public static final Codec<List<Tier>> CODEC = Codec.list(Tier.CODEC);
    private static final String SPLITTABLE_KEY = "splittable";
    private final BlockBounds bounds;
    private final ServerWorld world;
    private final TeamManager teamManager;
    private final List<Tier> config;
    private Map<Item, SpawnData> itemSpawnData;
    private final  Map<Item, Long> lastSpawnTime = new HashMap<>();
    int concurrentTier;

    public Forge(BlockBounds bounds, ClaimManager claim, List<Tier> config, ServerWorld world, TeamManager teamManager, GameActivity activity)
    {
        this.bounds = bounds;
        this.world = world;
        this.teamManager = teamManager;
        this.config = config;
        this.setTier(0);
        claim.addRegion(this.bounds);
        activity.listen(GameActivityEvents.TICK, this::tick);
        activity.listen(ItemPickupEvent.EVENT, this::onPickupItem);
    }

    public void setTier(int tier)
    {
        concurrentTier = tier;
        itemSpawnData = config.get(tier).itemsToSpawn();
        lastSpawnTime.clear();
        var concurrentTime = world.getTime();
        for(var item : itemSpawnData.keySet())
        {
            lastSpawnTime.put(item, concurrentTime);
            this.world.spawnEntity(new ItemEntity(world, X(), Y(), Z(), getStack(item, itemSpawnData.get(item).splittable()),0,0,0));
        }
    }

    public Tier getTier()
    {
        return config.get(concurrentTier);
    }
    public int getNextTierInt()
    {
        if(concurrentTier + 1 >= config.size())
            return concurrentTier;
        return concurrentTier + 1;
    }

    public void upgrade()
    {
        if(concurrentTier + 1 >= config.size())
            return;
        this.setTier(concurrentTier + 1);
    }

    public boolean isMaxed()
    {
        return concurrentTier + 1 >= config.size();
    }

    public Tier getNextTier()
    {
        if(concurrentTier + 1 >= config.size())
            return null;
        return config.get(concurrentTier + 1);
    }

    public Vec3d getCenter()
    {
        return bounds.center();
    }

    public ServerWorld getWorld()
    {
        return world;
    }

    //utilities for spawning new items
    private double X(){ return bounds.center().x; }
    private double Y(){ return bounds.center().y; }
    private double Z(){ return bounds.center().z; }
    private ItemStack getStack(Item item, boolean splittable)
    {
        var ingot = new ItemStack(item);
        if(splittable)
        {
            var nbt = new NbtCompound();
            nbt.putBoolean(SPLITTABLE_KEY, true);
            ingot.setNbt(nbt);
        }
        return ingot;
    }

    private boolean isFullOf(Item item, int maxCount)
    {
        int count = 0;
        var items = world.getEntitiesByClass(ItemEntity.class, bounds.asBox(), (itemEntity) -> {
            var stack = itemEntity.getStack();
            if(!stack.getItem().equals(item)) return false;
            if(!stack.hasNbt()) return false;
            var nbt = stack.getNbt();
            assert nbt != null;
            return (nbt.contains(SPLITTABLE_KEY) && nbt.getBoolean(SPLITTABLE_KEY));
        });

        for(var itemEntity :items)
        {
            count += itemEntity.getStack().getCount();
        }

        return count >= maxCount;
    }

    public void tick()
    {
        var concurrentTime = world.getTime();
        for(var resource : itemSpawnData.entrySet())
        {
            var item = resource.getKey();
            var data = resource.getValue();
            if(lastSpawnTime.get(item) + data.spawnTime() <= concurrentTime)
            {
                if(!isFullOf(item, data.maxInForge()))
                    this.world.spawnEntity(new ItemEntity(world, X(), Y(), Z(), getStack(item, data.splittable()), 0,0,0));
                lastSpawnTime.put(item, concurrentTime);
            }
        }
    }

    private ActionResult onPickupItem(ServerPlayerEntity player, ItemEntity entity, ItemStack stack)
    {
        if(!stack.hasNbt()) return ActionResult.PASS;
        //remove the splittable_tag
        var nbt = stack.getNbt();
        assert nbt != null;
        if(!nbt.contains(SPLITTABLE_KEY) || !nbt.getBoolean(SPLITTABLE_KEY) ) return ActionResult.PASS;
        stack.setNbt(null);
        var forgeBox = bounds.asBox();
        //check if the item was picked up in this forge
        if(!player.getBoundingBox().intersects(forgeBox)) return ActionResult.PASS;
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
