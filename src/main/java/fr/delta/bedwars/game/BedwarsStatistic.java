package fr.delta.bedwars.game;

import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.game.event.BedwarsEvents;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.stats.GameStatisticBundle;
import xyz.nucleoid.plasmid.game.stats.StatisticKey;
import xyz.nucleoid.plasmid.game.stats.StatisticKeys;

public class BedwarsStatistic
{
    public static StatisticKey<Integer> BEDS_DESTROYED = StatisticKey.intKey(new Identifier(Bedwars.ID, "beds_destroyed"));

    //TODO: Add more statistics because we love statistics
    public static void addTo(GameStatisticBundle bundle, GameActivity activity)
    {
        activity.listen(BedwarsEvents.PLAYER_DEATH, (player, source, killer, isFinalKill) -> {
            bundle.forPlayer(player).increment(StatisticKeys.DEATHS, 1);
            if(killer != null)
                bundle.forPlayer(killer).increment(StatisticKeys.KILLS, 1);
        });

        activity.listen(BedwarsEvents.BED_BROKEN, (team, breaker) -> {
            if(breaker != null)
                bundle.forPlayer(breaker).increment(BedwarsStatistic.BEDS_DESTROYED, 1);
        });
    }
}
