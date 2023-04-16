package fr.delta.bedwars.game.player;

import com.google.common.collect.Multimap;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.behaviour.DeathManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
    final private DeathManager deathManager;
    final Map<PlayerRef, Managers> playerManagerMap = new Object2ObjectArrayMap<>();
    final private BedwarsActive game;
    static final List<ItemStack> lootable = new ArrayList<>(Arrays.asList(
            new ItemStack(Items.IRON_INGOT),
            new ItemStack(Items.GOLD_INGOT),
            new ItemStack(Items.EMERALD),
            new ItemStack(Items.DIAMOND) //todo: could be added to the config
    ));

    public InventoryManager(DeathManager manager, BedwarsActive game, GameActivity activity)
    {
        this.deathManager = manager;
        this.game = game;
        activity.listen(BedwarsEvents.PLAYER_DEATH, this::onPlayerDeath);
        activity.listen(BedwarsEvents.PLAYER_RESPAWN, this::onPlayerRespawn);
        ToolManager.init(activity);
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
        var itemList = new ArrayList<ItemStack>();
        var inventory = player.getInventory();
        for(var lootableStack : lootable)
        {
            while(inventory.contains(lootableStack)){
                var index = inventory.getSlotWithStack(lootableStack);
                inventory.removeStack(index);
                itemList.add(inventory.removeStack(index));
            }
        }
        return itemList;
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
