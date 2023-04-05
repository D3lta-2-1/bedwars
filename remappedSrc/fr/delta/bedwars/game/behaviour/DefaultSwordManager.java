package fr.delta.bedwars.game.behaviour;

import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.event.ItemThrowEvent;
import fr.delta.bedwars.event.SlotInteractionEvent;
import fr.delta.notasword.item.OldSwords;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.stimuli.event.item.ItemPickupEvent;

import java.util.List;

public class DefaultSwordManager {
    static final Item defaultSword = OldSwords.WOODEN_SWORD;
    private final GameActivity activity;

    public DefaultSwordManager(GameActivity activity)
    {
        this.activity = activity;
        activity.listen(BedwarsEvents.PLAYER_RESPAWN, this::giveDefaultSword);
        activity.listen(SlotInteractionEvent.BEFORE, this::onInteract);
        activity.listen(SlotInteractionEvent.AFTER, ((player, handler, slotIndex, button, actionType) -> { updatePlayer(player, handler);
        return ActionResult.PASS;}));
        activity.listen(ItemPickupEvent.EVENT, this::onPickupItem);
        activity.listen(ItemThrowEvent.AFTER,this::afterThrowItem);
        activity.listen(xyz.nucleoid.stimuli.event.item.ItemThrowEvent.EVENT, this::onThrowItem);
        activity.listen(BedwarsEvents.IS_STACK_THROWABLE, (stack, player) -> stack.getItem() instanceof SwordItem ? ActionResult.FAIL : ActionResult.PASS);
    }

    public void removeDefaultSword(ServerPlayerEntity player)
    {
        var stackList= player.getInventory().main;
        for(int i =0; i < stackList.size(); i++)
        {
            if(stackList.get(i).isOf(defaultSword))
                stackList.set(i, ItemStack.EMPTY);
        }
    }

    public void updatePlayer(ServerPlayerEntity player, ScreenHandler handler)
    {
        var main= player.getInventory().main;
        var offHand= player.getInventory().offHand;
        int lastSwordIndex = -1; //should always refer a sword, if it's on -1 at the end, the player should get a sword
        List<ItemStack> container = main;

        for(int i =0; i < main.size(); i++)
        {
            if(main.get(i).getItem() instanceof SwordItem)
            {
                if(lastSwordIndex == -1) //first sword we find
                {
                    lastSwordIndex = i;
                }
                else if (main.get(i).isOf(defaultSword)) //if it's not the first sword, and it's a defaultSword
                {
                    main.set(i, ItemStack.EMPTY);
                }
                else if(container.get(lastSwordIndex).isOf(defaultSword))
                {
                    container.set(lastSwordIndex, ItemStack.EMPTY);
                    lastSwordIndex = i;
                }
            }
        }
        for(int i =0; i < offHand.size(); i++)
        {
            if(offHand.get(i).getItem() instanceof SwordItem)
            {
                if(lastSwordIndex == -1) //first sword we find
                {
                    lastSwordIndex = i;
                    container = offHand;
                }
                else if (offHand.get(i).isOf(defaultSword)) //if it's not the first sword, and it's a defaultSword
                {
                    offHand.set(i, ItemStack.EMPTY);
                }
                else if(container.get(lastSwordIndex).isOf(defaultSword))
                {
                    container.set(lastSwordIndex, ItemStack.EMPTY);
                    lastSwordIndex = i;
                    container = offHand;
                }
            }
        }

        if((handler != null && handler.getCursorStack().getItem() instanceof SwordItem))
        {

            if(lastSwordIndex != -1 && container.get(lastSwordIndex).isOf(defaultSword))
                container.set(lastSwordIndex, ItemStack.EMPTY);
            lastSwordIndex = -2;
        }
        if(lastSwordIndex == -1)
        {
            giveDefaultSword(player);
        }
    }


    void giveDefaultSword(ServerPlayerEntity player)
    {
        BedwarsEvents.ensureInventoryIsNotFull(player, activity);
        player.getInventory().offerOrDrop(ItemStackBuilder.of(defaultSword).setUnbreakable().build()); //may can be a bug since the sword can theoretically drop here
    }

    ActionResult onInteract(ServerPlayerEntity player, ScreenHandler handler, int slotIndex, int button, SlotActionType actionType)
    {
        //check if it's a "chest" handler
        var screenHandler = handler instanceof GenericContainerScreenHandler ? (GenericContainerScreenHandler)handler : null;
        if(screenHandler == null) return ActionResult.PASS;
        var inventory = screenHandler.getInventory();
        if(inventory == player.getInventory()) return ActionResult.PASS;

        //test if it's a sword movement
        if(actionType == SlotActionType.PICKUP &&
                screenHandler.getCursorStack().isOf(defaultSword) &&
                slotIndex < screenHandler.getRows() * 9) //cancel if it's the default sword move
            return ActionResult.FAIL;

        if(actionType == SlotActionType.QUICK_MOVE &&
                slotIndex >= screenHandler.getRows() * 9 &&
                player.getInventory().getStack(convertIndexToPlayerInventory(slotIndex, screenHandler.getRows() * 9)).isOf(defaultSword)) //prevent quick move to put default sword in chest
            return ActionResult.FAIL;

        if(actionType == SlotActionType.SWAP && slotIndex < screenHandler.getRows() * 9)
        {
            var chestStack = screenHandler.getInventory().getStack(slotIndex);
            var playerStack = player.getInventory().getStack(button);
            if(playerStack.isOf(defaultSword))
            {
                if(chestStack.getItem() instanceof SwordItem) player.getInventory().removeStack(button); //in case of a sword swap, allow it and remove the default sword
                else return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }

    private int convertIndexToPlayerInventory(int index, int GenericHandlerSize)
    {
        if(index >= 27 && index <=53) return index - GenericHandlerSize + 9;
        if(index >= 54) return index - GenericHandlerSize - 27;
        return 0;
    }

    ActionResult onPickupItem(ServerPlayerEntity player, ItemEntity entity, ItemStack stack)
    {
        if(stack.getItem() instanceof SwordItem)
        {
            removeDefaultSword(player);
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
        updatePlayer(player, null);
    }
}
