package fr.delta.bedwars;

import fr.delta.bedwars.game.BedwarsActive;
import xyz.nucleoid.plasmid.event.GameEvents;
import xyz.nucleoid.plasmid.game.GameSpace;

import java.util.HashMap;
import java.util.Map;

public class BedwarsActiveTracker {

    private final Map<GameSpace, BedwarsActive> activeGames = new HashMap<>();
    static private final BedwarsActiveTracker INSTANCE = new BedwarsActiveTracker();

    private BedwarsActiveTracker() {
        GameEvents.DESTROY_ACTIVITY.register((gameSpace, gameActivity, reason) -> activeGames.remove(gameSpace));
    }

    public static BedwarsActiveTracker getInstance() {
        return INSTANCE;
    }

    public void addGame(GameSpace gameSpace, BedwarsActive bedwarsActive) {
        activeGames.put(gameSpace, bedwarsActive);
    }

    public BedwarsActive getGame(GameSpace gameSpace) {
        return activeGames.get(gameSpace);
    }
}
