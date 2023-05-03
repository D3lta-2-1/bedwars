package fr.delta.bedwars.game.behaviour;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.event.SlotInteractionEvent;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.shop.ShopMenu.TrackerShopMenu;
import fr.delta.bedwars.game.ui.PlayerCustomPacketsSender;
import fr.delta.bedwars.mixin.PlayerInventoryAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.stimuli.event.item.ItemThrowEvent;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import java.util.Map;

public class CompassManager {

    static class target
    {
        public target(GameTeamKey team, ServerPlayerEntity player) {
            this.team = team;
            this.player = player; //we can use a ServerPlayerEntity as key because it's released when the died event is fired, so it cover case where it's recreated
        }

        public GameTeamKey team;
        public ServerPlayerEntity player;
    }

    private final Map<ServerPlayerEntity, target> playerTargetMap = new Object2ObjectOpenHashMap<>(); //same here
    private final TrackerShopMenu trackerShopMenu;
    private final BedwarsActive game;
    public CompassManager(BedwarsActive game, GameActivity activity)
    {
        this.game = game;
        this.trackerShopMenu = new TrackerShopMenu(game, activity);
        activity.listen(BedwarsEvents.PLAYER_RESPAWN, (player) -> player.getInventory().setStack(8, new ItemStack(Items.COMPASS)));
        activity.listen(SlotInteractionEvent.BEFORE, this::onInteract);
        activity.listen(ItemUseEvent.EVENT, this::onUse);
        activity.listen(ItemThrowEvent.EVENT, (player, slot, stack) -> stack != null && stack.isOf(Items.COMPASS) ? ActionResult.FAIL : ActionResult.PASS);
        activity.listen(BedwarsEvents.IS_STACK_THROWABLE, (stack, playerTested) -> stack.isOf(Items.COMPASS) ? ActionResult.FAIL : ActionResult.PASS);
        activity.listen(GameActivityEvents.TICK, this::tick);
        activity.listen(BedwarsEvents.AFTER_PLAYER_DEATH, this::onPlayerDeath);
    }

    //gui : open shop if no target, allow to change player targeted if target
    public TypedActionResult<ItemStack> onUse(ServerPlayerEntity player, Hand hand)
    {
        var stack = player.getStackInHand(hand);
        if(stack.isOf(Items.COMPASS))
        {
            if(playerTargetMap.containsKey(player))
                openTrackerSettings(player);
            else
                trackerShopMenu.open(player);
        }
        return TypedActionResult.pass(stack);
    }

    private ActionResult onInteract(ServerPlayerEntity player, ScreenHandler handler, int slotIndex, int button, SlotActionType actionType) //inventory click
    {
        var screenHandler = handler instanceof GenericContainerScreenHandler ? (GenericContainerScreenHandler) handler : null;
        if (screenHandler == null) return ActionResult.PASS; //if it's not a chest, we don't care
        var inventory = screenHandler.getInventory();
        if (inventory == player.getInventory())
            return ActionResult.PASS; //if it's the player's inventory, we don't care

        if (actionType == SlotActionType.PICKUP &&
                screenHandler.getCursorStack().isOf(Items.COMPASS) &&
                slotIndex < screenHandler.getRows() * 9) //prevent putting the tool in the chest
            return ActionResult.FAIL;

        if (actionType == SlotActionType.QUICK_MOVE &&
                slotIndex >= screenHandler.getRows() * 9 &&
                player.getInventory().getStack(convertIndexToPlayerInventory(slotIndex, screenHandler.getRows() * 9)).isOf(Items.COMPASS)) //prevent quick move to put tool in chest
            return ActionResult.FAIL;

        if (actionType == SlotActionType.SWAP && slotIndex < screenHandler.getRows() * 9) //prevent swapping with the tool
        {
            var playerStack = player.getInventory().getStack(button);
            if (playerStack.isOf(Items.COMPASS))
                return ActionResult.FAIL;

        }
        return ActionResult.PASS;
    }

    public void removeCompass(ServerPlayerEntity player)
    {
        for(var stackList : ((PlayerInventoryAccessor)player.getInventory()).getCombinedInventory())
        {
            for(int i =0; i < stackList.size(); i++)
            {
                if(stackList.get(i).isOf(Items.COMPASS))
                    stackList.set(i, ItemStack.EMPTY);
            }
        }
    }

    public void setTarget(ServerPlayerEntity buyer,GameTeamKey team, ServerPlayerEntity target)
    {
        playerTargetMap.put(buyer, new target(team, target));
    }

    private int convertIndexToPlayerInventory(int index, int GenericHandlerSize)
    {
        if(index >= 27 && index <=53) return index - GenericHandlerSize + 9;
        if(index >= 54) return index - GenericHandlerSize - 27;
        return 0;
    }

    public void openTrackerSettings(ServerPlayerEntity player)
    {
        //open gui
        var gui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false)
        {
            @Override
            public boolean canPlayerClose() {
                return true;
            }
        };
        gui.setAutoUpdate(false);
        gui.setTitle(Text.translatable("trackerSetting.bedwars.title"));
        //add player icon
        for(var target : game.getPlayersInTeam(playerTargetMap.get(player).team))
        {
            gui.addSlot(getPlayerIcon(target));
        }
        gui.open();
    }

    private GuiElementBuilder getPlayerIcon(ServerPlayerEntity player)
    {
        var icon = new GuiElementBuilder(Items.PLAYER_HEAD);
        icon.getOrCreateNbt().putString("SkullOwner", player.getEntityName());
        icon.setName(player.getDisplayName());
        icon.setCallback((index, type, action, gui) -> {
            var user = gui.getPlayer();
            playerTargetMap.get(user).player = player;
        });
        return icon;
    }

    private void tick()
    {
        //may be a little laggy, it's the only way to update the compass, but we can do less update to economize bandwidth
        this.playerTargetMap.forEach((player, target) ->
                {
                    player.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(target.player.getBlockPos(), 0));
                    var text = Text.translatable("tracker.bedwars.blockAway",
                                    TextUtilities.getFormattedPlayerName(target.player, game.getTeamManager()),
                                    (int)player.distanceTo(target.player))
                            .formatted(Formatting.BOLD);
                    PlayerCustomPacketsSender.showOverlay(player, text);
                }
        );
    }

    private void onPlayerDeath(ServerPlayerEntity player, DamageSource source, ServerPlayerEntity killer, boolean isFinal)
    {
        //if the player had a tracker, we remove it
        playerTargetMap.remove(player);

        for(var entry : playerTargetMap.entrySet())
        {
            var targetPlayer = entry.getValue().player;
            var targetTeam = entry.getValue().team;

            if(targetPlayer == player)
            {
                var newTargetPlayer = game.getPlayersInTeam(targetTeam).stream().findAny();
                if(newTargetPlayer.isPresent())
                {
                    entry.setValue(new target(targetTeam, newTargetPlayer.get()));
                }
                else
                {
                    playerTargetMap.forEach((key, value) ->{ //if the team is dead, we remove the tracker
                        if(value.player == player)
                            playerTargetMap.remove(key);
                    });

                    PlayerCustomPacketsSender.showOverlay(player, Text.empty());
                }
            }
        }
    }
}
