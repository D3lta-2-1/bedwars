package fr.delta.bedwars.StageEvent;

import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;

import java.util.Queue;

public class GameEventManager {

    private final World world;
    private final Queue<StageEvent> events;
    private final BedwarsActive game;
    private long stageBeginTime;

    public GameEventManager(World world, Queue<StageEvent> stages, BedwarsActive game, GameActivity activity) {
        this.world = world;
        this.events = stages;
        this.game = game;
        this.stageBeginTime = world.getTime();
        activity.listen(GameActivityEvents.TICK, this::tick);
    }

    private void tick() {
        if (events.isEmpty()) return;
        StageEvent stage = events.peek();
        if (stage.getTimeToWait() + stageBeginTime <= world.getTime()) {
            stage.run(game);
            events.poll();
            stageBeginTime = world.getTime();
        }
    }

    public Text getStageStatue(BedwarsActive game) {
        return events.isEmpty() ? Text.empty() : TextUtilities.concatenate(
                events.peek().getStageName(game),
                Text.translatable("events.bedwars.in"),
                getTime(stageBeginTime + events.peek().getTimeToWait()  - world.getTime()));
    }

    private Text getTime(long time) {
        var seconds = time / 20;
        seconds++;
        var minutes = seconds / 60;
        seconds %= 60;
        return Text.literal(String.format("%02d:%02d", minutes, seconds)).formatted(Formatting.GREEN);
    }
}
