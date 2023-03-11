package fr.delta.bedwars.game.behavior;

import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.event.ItemThrowEvent;
import fr.delta.notasword.item.OldSwords;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.stimuli.event.item.ItemPickupEvent;
import xyz.nucleoid.stimuli.event.player.PlayerInventoryActionEvent;

public class DefaultSword {
    static Item defaultSword = OldSwords.WOODEN_SWORD;

    public DefaultSword(GameActivity activity)
    {
        activity.listen(BedwarsEvents.PLAYER_RESPAWN, this::giveDefaultSword);
        activity.listen(PlayerInventoryActionEvent.EVENT, this::onInventoryAction);
        activity.listen(ItemPickupEvent.EVENT, this::onPickupItem);
        activity.listen(ItemThrowEvent.AFTER,this::afterThrowItem);
        activity.listen(xyz.nucleoid.stimuli.event.item.ItemThrowEvent.EVENT, this::onThrowItem);
    }

    public void remove(ServerPlayerEntity player)
    {
        var stackList= player.getInventory().main;
        for(int i =0; i < stackList.size(); i++)
        {
            if(stackList.get(i).isOf(defaultSword))
                stackList.set(i, ItemStack.EMPTY);
        }
    }

    boolean dontHaveASword(ServerPlayerEntity player)
    {
        var stackList= player.getInventory().main;
        for(var stack : stackList)
        {
            if(stack.getItem() instanceof SwordItem)
                return false;
        }
        return true;
    }


    void giveDefaultSword(ServerPlayerEntity player)
    {
        player.getInventory().offerOrDrop(ItemStackBuilder.of(defaultSword).setUnbreakable().build());
    }
    ActionResult onInventoryAction(ServerPlayerEntity player, int slot, SlotActionType actionType, int button)
    {
        //Todo: protect chest
        return ActionResult.PASS;
    }

    ActionResult onPickupItem(ServerPlayerEntity player, ItemEntity entity, ItemStack stack)
    {
        if(stack.getItem() instanceof SwordItem)
        {
            this.remove(player);
        }
        return ActionResult.PASS;
    }

    ActionResult onThrowItem(ServerPlayerEntity player, int slot, ItemStack stack)
    {
        if(stack == null) return ActionResult.PASS;
        if(stack.isOf(defaultSword))
        {
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    void afterThrowItem(ServerPlayerEntity player, ItemStack stack)
    {
        if(stack.getItem() instanceof SwordItem && dontHaveASword(player)) {
            player.getInventory().offerOrDrop(ItemStackBuilder.of(defaultSword).setUnbreakable().build());
        }
    }

}
