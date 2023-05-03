package fr.delta.bedwars.game.teamComponent;

import fr.delta.bedwars.game.behaviour.DeathManager;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.traps.Trap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;

import java.util.*;

public class TrapHandler {
    private final BlockBounds bounds;
    private final ServerWorld world;
    private final TeamManager teamManager;
    private final DeathManager deathManager;
    private final GameTeam team;
    private final List<Trap> traps = new ArrayList<>();
    private final Map<ServerPlayerEntity, Pair<Trap, Long>> playerTrapMap = new HashMap<>();
    private long playAlarmToTick = 0;
    private final BlockPos alarmPos;


    public static BlockBounds createFromBedBlockBound(BlockBounds bedBounds)
    {
        var min = bedBounds.min().add(-5, -5, -5); //add 5 blocks all around the bed
        var max = bedBounds.max().add(5,5 ,5);
        return BlockBounds.of(min, max);
    }

    public TrapHandler(BlockBounds bounds, ServerWorld world, TeamManager teamManager, GameTeam team, DeathManager deathManager, GameActivity activity)
    {
        this.bounds = bounds;
        this.world = world;
        this.teamManager = teamManager;
        this.deathManager = deathManager;
        this.team = team;
        this.alarmPos = new BlockPos((int)bounds.center().x, (int)bounds.center().y, (int)bounds.center().z);
        activity.listen(GameActivityEvents.TICK, this::tick);
        //remove the trap when the player dies
        activity.listen(BedwarsEvents.PLAYER_DEATH, (player, source, killer, isFinalKill) -> playerTrapMap.remove(player));
    }

    void tick()
    {
        //process triggered traps
        for(var entries : playerTrapMap.entrySet())
        {
            var player = entries.getKey();
            var trap = entries.getValue().getLeft();
            var triggerTime = entries.getValue().getRight();
            if(triggerTime + trap.getCooldown() < world.getTime())
            {
                //trap cooldown is over
               playerTrapMap.remove(player);
            }

        }
        //check if a player triggered a trap
        for(var team : teamManager)
        {
            if(team == this.team) continue; //skip the team that owns the trap
            for(var player : teamManager.playersIn(team.key()))
            {
                if(bounds.asBox().contains(player.getPos()) && deathManager.isAlive(player)) //Trigger the trap
                {
                    if(playerTrapMap.containsKey(player)) continue;
                    var trap = peekTrap();
                    if(trap == null) continue; //no trap to trigger
                    triggerTrap(player, trap);
                }
            }
        }
        tickAlarm();
    }

    private void triggerTrap(ServerPlayerEntity player, Trap trap)
    {
        playAlarmToTick = trap.trigger(teamManager, team.key(), player) + world.getTime();
        var players = teamManager.playersIn(team.key());
        players.showTitle(Text.translatable("trap.bedwars.triggered").formatted(Formatting.RED),
                            Text.translatable("trap.bedwars.trapSetOff", trap.getName()),
                            0, 60, 20);
        players.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT);
        playerTrapMap.put(player, new Pair<>(trap, world.getTime()));
    }

    private void tickAlarm()
    {
        if(playAlarmToTick > world.getTime())
        {
            if(world.getTime() % 2 == 0)
                world.playSound(null, alarmPos, SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.BLOCKS, 1.f, 1.8f);
            else
                world.playSound(null, alarmPos, SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.BLOCKS, 1.f, 1.6f);
        }
    }

    private Trap peekTrap()
    {
        if(traps.isEmpty())
            return null;
        return traps.remove(0);
    }

    public boolean isTrapQueueFull()
    {
        return traps.size() >= 3;
    }

    public void addTrap(Trap trap)
    {
        traps.add(trap);
    }

    public Iterator<Trap> iterator()
    {
        return traps.iterator();
    }

    public int getTrapCount()
    {
        return traps.size();
    }
}
