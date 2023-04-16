package fr.delta.bedwars.game;

import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;

//Activity handler for the end of the Game, manage the close and should be suitable for victory dances
public class BedwarsEnd {
    final GameSpace space;
    GameActivity activity;
    final ServerWorld world;
    final GameSpace gameSpace;
    long date;
    static final int timeBeforeClose = 10 * 20; //5s

    BedwarsEnd(GameSpace gameSpace, ServerWorld world)
    {
        this.space = gameSpace;
        this.date = world.getTime();
        this.world = world;
        this.gameSpace = gameSpace;
        gameSpace.setActivity(gameActivity -> this.activity = gameActivity);
        activity.listen(GameActivityEvents.TICK, this::tick);
    }

    void tick()
    {
        if(world.getTime() >= date + timeBeforeClose)
        {
            this.gameSpace.close(GameCloseReason.FINISHED);
        }
    }


}
