package fr.delta.bedwars.game.player;

import com.google.common.collect.Multimap;
import fr.delta.bedwars.codec.BedwarsConfig;
import fr.delta.bedwars.data.AdditionalDataLoader;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.behaviour.DeathManager;
import fr.delta.bedwars.game.teamComponent.Forge;
import fr.delta.bedwars.mixin.PlayerInventoryAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.*;

public class InventoryManager
{
    public static class Managers
    {
        PlayerArmorManager armorManager;
        List<ToolManager> toolManagers;
        public Managers(PlayerArmorManager armorManager, List<ToolManager> toolManagers)
        {
            this.armorManager = armorManager;
            this.toolManagers = toolManagers;
        }
    }
    private final DeathManager deathManager;
    private final Map<PlayerRef, Managers> playerManagerMap = new Object2ObjectArrayMap<>();
    private final BedwarsActive game;
    private final Set<Item> lootable;

    public InventoryManager(DeathManager manager, BedwarsActive game, BedwarsConfig config, GameActivity activity)
    {
        this.deathManager = manager;
        this.game = game;
        this.lootable = getLootable(config);
        activity.listen(BedwarsEvents.PLAYER_DEATH, this::onPlayerDeath);
        activity.listen(BedwarsEvents.PLAYER_RESPAWN, this::onPlayerRespawn);
        ToolManager.init(activity);
    }

    public Set<Item> getLootable(BedwarsConfig config)
    {
        var items = new HashSet<Item>();
        var forgeConfig = AdditionalDataLoader.FORGE_CONFIG_REGISTRY.get(config.forgeConfigId());
        if(forgeConfig != null) //silently fail if forge config is not found, because the error be thrown here
            items.addAll(Forge.Tier.itemsSpawned(forgeConfig));
        for(var generatorType : config.generatorTypeIdList())
        {
            var generatorBuilder = AdditionalDataLoader.GENERATOR_TYPE_REGISTRY.get(generatorType);
            if(generatorBuilder != null)
                items.add(generatorBuilder.getItem());
        }
        return items;
    }

    public void init(Multimap<GameTeam, PlayerRef> teamPlayersMap)
    {
        for(var team : teamPlayersMap.keySet())
        {
            game.getPlayersInTeam(team).forEach(player -> playerManagerMap.put(PlayerRef.of(player), new Managers(new PlayerArmorManager(team.config().blockDyeColor(), game.getTeamComponentsFor(player).enchantments), new ArrayList<>())));
        }
    }

    List<ItemStack> generateDrop(ServerPlayerEntity player)
    {
        var loots = new ArrayList<ItemStack>();
        var inventory = player.getInventory();
        for(var Lists : ((PlayerInventoryAccessor)inventory).getCombinedInventory())
        {
            for(int i = 0; i < Lists.size(); i++)
            {
                var stack = Lists.get(i);
                if(lootable.contains(stack.getItem()))
                {
                    loots.add(stack);
                    Lists.set(i, ItemStack.EMPTY);
                }
            }
        }
        return loots;
    }

    private void onPlayerDeath(ServerPlayerEntity player, DamageSource source, ServerPlayerEntity killer, boolean isFinal)
    {

        if(killer != null && deathManager.isAlive(killer))
        {
            var loots = generateDrop(player);
            for(var item : loots)
                killer.getInventory().offerOrDrop(item);
        }
        else if(!source.equals(player.getDamageSources().outOfWorld()) && !isFinal)
        {
            var loots = generateDrop(player);
            //todo: add a fancy message that says how many items the player earned
            for(var item : loots)
            {
                player.dropStack(item);
            }
        }
        if(isFinal)
        {
            playerManagerMap.get(PlayerRef.of(player)).toolManagers.forEach((tool) -> tool.removeTool(player));
            game.getDefaultSwordManager().removeDefaultSword(player);
            var forge =game.getTeamComponentsFor(player).forge;
            var world = forge.getWorld();
            for(var stack : player.getInventory().main)
            {
                world.spawnEntity(new ItemEntity(world, forge.getCenter().getX(), forge.getCenter().getY(), forge.getCenter().getZ(), stack));
            }
            for(var stack : player.getInventory().main)
            {
                world.spawnEntity(new ItemEntity(world, forge.getCenter().getX(), forge.getCenter().getY(), forge.getCenter().getZ(), stack));
            }
            for(var stack : player.getEnderChestInventory().stacks)
            {
                world.spawnEntity(new ItemEntity(world, forge.getCenter().getX(), forge.getCenter().getY(), forge.getCenter().getZ(), stack));
            }
            playerManagerMap.remove(PlayerRef.of(player));
        }
        else
        {
            player.getInventory().clear();
            playerManagerMap.get(PlayerRef.of(player)).toolManagers.forEach(ToolManager::decrementTier);
        }
    }

    private void onPlayerRespawn(ServerPlayerEntity player)
    {
        var managers= playerManagerMap.get(PlayerRef.of(player));
        managers.armorManager.updateArmor(player);
        managers.toolManagers.forEach(toolManager -> player.getInventory().offerOrDrop(toolManager.createTool()));
    }

    public PlayerArmorManager getArmorManager(ServerPlayerEntity player)
    {
        return playerManagerMap.get(PlayerRef.of(player)).armorManager;
    }

    public List<ToolManager> getToolManagers(PlayerRef player)
    {
        return playerManagerMap.get(player).toolManagers;
    }
}
