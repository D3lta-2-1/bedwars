package me.verya.bedwars.game.player;

import com.google.common.collect.Multimap;
import me.verya.bedwars.game.behavior.DeathManager;
import me.verya.bedwars.game.event.BedwarsEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;

import java.util.*;

public class InventoryManager
{
    final private DeathManager deathManager;
    final Map<ServerPlayerEntity, PlayerArmor> playerArmorMap;
    static final List<ItemStack> lootable = new ArrayList<>(Arrays.asList(
            new ItemStack(Items.IRON_INGOT),
            new ItemStack(Items.GOLD_INGOT),
            new ItemStack(Items.EMERALD),
            new ItemStack(Items.DIAMOND)
    ));

    public InventoryManager(DeathManager manager, Multimap<GameTeam, ServerPlayerEntity> teamPlayersMap, GameActivity activity)
    {
        this.deathManager = manager;
        this.playerArmorMap = new HashMap<>();
        for(var entry : teamPlayersMap.entries())
        {
            playerArmorMap.put(entry.getValue(), new PlayerArmor(entry.getValue(), entry.getKey()));
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
        //if the player is eliminated
        if(isFinal)
        {
            playerArmorMap.remove(player); 
        }
    }

    private void onPlayerRespawn(ServerPlayerEntity player)
    {
        playerArmorMap.get(player).updateArmor();
    }
}
