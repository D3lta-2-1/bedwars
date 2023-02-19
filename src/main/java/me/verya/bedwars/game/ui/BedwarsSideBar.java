package me.verya.bedwars.game.ui;

import eu.pb4.sidebars.api.Sidebar;
import me.verya.bedwars.TextUtilities;
import me.verya.bedwars.game.component.TeamComponents;
import me.verya.bedwars.game.component.Bed;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.List;
import java.util.Map;

public class BedwarsSideBar {
    public static Sidebar build(Map<GameTeamKey, TeamComponents> teamComponents, TeamManager manager, List<GameTeam> teamsInOrder)
    {
        var sidebar = new Sidebar(Sidebar.Priority.MEDIUM);
        sidebar.setTitle(Text.translatable("sidebar.bedwars.title").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true)));

        sidebar.set( b -> {
            b.add(ScreenTexts.EMPTY);
            for(var team : teamsInOrder)
            {
                b.add(player -> {
                    var bed = teamComponents.get(team.key()).bed;
                    var dyeColor = team.config().blockDyeColor();
                    var prefix = TextUtilities.getTranslation("prefix", dyeColor.name()).setStyle(Style.EMPTY.withColor(dyeColor.getSignColor()));
                    var teamName = TextUtilities.getTranslation("name", dyeColor.name());
                    var mark =getMark(bed, manager, team);
                    var you = getYouMarker(player, team, manager);
                    return TextUtilities.concatenate(prefix, TextUtilities.SPACE, teamName, TextUtilities.DOTS, TextUtilities.SPACE, mark, TextUtilities.SPACE, you);
                });
            }
        });
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
