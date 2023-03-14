package fr.delta.bedwars.game.event;

import fr.delta.bedwars.game.shop.entries.ShopEntry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
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

    public static final StimulusEvent<PlayerDeath> PLAYER_DEATH = StimulusEvent.create(PlayerDeath.class, ctx -> (ServerPlayerEntity player, DamageSource source, ServerPlayerEntity killer,boolean isFinal) -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onDeath(player, source, killer, isFinal);
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
    });

    public static final StimulusEvent<AfterPlayerDeath> AFTER_PLAYER_DEATH = StimulusEvent.create(AfterPlayerDeath.class, ctx -> (ServerPlayerEntity player, DamageSource source, ServerPlayerEntity killer,boolean isFinal) -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.afterPlayerDeath(player, source, killer, isFinal);
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
    });

    public static final StimulusEvent<PlayerRespawn> PLAYER_RESPAWN = StimulusEvent.create(PlayerRespawn.class, ctx -> (ServerPlayerEntity player) -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onRespawn(player);
            }
        } catch (Throwable t) {
            ctx.handleException(t);
        }
    });

    public static final StimulusEvent<PlayerBuy> PLAYER_BUY = StimulusEvent.create(PlayerBuy.class, ctx -> (ServerPlayerEntity player, ShopEntry entry) -> {
        try {
            for (var listener : ctx.getListeners()) {
                listener.onBuy(player, entry);
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
        void onBuy(ServerPlayerEntity player, ShopEntry entry);
    }

    public interface TeamWin
    {
        void onTeamWin(GameTeam team);
    }
}
