package fr.delta.bedwars.game.player;

import com.google.common.collect.Multimap;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.behaviour.DeathManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;

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
    final Map<ServerPlayerEntity, Managers> playerManagerMap;
    static final List<ItemStack> lootable = new ArrayList<>(Arrays.asList(
            new ItemStack(Items.IRON_INGOT),
            new ItemStack(Items.GOLD_INGOT),
            new ItemStack(Items.EMERALD),
            new ItemStack(Items.DIAMOND) //todo: could be added to the config
    ));

    public InventoryManager(DeathManager manager, Multimap<GameTeam, ServerPlayerEntity> teamPlayersMap, GameActivity activity)
    {
        this.deathManager = manager;
        this.playerManagerMap = new HashMap<>();
        for(var entry : teamPlayersMap.entries())
        {
            playerManagerMap.put(entry.getValue(), new Managers(new PlayerArmorManager(entry.getValue(), entry.getKey()), new ArrayList<>()));
        }
        activity.listen(BedwarsEvents.PLAYER_DEATH, this::onPlayerDeath);
        activity.listen(BedwarsEvents.PLAYER_RESPAWN, this::onPlayerRespawn);
    }

    List<ItemStack> generateDrop(ServerPlayerEntity player)
    {
        var itemList = new ArrayList<ItemStack>();
        var inventory = player.getInventory();
        for(var lootableStack : lootable)
        {
            while(inventory.contains(lootableStack)){
                var index = inventory.getSlotWithStack(lootableStack);
                itemList.add(inventory.removeStack(index));
            }
        }
        return itemList;
    }

    private void onPlayerDeath(ServerPlayerEntity player, DamageSource source, ServerPlayerEntity killer, boolean isFinal)
    {
        var loots = generateDrop(player);
        if(killer != null && deathManager.isAlive(killer))
        {
            for(var item : loots)
                killer.getInventory().offerOrDrop(item);
        }
        else if(!source.equals(DamageSource.OUT_OF_WORLD))
        {
            for(var item : loots)
            {
                player.dropStack(item);
            }
        }
        player.getInventory().clear();
        playerManagerMap.get(player).toolManagers.forEach(ToolManager::decrementTier);
        //if the player is eliminated
        if(isFinal)
        {
            playerManagerMap.remove(player);
        }
    }

    private void onPlayerRespawn(ServerPlayerEntity player)
    {
        var managers= playerManagerMap.get(player);
        managers.armorManager.updateArmor();
        managers.toolManagers.forEach(toolManager -> player.getInventory().offerOrDrop(toolManager.createTool()));
    }

    public PlayerArmorManager getArmorManager(ServerPlayerEntity player)
    {
        return playerManagerMap.get(player).armorManager;
    }

    public List<ToolManager> getToolManagers(ServerPlayerEntity player)
    {
        return playerManagerMap.get(player).toolManagers;
    }
}
