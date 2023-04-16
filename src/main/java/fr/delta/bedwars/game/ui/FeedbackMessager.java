package fr.delta.bedwars.game.ui;

import com.google.common.collect.Multimap;
import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.shop.entries.ShopEntry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpacePlayers;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class FeedbackMessager {

    final BedwarsActive game;
    final private GameSpacePlayers players;
    final private Multimap<GameTeam, PlayerRef> teamPlayersMap;
    final private ServerWorld world;

    public FeedbackMessager(BedwarsActive game, Multimap<GameTeam, PlayerRef> teamPlayersMap, ServerWorld world, GameActivity activity)
    {
        this.game = game;
        this.players = activity.getGameSpace().getPlayers();
        this.teamPlayersMap = teamPlayersMap;
        this.world = world;
        activity.listen(BedwarsEvents.BED_BROKEN, this::onBedBreak);
        activity.listen(BedwarsEvents.BED_DESTRUCTION, this::onBedDestruction);
        activity.listen(BedwarsEvents.PLAYER_DEATH, this::onPlayerDeath);
        activity.listen(BedwarsEvents.AFTER_PLAYER_DEATH, this::onAfterPlayerDeath);
        activity.listen(BedwarsEvents.PLAYER_BUY, this::onBuy);
        activity.listen(BedwarsEvents.TEAM_WIN, this::onTeamWin);
    }

    void onBedBreak(GameTeam owner, ServerPlayerEntity breaker)
    {
        if(breaker != null)
        {
            var messageMark = TextUtilities.concatenate(Text.translatable("bed.bedwars.bedDestruction"), TextUtilities.SPACE ,TextUtilities.GENERAL_PREFIX, TextUtilities.SPACE).setStyle(Style.EMPTY.withBold(true));
            var teamName = TextUtilities.getTranslation("name", owner.config().blockDyeColor().name()).formatted(owner.config().chatFormatting());
            //var personalContent = Text.empty();
            //var generalContent = TextUtilities.concatenate(teamName, Text.translatable("bed.bedwars.anonymousBreak")).setStyle(Style.EMPTY.withBold(false));
            var personalContent = Text.translatable("bed.bedwars.personalBreakContent").append(breaker.getDisplayName()).setStyle(Style.EMPTY.withBold(false));
            var generalContent = TextUtilities.concatenate(teamName, Text.translatable("bed.bedwars.generalBreakContent"), breaker.getDisplayName().copy()).setStyle(Style.EMPTY.withBold(false));
            players.playSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL);


            players.sendMessage(Text.empty());
            for(var player : players)
            {
                if(game.getTeamForPlayer(player) == owner)
                {

                    var title = Text.translatable("bed.bedwars.bedBrokenTitle").setStyle(Style.EMPTY.withColor(Formatting.RED));
                    var subtitle = Text.translatable("bed.bedwars.bedBrokenSubtitle").setStyle(Style.EMPTY.withColor(Formatting.GRAY));
                    PlayerCustomPacketsSender.showTitle(player, title, subtitle, 0, 60, 20);
                    player.sendMessage(messageMark.copy().append(personalContent));
                }
                else
                {
                    player.sendMessage(messageMark.copy().append(generalContent));
                }
            }
            players.sendMessage(Text.empty());
        }
    }

    void onBedDestruction()
    {
        var message = Text.translatable("events.bedwars.allBedsHaveBeenDestroyed");
        players.forEach(player -> PlayerCustomPacketsSender.showTitle(player, Text.translatable("events.bedwars.bedDestruction").formatted(Formatting.RED), message.copy().formatted(Formatting.GRAY), 0, 60, 20));
        players.sendMessage(message.formatted(Formatting.RED));
        players.playSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL);
    }

    void onPlayerDeath(ServerPlayerEntity player, DamageSource source, ServerPlayerEntity killer,boolean isFinal)
    {
        players.sendMessage(getDeathMessage(player, source, isFinal));
    }

    void onAfterPlayerDeath(ServerPlayerEntity player, DamageSource source, ServerPlayerEntity killer,boolean isFinal)
    {
        player.playSound(SoundEvents.ENTITY_SQUID_DEATH, SoundCategory.PLAYERS, 1.f, 1.f);
    }

    private Text getDeathMessage(ServerPlayerEntity player, DamageSource source , boolean Final) {
        var deathMessage = source.getDeathMessage(player).copy();
        if(Final)
        {
            deathMessage.append(TextUtilities.SPACE).append(Text.translatable("death.bedwars.finalKill").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withBold(true)));
        }
        return deathMessage;
    }

    private void onBuy(ServerPlayerEntity player, Text name, ShopEntry entry)
    {
        var text = Text.translatable("shop.bedwars.youPurchase").setStyle(Style.EMPTY.withFormatting(Formatting.GREEN));
        text.append(name.copy().setStyle(Style.EMPTY.withFormatting(Formatting.YELLOW)));
        player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.f, 1.f);
        if(entry.shouldNotifyAllTeam())
            game.getPlayersInTeam(game.getTeamForPlayer(player)).sendMessage(text);
        else
            player.sendMessage(text);
    }

    private void onTeamWin(GameTeam team)
    {
        if(team == null) return;
        var winMessage = TextUtilities.concatenate(TextUtilities.getTranslation("name", team.config().blockDyeColor().name()).formatted(team.config().chatFormatting()),
                Text.translatable("win.bedwars.win").formatted(Formatting.GRAY));


        players.sendMessage(winMessage);
        for(var entries : teamPlayersMap.entries())
        {
            var playerRef = entries.getValue();
            if(playerRef == null) continue;
            var player = playerRef.getEntity(world);
            if(player == null) continue;
            if(entries.getKey() == team)
            {
                PlayerCustomPacketsSender.showTitle(player, Text.translatable("win.bedwars.victory").formatted(Formatting.GOLD, Formatting.BOLD), 0, 100, 20);
            }
            else
            {
                PlayerCustomPacketsSender.showTitle(player, Text.translatable("win.bedwars.gameOver").formatted(Formatting.RED, Formatting.BOLD), winMessage, 0, 100, 20);
            }
        }
    }
}
