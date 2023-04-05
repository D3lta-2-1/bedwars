package fr.delta.bedwars.game.ui;

import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.shop.entry.ShopEntry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
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
        activity.listen(BedwarsEvents.PLAYER_BUY, this::onBuy);
    }

    void onBedBreak(GameTeam owner, ServerPlayerEntity breaker)
    {
        var messageMark = TextUtilities.concatenate(Text.translatable("bed.bedwars.bedDestruction"), TextUtilities.SPACE ,TextUtilities.GENERAL_PREFIX, TextUtilities.SPACE).setStyle(Style.EMPTY.withBold(true));
        var teamName = TextUtilities.getTranslation("name", owner.config().blockDyeColor().name()).setStyle(Style.EMPTY.withFormatting(owner.config().chatFormatting()));
        var personalContent = Text.empty();
        var generalContent = TextUtilities.concatenate(teamName, Text.translatable("bed.bedwars.anonymousBreak")).setStyle(Style.EMPTY.withBold(false));
        if(breaker != null)
        {
            personalContent = Text.translatable("bed.bedwars.personalBreakContent").append(breaker.getDisplayName()).setStyle(Style.EMPTY.withBold(false));
            generalContent = TextUtilities.concatenate(teamName, Text.translatable("bed.bedwars.generalBreakContent"), breaker.getDisplayName().copy()).setStyle(Style.EMPTY.withBold(false));
            players.playSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL);
        }

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
        player.playSound(SoundEvents.ENTITY_SQUID_DEATH, SoundCategory.PLAYERS, 1.f, 1.f);
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

    private void onBuy(ServerPlayerEntity player, ShopEntry entry)
    {
        var text1 = Text.translatable("shop.bedwars.youPurchase").setStyle(Style.EMPTY.withFormatting(Formatting.GREEN));
        var text2 = entry.getTitle().setStyle(Style.EMPTY.withFormatting(Formatting.YELLOW));
        player.sendMessage(text1.append(text2));
        player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.f, 1.f);
    }
}
