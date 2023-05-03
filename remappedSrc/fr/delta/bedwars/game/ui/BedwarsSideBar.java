package fr.delta.bedwars.game.ui;

import eu.pb4.sidebars.api.Sidebar;
import fr.delta.bedwars.StageEvent.GameEventManager;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.teamComponent.TeamComponents;
import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.game.teamComponent.Bed;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.List;
import java.util.Map;

public class BedwarsSideBar {
    public static Sidebar build(Map<GameTeamKey, TeamComponents> teamComponents, TeamManager manager, List<GameTeam> teamsInOrder, GameEventManager gameEventManager, BedwarsActive game, GameActivity activity)
    {
        var sidebar = new Sidebar(Sidebar.Priority.MEDIUM);
        sidebar.setTitle(Text.translatable("sidebar.bedwars.title").formatted(Formatting.GOLD, Formatting.BOLD));

        sidebar.set( b -> {
            b.add(ScreenTexts.EMPTY);
            b.add(player -> gameEventManager.getStageStatue(game));
            b.add(ScreenTexts.EMPTY);
            for(var team : teamsInOrder)
            {
                b.add(player -> {
                    var bed = teamComponents.get(team.key()).bed;
                    var dyeColor = team.config().blockDyeColor();
                    var config = team.config();
                    var prefix = TextUtilities.getTranslation("prefix", dyeColor.name()).formatted(config.chatFormatting());
                    var teamName = TextUtilities.getTranslation("name", dyeColor.name());
                    var mark = getMark(bed, manager, team);
                    var you = getYouMarker(player, team, manager);
                    return TextUtilities.concatenate(prefix, TextUtilities.SPACE, teamName, TextUtilities.DOTS, TextUtilities.SPACE, mark, TextUtilities.SPACE, you);
                });
            }
        });
        for(var player : activity.getGameSpace().getPlayers())
            sidebar.addPlayer(player);
        sidebar.show();
        //register events
        activity.listen(GamePlayerEvents.JOIN, sidebar::addPlayer);
        activity.listen(GamePlayerEvents.REMOVE, sidebar::removePlayer);
        return sidebar;
    }

    private static MutableText getYouMarker(ServerPlayerEntity player, GameTeam team,TeamManager manager)
    {
        return (manager.playersIn(team.key()).contains(PlayerRef.of(player))) ? Text.translatable("sidebar.bedwars.you").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true)) : Text.empty();
    }

    private static MutableText getMark(Bed bed, TeamManager manager, GameTeam team)
    {
        if(!bed.isBroken()) return TextUtilities.CHECKMARK;
        int alivePlayers = manager.playersIn(team.key()).size();
        if(alivePlayers > 0) return Text.literal( String.valueOf(alivePlayers)).setStyle(Style.EMPTY.withColor(Formatting.GREEN));
        return TextUtilities.X;
    }
}
