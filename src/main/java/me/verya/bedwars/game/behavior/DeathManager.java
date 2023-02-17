package me.verya.bedwars.game.behavior;

import me.verya.bedwars.TextUtilities;
import me.verya.bedwars.game.TeamComponents;
import me.verya.bedwars.game.TeleporterLogic;
import me.verya.bedwars.game.map.BedwarsMap;
import me.verya.bedwars.game.ui.PlayerPackets;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//in charge of death, final elimination, reconnection, spec connection handling
public class DeathManager {
    static final int RESPAWN_TIME = 5;
    private final Map<GameTeamKey, TeamComponents> teamComponentsMap;
    private final TeamManager teamManager;
    private final ServerWorld world;
    private final BedwarsMap map;

    static class DeadPlayer
    {
        public ServerPlayerEntity player;
        public Long dateOfDeath;
        public DeadPlayer(ServerPlayerEntity player, Long dateOfDeath)
        {
            this.player = player;
            this.dateOfDeath = dateOfDeath;
        }
    }
    private final List<DeadPlayer> deadPlayers = new ArrayList<>();
    public DeathManager(Map<GameTeamKey, TeamComponents> teamComponentsMap, TeamManager teamManager, ServerWorld world, BedwarsMap map, GameActivity activity)
    {
        this.teamComponentsMap = teamComponentsMap;
        this.teamManager = teamManager;
        this.world = world;
        this.map = map;
        //register events
        activity.listen(PlayerDeathEvent.EVENT, this::onPlayerDeath);
        activity.listen(GameActivityEvents.TICK, this::tick);
    }

    public ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source)
    {
        var teamKey = teamManager.teamFor(player);
        var bed = teamComponentsMap.get(teamKey).bed;
        if(bed.isBroken())
        {
            teamManager.removePlayer(player);
        }
        else
        {
            deadPlayers.add(new DeadPlayer(player, world.getTime()));
        }
        spawnSpec(player);
        return ActionResult.FAIL;
    }

    private void tick()
    {
        var iter = deadPlayers.listIterator();
        while(iter.hasNext())
        {
            var deadPlayer = iter.next();
            long timeBeforeRespawn = deadPlayer.dateOfDeath + RESPAWN_TIME * 20 + 1 - world.getTime();
            if(timeBeforeRespawn < 1)
            {
                iter.remove();
                respawnPlayer(deadPlayer.player);
                continue;
            }
            //send a death message every second
            if(timeBeforeRespawn % 20 != 0) continue;
            var title = Text.translatable("death.bedwars.title").setStyle(Style.EMPTY.withColor(Formatting.RED));
            var subtitle = TextUtilities.concatenate(Text.translatable("death.bedwars.subtitleBeginning").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)),
                                                                    Text.literal(String.valueOf(timeBeforeRespawn / 20 )).setStyle(Style.EMPTY.withColor(Formatting.RED)),
                                                                    Text.translatable("death.bedwars.subtitleEnd").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
            PlayerPackets.showTitle(deadPlayer.player, title, subtitle, 0, 20, 20);
        }
    }

    public void spawnSpec(ServerPlayerEntity player)
    {
        TeleporterLogic.spawnPlayer(player, map.waiting().center(), world);
        player.changeGameMode(GameMode.SPECTATOR);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;
        player.setHealth(20.0f);
    }

    public void respawnPlayer(ServerPlayerEntity player)
    {
        player.changeGameMode(GameMode.SURVIVAL);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;
        player.setHealth(20.0f);
        var teamKey = teamManager.teamFor(player);
        var spawn = teamComponentsMap.get(teamKey).spawn;
        spawn.spawnPlayer(player);
    }
}
