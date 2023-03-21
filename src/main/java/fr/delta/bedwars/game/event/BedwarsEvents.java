package fr.delta.bedwars.game.event;

import fr.delta.bedwars.game.shop.entries.ShopEntry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public final class BedwarsEvents {
    /*
    many internals events of the bedwars game
     */
    public static final StimulusEvent<BedBroken> BED_BROKEN = StimulusEvent.create(BedBroken.class, ctx -> (owner, breaker) -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onBreak(owner, breaker);
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
    });

    public static final StimulusEvent<PlayerDeath> PLAYER_DEATH = StimulusEvent.create(PlayerDeath.class, ctx -> (player, source,  killer, isFinal) -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onDeath(player, source, killer, isFinal);
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
    });

    public static final StimulusEvent<AfterPlayerDeath> AFTER_PLAYER_DEATH = StimulusEvent.create(AfterPlayerDeath.class, ctx -> (player, source, killer, isFinal) -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.afterPlayerDeath(player, source, killer, isFinal);
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
    });

    public static final StimulusEvent<PlayerRespawn> PLAYER_RESPAWN = StimulusEvent.create(PlayerRespawn.class, ctx -> player -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onRespawn(player);
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
    });

    public static final StimulusEvent<PlayerBuy> PLAYER_BUY = StimulusEvent.create(PlayerBuy.class, ctx -> (player, name, entry) -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onBuy(player, name, entry);
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
    });

    public static final StimulusEvent<TeamWin> TEAM_WIN = StimulusEvent.create(TeamWin.class, ctx -> (GameTeam winner) -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onTeamWin(winner);
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
    });

    public static final StimulusEvent<isStackThrowable> IS_STACK_THROWABLE = StimulusEvent.create(isStackThrowable.class, ctx -> (stack, player) -> {
        try {
            for (var listener : ctx.getListeners()) {
                 var result = listener.test(stack, player);
                 if(result != ActionResult.PASS) return result;
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
        return ActionResult.SUCCESS;
    });

    /**
     * Ensure that the player's inventory is not full, if it is, drop the fist item that is not protected by the IS_STACK_THROWABLE event
     * @param player the player
     * @param activity the game activity
     */
    public static void ensureInventoryIsNotFull(ServerPlayerEntity player, GameActivity activity)
    {
        if (player.getInventory().getEmptySlot() != -1) return;

        var main = player.getInventory().main;
        for(int i = 0; i < main.size(); i++)
        {
            var result = activity.invoker(BedwarsEvents.IS_STACK_THROWABLE).test(main.get(i), player);
            if(result == ActionResult.FAIL) continue;
            player.dropItem(player.getInventory().getStack(i), false);
            player.getInventory().setStack(i, ItemStack.EMPTY);
            return;
        }
        throw new IllegalStateException("The player's inventory is full and no item can be dropped");
    }

    public interface BedBroken
    {
        void onBreak(GameTeam owner, ServerPlayerEntity breaker);
    }

    public interface PlayerDeath
    {
        void onDeath(ServerPlayerEntity player, DamageSource source, ServerPlayerEntity killer, boolean isFinal);
    }

    public interface AfterPlayerDeath
    {
        void afterPlayerDeath(ServerPlayerEntity player, DamageSource source, ServerPlayerEntity killer, boolean isFinal);
    }

    public interface PlayerRespawn
    {
        void onRespawn(ServerPlayerEntity player);
    }

    public interface PlayerBuy
    {
        void onBuy(ServerPlayerEntity player, Text name, ShopEntry entry);
    }

    public interface TeamWin
    {
        void onTeamWin(GameTeam team);
    }

    public interface isStackThrowable
    {
        ActionResult test(ItemStack stack, ServerPlayerEntity player);
    }
}
