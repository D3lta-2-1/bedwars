package me.verya.bedwars.game.shop.ShopMenu;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import me.verya.bedwars.game.event.BedwarsEvents;
import me.verya.bedwars.game.shop.entry.ShopEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameActivity;

import java.util.ArrayList;
import java.util.List;

public abstract class ShopMenu {
    final private GameActivity activity;

    public ShopMenu(GameActivity activity)
    {
        this.activity = activity;
    }
    public abstract void open(ServerPlayerEntity player);
    protected void add(SimpleGui gui, ShopEntry entry, int slot)
    {
        var guiElement = new GuiElementBuilder();
        guiElement.setItem(entry.getItem());
        int count = entry.getCount();
        var multiplier = count != 1 ? Text.literal(" x" + count + " ").setStyle(Style.EMPTY.withFormatting(Formatting.GRAY).withItalic(true)): Text.empty();
        guiElement.setName(entry.getTitle().append(multiplier));
        var lore = new ArrayList<Text>();
        lore.add(Text.literal("cost: " + entry.getCost().count + " ").append(Text.translatable(entry.getCost().item.getTranslationKey())).setStyle(Style.EMPTY.withFormatting(Formatting.GRAY)));
        lore.add(Text.empty());
        lore.add(Text.literal("Click to purchase").setStyle(Style.EMPTY.withFormatting(Formatting.YELLOW)));
        guiElement.setLore(lore);
        guiElement.setCallback( (index, type, action, guiInterface) -> purchase(entry, type, guiInterface, activity));
        gui.setSlot(slot, guiElement);
    }
    private void purchase(ShopEntry entry, ClickType type, SlotGuiInterface gui, GameActivity activity)
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
            var boughStack = entry.onBuy(player);
            boughStack.setCount(entry.getCount());

            activity.invoker(BedwarsEvents.PLAYER_BUY).onBuy(player, entry);
            if(boughStack.isEmpty()) return;
            if(type.numKey)
            {
                addStackInSlot(player, type.value -1, boughStack);
            }
            else
            {
                inventory.offerOrDrop(boughStack);
            }
        }
        else
        {
            player.sendMessage(Text.translatable("warning.bedwars.resourcesMissing").setStyle(Style.EMPTY.withFormatting(Formatting.RED)));
            player.playSound(SoundEvents.ENTITY_SILVERFISH_HURT, SoundCategory.PLAYERS, 1.f, 1.f);
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
