package fr.delta.bedwars.game.behavior;

import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.game.TeleporterLogic;
import fr.delta.bedwars.game.component.TeamComponents;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.map.BedwarsMap;
import fr.delta.bedwars.game.ui.PlayerCustomPacketsSender;
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
import xyz.nucleoid.plasmid.game.GameSpacePlayers;
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
    private final GameSpacePlayers players;
    private final GameActivity activity;
    private final Vec3d respawnPos;

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
        this.players = activity.getGameSpace().getPlayers();
        this.activity = activity;
        this.respawnPos = map.waiting().center();
        //register events
        activity.listen(PlayerDeathEvent.EVENT, this::onPlayerDeath);
        activity.listen(GameActivityEvents.TICK, this::tick);
    }

    public ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source)
    {
        if(isAlive(player))
        {
            var teamKey = teamManager.teamFor(player);
            var bed = teamComponentsMap.get(teamKey).bed;
            ServerPlayerEntity attacker = null;

            if (source.getAttacker() != null) {
                if (source.getAttacker() instanceof ServerPlayerEntity adversary) {
                    attacker = adversary;
                }
            } else if (player.getPrimeAdversary() != null && player.getPrimeAdversary() instanceof ServerPlayerEntity adversary) {
                attacker = adversary;
            }
            activity.invoker(BedwarsEvents.PLAYER_DEATH).onDeath(player, source, attacker, bed.isBroken());

            if(bed.isBroken()) {
                //this is a final kill just remove it from the game, this may need to be moved to a dedicated listener
                teamManager.removePlayer(player);
            }
            else {
                var title = Text.translatable("death.bedwars.title").setStyle(Style.EMPTY.withColor(Formatting.RED));
                PlayerCustomPacketsSender.showTitle(player, title, 0, 20 * 6, 1);
                deadPlayers.add(new DeadPlayer(player, world.getTime()));
            }
            activity.invoker(BedwarsEvents.AFTER_PLAYER_DEATH).afterPlayerDeath(player, source, attacker, bed.isBroken());
        }
        spawnSpec(player);
        return ActionResult.FAIL;
    }

    private void tick()
    {
        //kill all player under 0
        for(var player : players)
        {
            if(player.getPos().getY() > 0) continue;

            onPlayerDeath(player, DamageSource.OUT_OF_WORLD);
        }

        //do the dead animation
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

            var subtitle = TextUtilities.concatenate(Text.translatable("death.bedwars.subtitleBeginning").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)),
                                                                    Text.literal(String.valueOf(timeBeforeRespawn / 20 )).setStyle(Style.EMPTY.withColor(Formatting.RED)),
                                                                    Text.translatable("death.bedwars.subtitleEnd").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
            PlayerCustomPacketsSender.changeSubtitle(deadPlayer.player, subtitle);
            deadPlayer.player.sendMessage(subtitle);
        }
    }

    public boolean isAlive(ServerPlayerEntity player)
    {
        for(var deadPlayer : deadPlayers)
        {
            if(deadPlayer.player == player)
                return false;
        }
        return true;
    }

    public void spawnSpec(ServerPlayerEntity player)
    {
        player.changeGameMode(GameMode.SPECTATOR);
        TeleporterLogic.spawnPlayer(player, respawnPos, world);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;
        player.setHealth(20.0f);
    }

    public void respawnPlayer(ServerPlayerEntity player)
    {
        PlayerCustomPacketsSender.showTitle(player, Text.translatable("death.bedwars.respawn").setStyle(Style.EMPTY.withColor(Formatting.GREEN)), 0 ,20 ,20);
        PlayerCustomPacketsSender.changeSubtitle(player, Text.empty());
        var teamKey = teamManager.teamFor(player);
        var spawn = teamComponentsMap.get(teamKey).spawn;
        spawn.spawnPlayer(player);
    }
}
