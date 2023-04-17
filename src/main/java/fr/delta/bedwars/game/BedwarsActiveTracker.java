package fr.delta.bedwars.game;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.event.GameEvents;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

import java.util.HashMap;
import java.util.Map;

public class BedwarsActiveTracker
{
    private static final Map<GameSpace, BedwarsActive> activeGames = new HashMap<>();

    static void addGame(GameSpace gameSpace, BedwarsActive game)
    {
        activeGames.put(gameSpace, game);
    }

    static void removeGame(GameSpace gameSpace)
    {
        activeGames.remove(gameSpace);
    }

    static
    {
        GameEvents.DESTROY_ACTIVITY.register((gameSpace, activity, reason) -> removeGame(gameSpace));
    }

    public static BedwarsActive getBedwarsActive(ServerPlayerEntity player)
    {
        var gameSpace = GameSpaceManager.get().byPlayer(player);
        if (gameSpace == null)
            return null;
        return activeGames.get(gameSpace);
    }
}
