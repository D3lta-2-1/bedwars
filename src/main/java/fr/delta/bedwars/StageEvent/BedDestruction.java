package fr.delta.bedwars.StageEvent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.event.BedwarsEvents;
import net.minecraft.text.Text;

public record BedDestruction(int time) implements GameEvent {

    public static final Codec<BedDestruction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("time").forGetter(BedDestruction::time)
    ).apply(instance, BedDestruction::new));

    @Override
    public int getTimeToWait() {
        return time;
    }

    @Override
    public Text getStageName(BedwarsActive game) {
        return Text.translatable("events.bedwars.bedDestructionIn");
    }

    @Override
    public void run(BedwarsActive game)
    {
        for(var team : game.getTeamManager())
        {
            var  teamComponent = game.getTeamComponentsFor(team);
            if(teamComponent == null) continue;
            teamComponent.bed.breakIt(game.getWorld(), null);
            //breaker is null so no sound will be played
        }
        game.getActivity().invoker(BedwarsEvents.BED_DESTRUCTION).onDestruction();
    }
}
