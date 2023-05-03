package fr.delta.bedwars.game;

import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

//Activity handler for the end of the Game, manage the close and should be suitable for victory dances
public class BedwarsEnd {
    final GameSpace space;
    GameActivity activity;
    final ServerWorld world;
    final GameSpace gameSpace;
    long date;
    static final int timeBeforeClose = 5 * 20; //5s

    BedwarsEnd(GameSpace gameSpace, ServerWorld world)
    {
        this.space = gameSpace;
        this.date = world.getTime();
        this.world = world;
        this.gameSpace = gameSpace;
        gameSpace.setActivity(gameActivity -> this.activity = gameActivity);
        activity.deny(GameRuleType.HUNGER);
        activity.deny(GameRuleType.PORTALS);
        activity.deny(GameRuleType.PVP);
        activity.deny(GameRuleType.FALL_DAMAGE);
        activity.deny(GameRuleType.INTERACTION);
        activity.deny(GameRuleType.BLOCK_DROPS);
        activity.deny(GameRuleType.THROW_ITEMS);
        activity.deny(GameRuleType.MODIFY_ARMOR);
        activity.deny(GameRuleType.MODIFY_INVENTORY);
        activity.deny(GameRuleType.CRAFTING);
        activity.deny(GameRuleType.PORTALS);
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
