package fr.delta.bedwars.game.shop.ShopMenu;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.shop.articles.ShopEntry;
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
    final private BedwarsActive bedwarsGame;

    public ShopMenu(BedwarsActive bedwarsGame, GameActivity activity)
    {
        this.bedwarsGame = bedwarsGame;
        this.activity = activity;
    }
    public abstract void open(ServerPlayerEntity player);
    protected void setEntryInSlot(SimpleGui gui, ShopEntry entry, int slot)
    {
        var display = entry.getDisplay();
        var displayCount = entry.displayCount();
        var count = entry.getCount();
        var cost = entry.getCost(bedwarsGame, gui.getPlayer());
        var hasGlint = entry.hasGlint();

        //set Icon
        var guiElement = new GuiElementBuilder();
        guiElement.setItem(display);
        guiElement.setCount(displayCount);
        if(hasGlint) guiElement.glow();

        //set Name
        var multiplier = count != 1 ? Text.literal(" x" + count + " ").setStyle(Style.EMPTY.withFormatting(Formatting.GRAY).withItalic(true)): Text.empty();
        guiElement.setName(entry.getName().append(multiplier));

        //set lore

        var lore = new ArrayList<Text>();
        var parentedLore= entry.getLore(bedwarsGame, gui.getPlayer());
        if(parentedLore != null)
            lore.addAll(parentedLore);
        lore.add(Text.literal("cost: " + cost.count() + " ").append(Text.translatable(cost.item().getTranslationKey())).setStyle(Style.EMPTY.withFormatting(Formatting.GRAY)));
        lore.add(Text.empty());
        lore.add(Text.literal("Click to purchase").setStyle(Style.EMPTY.withFormatting(Formatting.YELLOW)));
        guiElement.setLore(lore);

        //add purchase action
        guiElement.setCallback( (index, type, action, guiInterface) -> purchase(entry, type, guiInterface, activity));

        //finally set item in slot
        gui.setSlot(slot, guiElement);
    }
    private void purchase(ShopEntry entry, ClickType type, SlotGuiInterface gui, GameActivity activity)
    {
        var player = gui.getPlayer();
        var result = entry.canBeBough(bedwarsGame, player);
        if(!result.isSuccess())
        {
            player.sendMessage(result.errorMessage());
            return;
        }
        var cost = entry.getCost(bedwarsGame, player);
        var inventory = player.getInventory();
        var stackList = inventory.main;


        //get all slots
        List<Integer> slotWithResources = new ArrayList<>();
        int i = 0;
        int total = 0;
        for(var stack : stackList)
        {
            if(stack.isOf(cost.item()))
            {
                total += stack.getCount();
                slotWithResources.add(i);
            }
            i++;
        }
        if(total >= cost.count())
        {
            //make the transaction
            int restToPay = cost.count();
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
            var boughStack = entry.onBuy(bedwarsGame, player);



            activity.invoker(BedwarsEvents.PLAYER_BUY).onBuy(player, entry);
            if(boughStack.isEmpty()) return;
            boughStack.setCount(entry.getCount());
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
