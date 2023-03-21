package fr.delta.bedwars.game.player;
import fr.delta.bedwars.event.SlotInteractionEvent;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.shop.entries.ToolEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.stimuli.event.item.ItemThrowEvent;

import java.util.List;

public class ToolManager {
    List<ToolEntry.Tier> availableTiers;
    ServerPlayerEntity player;
    int currentTier;
    ItemStack currentTool = ItemStack.EMPTY;

    public ToolManager(ServerPlayerEntity player, List<ToolEntry.Tier> availableTiers, GameActivity activity)
    {
        this.player = player;
        this.availableTiers = availableTiers;
        this.currentTier = -1;
        activity.listen(SlotInteractionEvent.BEFORE, this::onInteract);
        activity.listen(ItemThrowEvent.EVENT, this::onThrowItem);
        activity.listen(BedwarsEvents.IS_STACK_THROWABLE, (stack, playerTested) ->
        {
            if(playerTested != player)
                return ActionResult.PASS;
            return stack.isItemEqual(currentTool) ? ActionResult.FAIL : ActionResult.PASS;
        });
    }

    public ItemStack concurrentTool()
    {
        return currentTool;
    }
    public int concurrentTier()
    {
        return currentTier;
    }


    public ItemStack createTool()
    {
        if(currentTier != -1)
        {
            var builder = ItemStackBuilder.of(availableTiers.get(currentTier).item());
            availableTiers.get(currentTier).enchantments().forEach(builder::addEnchantment);
            builder.setUnbreakable();
            currentTool = builder.build();
        }
        return currentTool.copy();
    }

    public void removeTool()
    {
        var main = player.getInventory().main;
        for(int i = 0; i < main.size(); i++) {
            if(main.get(i).isItemEqual(currentTool))
            {
                main.set(i, ItemStack.EMPTY);
                break;
            }
        }
    }

    public boolean isEmpty() {
        return currentTier == -1;
    }

    public boolean isMaxed()
    {
        return currentTier == availableTiers.size() - 1;
    }

    public ItemStack upgrade()
    {
        removeTool();
        if(currentTier < availableTiers.size() - 1)
            currentTier++;
        else
        {
            System.out.println("try to upgrade a tool that is already maxed");
        }
        return createTool();
    }

    public void decrementTier()
    {
        if(currentTier > 0)
        {
            currentTier--;
        }
    }

    public List<ToolEntry.Tier> getAvailableTiers()
    {
        return availableTiers;
    }

    private ActionResult onInteract(ServerPlayerEntity player, ScreenHandler handler, int slotIndex, int button, SlotActionType actionType)
    {
        if(player != this.player) return ActionResult.PASS; //if it's not the player we're managing, we don't care
        var screenHandler = handler instanceof GenericContainerScreenHandler ? (GenericContainerScreenHandler)handler : null;
        if(screenHandler == null) return ActionResult.PASS; //if it's not a chest, we don't care
        var inventory = screenHandler.getInventory();
        if(inventory == player.getInventory()) return ActionResult.PASS; //if it's the player's inventory, we don't care

        if(actionType == SlotActionType.PICKUP &&
                screenHandler.getCursorStack().isItemEqual(concurrentTool()) &&
                slotIndex < screenHandler.getRows() * 9) //prevent putting the tool in the chest
            return ActionResult.FAIL;

        if(actionType == SlotActionType.QUICK_MOVE &&
                slotIndex >= screenHandler.getRows() * 9 &&
                player.getInventory().getStack(convertIndexToPlayerInventory(slotIndex, screenHandler.getRows() * 9)).isItemEqual(concurrentTool())) //prevent quick move to put tool in chest
            return ActionResult.FAIL;

        if(actionType == SlotActionType.SWAP && slotIndex < screenHandler.getRows() * 9) //prevent swapping with the tool
        {
            var playerStack = player.getInventory().getStack(button);
            if(playerStack.isItemEqual(concurrentTool()))
            {
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }

    ActionResult onThrowItem(ServerPlayerEntity player, int slot, ItemStack stack)
    {
        if(player != this.player) return ActionResult.PASS; //if it's not the player we're managing, we don't care
        if(stack == null) return ActionResult.PASS;
        if(stack.isItemEqual(concurrentTool()))
        {
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    private int convertIndexToPlayerInventory(int index, int GenericHandlerSize)
    {
        if(index >= 27 && index <=53) return index - GenericHandlerSize + 9;
        if(index >= 54) return index - GenericHandlerSize - 27;
        return 0;
    }

}
