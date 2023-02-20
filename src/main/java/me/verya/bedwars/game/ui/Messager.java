package me.verya.bedwars.game.ui;

import me.verya.bedwars.TextUtilities;
import me.verya.bedwars.game.event.BedwarsEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpacePlayers;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;

public class Messager {
    final private GameSpacePlayers players;
    final private TeamManager teamManager;

    public Messager(TeamManager teamManager, GameActivity activity)
    {
        this.teamManager = teamManager;
        this.players = activity.getGameSpace().getPlayers();
        activity.listen(BedwarsEvents.BED_BROKEN, this::onBedBreak);
        activity.listen(BedwarsEvents.PLAYER_DEATH, this::onPlayerDeath);
    }

    void onBedBreak(GameTeam owner, ServerPlayerEntity breaker)
    {

        var messageMark = TextUtilities.concatenate(Text.translatable("bed.bedwars.bedDestruction"), TextUtilities.SPACE ,TextUtilities.GENERAL_PREFIX, TextUtilities.SPACE).setStyle(Style.EMPTY.withBold(true));
        var personalContent = Text.translatable("bed.bedwars.personalBreakContent").append(breaker.getDisplayName()).setStyle(Style.EMPTY.withBold(false));
        var teamName = TextUtilities.getTranslation("name", owner.config().blockDyeColor().name()).setStyle(Style.EMPTY.withFormatting(owner.config().chatFormatting()));
        var generalContent = TextUtilities.concatenate(teamName, Text.translatable("bed.bedwars.generalBreakContent"), breaker.getDisplayName().copy()).setStyle(Style.EMPTY.withBold(false));
        players.playSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL);
        players.sendMessage(Text.empty());
        for(var player : players)
        {
            if(teamManager.teamFor(player) == owner.key())
            {

                var title = Text.translatable("bed.bedwars.bedBrokenTitle").setStyle(Style.EMPTY.withColor(Formatting.RED));
                var subtitle = Text.translatable("bed.bedwars.bedBrokenSubtitle").setStyle(Style.EMPTY.withColor(Formatting.GRAY));
                PlayerPackets.showTitle(player, title, subtitle, 0, 20, 20);
                player.sendMessage(messageMark.copy().append(personalContent));
            }
            else
            {
                player.sendMessage(messageMark.copy().append(generalContent));
            }
        }
        players.sendMessage(Text.empty());
    }

    void onPlayerDeath(ServerPlayerEntity player, DamageSource source, ServerPlayerEntity killer,boolean isFinal)
    {
        players.sendMessage(getDeathMessage(player, source, isFinal));
    }

    private Text getDeathMessage(ServerPlayerEntity player, DamageSource source , boolean Final) {
        var deathMessage = source.getDeathMessage(player).copy();
        if(Final)
        {
            deathMessage.append(TextUtilities.SPACE).append(Text.translatable("death.bedwars.finalKill").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withBold(true)));
        }
        return deathMessage;
    }
}
