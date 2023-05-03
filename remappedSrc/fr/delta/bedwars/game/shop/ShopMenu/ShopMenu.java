package fr.delta.bedwars.game.shop.ShopMenu;

import I;
import Z;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import fr.delta.bedwars.data.ShopEntryGetter;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.shop.entries.EmptyEntry;
import fr.delta.bedwars.game.shop.entries.ShopEntry;
import fr.delta.bedwars.game.shop.entries.ShopEntry.BuyOffer;
import fr.delta.bedwars.game.shop.entries.ShopEntry.Cost;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class ShopMenu {
    final private GameActivity activity;
    final private BedwarsActive bedwarsGame;

    public ShopMenu(BedwarsActive bedwarsGame, GameActivity activity)
    {
        this.bedwarsGame = bedwarsGame;
        this.activity = activity;
    }
    public abstract void open(ServerPlayerEntity player);
    protected void setEntryInSlot(SlotGuiInterface gui, ShopEntry entry, int slot)
    {
        var player = gui.getPlayer();
        var display = entry.getDisplay(bedwarsGame, player);
        var displayCount = entry.displayCount(bedwarsGame, player);
        var count = entry.getCount();
        var cost = entry.getCost(bedwarsGame, player);
        var hasGlint = entry.hasGlint(bedwarsGame, player);
        var canBeBough = entry.canBeBough(bedwarsGame, player);

        //set Icon
        var guiElement = new GuiElementBuilder();
        guiElement.setItem(display);
        guiElement.setCount(displayCount);
        var enchantments= entry.enchantment(bedwarsGame, player);
        if(enchantments != null)
            enchantments.forEach(guiElement::enchant);
        //edit nbt
        entry.editNbt(guiElement.getOrCreateNbt(), bedwarsGame, player);

        if(hasGlint) guiElement.glow();

        //set Name
        var multiplier = count != 1 ? Text.literal(" x" + count + " ").setStyle(Style.EMPTY.withFormatting(Formatting.GRAY).withItalic(true)): Text.empty();
        guiElement.setName(entry.getName(bedwarsGame, player).append(multiplier));

        //set lore

        var lore = new ArrayList<Text>();
        var parentedLore= entry.getLore(bedwarsGame, player);
        if(parentedLore != null)
            lore.addAll(parentedLore);
        if(cost != null)
            lore.add(Text.literal("cost: " + cost.count() + " ").append(Text.translatable(cost.item().getTranslationKey())).setStyle(Style.EMPTY.withFormatting(Formatting.GRAY)));
        lore.add(Text.empty());
        if(canBeBough.isSuccess())
            lore.add(Text.literal("Click to purchase").setStyle(Style.EMPTY.withFormatting(Formatting.YELLOW)));
        else
            lore.add(canBeBough.errorMessage());
        guiElement.setLore(lore);

        //add purchase action
        guiElement.setCallback( (index, type, action, guiInterface) ->
        {
            purchase(entry, type, guiInterface, activity);
            setEntryInSlot(gui, entry, slot);
        });

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
            player.playSound(SoundEvents.ENTITY_SILVERFISH_HURT, SoundCategory.PLAYERS, 1.f, 1.f);
            return;
        }
        var cost = entry.getCost(bedwarsGame, player);
        if(cost == null)
        {
            player.sendMessage(Text.literal("[DEBUG] error no cost found").setStyle(Style.EMPTY.withFormatting(Formatting.RED)));
            return;
        }
        var inventory = player.getInventory();
        var stackList = inventory.main;


        //get all slots that contains the "money" item
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
            var name = entry.getName(bedwarsGame, player);
            var boughStack = entry.onBuy(bedwarsGame, player);

            activity.invoker(BedwarsEvents.PLAYER_BUY).onBuy(player, name, entry);
            afterPurchase(gui);
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

    protected void afterPurchase(SlotGuiInterface gui){}

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

    protected void buildListAt(SlotGuiInterface gui, List<Identifier> entriesIDs, ShopEntryGetter entries, int xOffset, int yOffset, int width, int height)
    {
        int x = 0;
        int y = 0;
        var iter = entriesIDs.iterator();
        while(x != width)
        {
            int slot = (xOffset + x) + (yOffset + y) * 9;
            if(iter.hasNext())
            {
                var entry = entries.get(iter.next());

                if(entry == null)
                    setEntryInSlot(gui, EmptyEntry.INSTANCE, slot);
                else
                    setEntryInSlot(gui, entry, slot);
            }
            else
            {
                gui.clearSlot(slot);
            }
            x++;
            if(x == width && y != height -1) //cause of start from 0
            {
                x %= width;
                y++;
            }
        }
    }

    protected void buildSeparator(SlotGuiInterface gui, int y)
    {
        var builder = new GuiElementBuilder();
        builder.setItem(Items.BLACK_STAINED_GLASS_PANE);
        for(int i = y * 9; i < (y + 1) * 9; i++)
        {
            gui.setSlot(i, builder.build());
        }
    }

    public BedwarsActive getBedwarsGame() {
        return bedwarsGame;
    }
}
