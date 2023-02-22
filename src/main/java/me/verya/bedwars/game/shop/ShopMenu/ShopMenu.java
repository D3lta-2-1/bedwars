package me.verya.bedwars.game.shop.ShopMenu;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import me.verya.bedwars.game.shop.Entry.ShopEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public interface ShopMenu {
    void open(ServerPlayerEntity player);
    default void purchase(ShopEntry entry, ClickType type, SlotGuiInterface gui)
    {
        var player = gui.getPlayer();
        var cost = entry.getCost();
        var inventory = player.getInventory();
        var stackList = inventory.main;

        //get all slots
        List<Integer> slotWithResources = new ArrayList<>();
        int i = 0;
        int total = 0;
        for(var stack : stackList)
        {
            if(stack.isOf(cost.item))
            {
                total += stack.getCount();
                slotWithResources.add(i);
            }
            i++;
        }
        if(total >= cost.count)
        {
            //make the transaction
            int restToPay = cost.count;
            for(var index : slotWithResources)
            {
                var stack = stackList.get(index);
                if(stack.getCount() > restToPay)
                {
                    stackList.get(index).decrement(restToPay);
                    break;
                }
                else
                {
                    restToPay -= stack.getCount();
                    stackList.set(index, ItemStack.EMPTY);
                }
            }
            //Todo: make a shop event and add sound
            var boughStack = entry.onBuy(player);
            if(boughStack.isEmpty()) return;
            if(type.numKey)
            {
                addStackInSlot(player, type.value -1, boughStack);
            }
            else
            {
                inventory.offerOrDrop(entry.onBuy(player));
            }
        }
        else
        {
            //Todo: make a better error message and add sound
            player.sendMessage(Text.literal("you don't have enough resources"));
        }
    }

    private void addStackInSlot(ServerPlayerEntity player, int slot, ItemStack stack)
    {
        var inventory = player.getInventory();
        var stackToGiveBack = ItemStack.EMPTY;
        if(!inventory.getStack(slot).isItemEqual(stack))
        {
            stackToGiveBack = inventory.removeStack(slot);
        }
        while(true)
        {
            if (slot != -1)
            {
                int j = stack.getMaxCount() - inventory.getStack(slot).getCount();
                if (inventory.insertStack(slot, stack.split(j)))
                {
                    player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, slot, inventory.getStack(slot)));
                }

                if (!stack.isEmpty())
                {
                    slot = inventory.getOccupiedSlotWithRoomForStack(stack);
                    if (slot == -1)
                    {
                        slot = inventory.getEmptySlot();
                    }
                    continue;
                }
            }
            player.dropItem(stack, false);
            inventory.offerOrDrop(stackToGiveBack);
            return;
        }
    }
}
