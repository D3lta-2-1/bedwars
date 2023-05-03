package fr.delta.bedwars.game.player;
import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.event.SlotInteractionEvent;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.shop.entries.ToolEntry;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.stimuli.event.item.ItemThrowEvent;

import java.util.List;

public class ToolManager {
    private final List<ToolEntry.Tier> availableTiers;
    private int currentTier;
    private ItemStack currentTool = ItemStack.EMPTY;
    static public final String TOOL_KEY = "bedwarsTool";

    public static void init(GameActivity activity)
    {
        activity.listen(SlotInteractionEvent.BEFORE, ToolManager::onInteract);
        activity.listen(ItemThrowEvent.EVENT, ToolManager::onThrowItem);
        activity.listen(BedwarsEvents.IS_STACK_THROWABLE, (stack, playerTested) -> isTool(stack) ? ActionResult.FAIL : ActionResult.PASS);
    }

    public ToolManager(List<ToolEntry.Tier> availableTiers)
    {
        this.availableTiers = availableTiers;
        this.currentTier = -1;
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
            var nbt = currentTool.getOrCreateNbt();
            nbt.putInt(TOOL_KEY, getAvailableTiers().get(currentTier).hashCode());
            currentTool.setNbt(nbt);
        }
        return currentTool.copy();
    }

    static public boolean isTool(ItemStack stack)
    {
        if(stack == null) return false;
        if(stack.getNbt() == null) return false;
        return stack.getNbt().contains(TOOL_KEY);
    }
    private boolean isThisTool(ItemStack stack)
    {
        if(currentTier == -1) return false;
        if(stack == null) return false;
        if(stack.getNbt() == null) return false;
        if(!stack.getNbt().contains(TOOL_KEY)) return false;
        return stack.getNbt().getInt(TOOL_KEY) == availableTiers.get(currentTier).hashCode();
    }

    public void removeTool(ServerPlayerEntity player)
    {
        var main = player.getInventory().main;
        for(int i = 0; i < main.size(); i++) {
            if(isThisTool(main.get(i)))
            {
                main.set(i, ItemStack.EMPTY); //could break here but in case of multiple tools in the inventory, by duplication or what ever bug, we remove them all
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

    public ItemStack upgrade(ServerPlayerEntity player)
    {
        removeTool(player);
        if(currentTier < availableTiers.size() - 1)
            currentTier++;
        else
        {
            Bedwars.LOGGER.warn("try to upgrade a tool that is already maxed");
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

    private static ActionResult onInteract(ServerPlayerEntity player, ScreenHandler handler, int slotIndex, int button, SlotActionType actionType) //inventory click
    {
        var screenHandler = handler instanceof GenericContainerScreenHandler ? (GenericContainerScreenHandler)handler : null;
        if(screenHandler == null) return ActionResult.PASS; //if it's not a chest, we don't care
        var inventory = screenHandler.getInventory();
        if(inventory == player.getInventory()) return ActionResult.PASS; //if it's the player's inventory, we don't care

        if(actionType == SlotActionType.PICKUP &&
                isTool(screenHandler.getCursorStack()) &&
                slotIndex < screenHandler.getRows() * 9) //prevent putting the tool in the chest
            return ActionResult.FAIL;

        if(actionType == SlotActionType.QUICK_MOVE &&
                slotIndex >= screenHandler.getRows() * 9 &&
                isTool(player.getInventory().getStack(convertIndexToPlayerInventory(slotIndex, screenHandler.getRows() * 9)))) //prevent quick move to put tool in chest
            return ActionResult.FAIL;

        if(actionType == SlotActionType.SWAP && slotIndex < screenHandler.getRows() * 9) //prevent swapping with the tool
        {
            var playerStack = player.getInventory().getStack(button);
            if(isTool(playerStack))
                return ActionResult.FAIL;

        }
        return ActionResult.PASS;
    }

    private static ActionResult onThrowItem(ServerPlayerEntity player, int slot, ItemStack stack) //item throw protection
    {
        if(stack == null) return ActionResult.PASS;
        if(isTool(stack))
        {
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    private static int convertIndexToPlayerInventory(int index, int GenericHandlerSize)
    {
        if(index >= 27 && index <=53) return index - GenericHandlerSize + 9;
        if(index >= 54) return index - GenericHandlerSize - 27;
        return 0;
    }

}
