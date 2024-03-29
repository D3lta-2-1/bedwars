package fr.delta.bedwars.game.behaviour;

import com.google.common.collect.Multimap;
import fr.delta.bedwars.codec.BedwarsConfig;
import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.TeleporterLogic;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.map.BedwarsMap;
import fr.delta.bedwars.game.ui.PlayerCustomPacketsSender;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpacePlayers;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;

//in charge of death, final elimination, and reconnection, spec connection handling
public class DeathManager {
    static final int RESPAWN_TIME = 5;
    private final BedwarsActive game;
    private final ServerWorld world;
    private final GameSpacePlayers players;
    private final GameActivity activity;
    private final Vec3d respawnPos;
    private final int voidHigh;
    private final Multimap<GameTeam, PlayerRef> teamPlayersMap;

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
    public DeathManager(BedwarsActive bedwarsGame, Multimap<GameTeam, PlayerRef> teamPlayersMap, ServerWorld world, BedwarsMap map, BedwarsConfig config, GameActivity activity)
    {
        this.game = bedwarsGame;
        this.world = world;
        this.players = activity.getGameSpace().getPlayers();
        this.activity = activity;
        this.respawnPos = map.waiting().center();
        this.voidHigh = config.voidHigh();
        this.teamPlayersMap = teamPlayersMap;
        //register events
        activity.listen(PlayerDeathEvent.EVENT, this::onPlayerDeath);
        activity.listen(GameActivityEvents.TICK, this::tick);
        activity.listen(GamePlayerEvents.LEAVE, this::onPlayerLeft);
        activity.listen(GamePlayerEvents.OFFER, this::onPlayerJoin);
        activity.listen(BedwarsEvents.BED_BROKEN, this::onBedBroken);
        activity.listen(BedwarsEvents.AFTER_PLAYER_DEATH, (player, source, killer, isFinal) -> checkForWin());
    }

    private Pair<DamageSource, ServerPlayerEntity> getDeathContext(ServerPlayerEntity player, DamageSource source)
    {
        ServerPlayerEntity attacker = null;

        if (source.getAttacker() != null && source.getAttacker() instanceof ServerPlayerEntity adversary && adversary != player)
            attacker = adversary;
        else if (player.getPrimeAdversary() != null && player.getPrimeAdversary() instanceof ServerPlayerEntity adversary && adversary != player)
            attacker = adversary;

        boolean shouldNotHaveAnAttacker = attacker != null && isAlive(attacker) && game.getTeamForPlayer(player) == game.getTeamForPlayer(attacker);
        if(shouldNotHaveAnAttacker)
            attacker = null;

        return new Pair<>( shouldNotHaveAnAttacker ? overwriteSource(source, player.getDamageSources()) : source, attacker);
    }

    private DamageSource overwriteSource(DamageSource source, DamageSources sources)
    {
        if(source.isOf(DamageTypes.FIREBALL)) return sources.fireball((AbstractFireballEntity)source.getSource(), null);
        return source;
    }

    public ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source)
    {
        if(isAlive(player))
        {
            var deathContext = getDeathContext(player, source);

            var bed = game.getTeamComponentsFor(player).bed;

            activity.invoker(BedwarsEvents.PLAYER_DEATH).onDeath(player, deathContext.getLeft(), deathContext.getRight(), bed.isBroken());

            if(bed.isBroken()) {
                //this is a final kill just remove it from the game
                game.removePlayerFromTeam(PlayerRef.of(player));
            }
            else {
                var title = Text.translatable("death.bedwars.title").formatted(Formatting.RED);
                PlayerCustomPacketsSender.showTitle(player, title, 0, 20 * 6, 1);
                deadPlayers.add(new DeadPlayer(player, world.getTime()));
            }
            activity.invoker(BedwarsEvents.AFTER_PLAYER_DEATH).afterPlayerDeath(player, deathContext.getLeft(), deathContext.getRight(), bed.isBroken());
        }
        spawnSpec(player);
        return ActionResult.FAIL;
    }

    private void tick()
    {
        //kill all player under 0
        for(var player : players)
        {
            if(player.getY() > voidHigh) continue;
            onPlayerDeath(player, player.getDamageSources().outOfWorld());
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

            var subtitle = TextUtilities.concatenate(Text.translatable("death.bedwars.youWillRespawn", Text.literal(String.valueOf(timeBeforeRespawn / 20 )).formatted(Formatting.RED)).formatted(Formatting.YELLOW));
            PlayerCustomPacketsSender.changeSubtitle(deadPlayer.player, subtitle);
            deadPlayer.player.sendMessage(subtitle);
        }
    }

    private void onPlayerLeft(ServerPlayerEntity player)
    {
        if(isAlive(player))
        {
            var deathContext = getDeathContext(player, player.getDamageSources().outOfWorld()); //TODO: implement a fancy damage source for this

            var bed = game.getTeamComponentsFor(player).bed;
            activity.invoker(BedwarsEvents.PLAYER_DEATH).onDeath(player, deathContext.getLeft(), deathContext.getRight(), bed.isBroken());
            if(bed.isBroken()) {
                //this is a final kill just remove it from the game
                game.removePlayerFromTeam(PlayerRef.of(player));
            }
            activity.invoker(BedwarsEvents.AFTER_PLAYER_DEATH).afterPlayerDeath(player, deathContext.getLeft(), deathContext.getRight(), bed.isBroken());
            //no need to do anything to remove it, the player will be removed from the game by the team manager by the death event if needed
        }
        else
        {
            deadPlayers.removeIf(deadPlayer -> deadPlayer.player == player);
        }
    }

    private void onBedBroken(GameTeam team, ServerPlayerEntity $)
    {
        var players = game.getTeamManager().allPlayersIn(team.key());
        for(var player : players)
        {
            if(!game.getPlayers().contains(player))
            {
                game.removePlayerFromTeam(player); //eliminate the player if he is not in the game
            }
        }
        checkForWin();
    }

    public boolean isAlive(ServerPlayerEntity player)
    {
        for(var deadPlayer : deadPlayers)
        {
            if(deadPlayer.player == player)
                return false;
        }
        return game.getTeamForPlayer(player) != null;
    }

    public void spawnSpec(ServerPlayerEntity player)
    {
        player.closeHandledScreen();
        PlayerCustomPacketsSender.showOverlay(player, Text.empty());
        player.clearStatusEffects();
        player.changeGameMode(GameMode.SPECTATOR);
        TeleporterLogic.spawnPlayer(player, respawnPos, world);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;
        player.setHealth(20.0f);
    }

    public void respawnPlayer(ServerPlayerEntity player)
    {
        PlayerCustomPacketsSender.showTitle(player, Text.translatable("death.bedwars.respawn").formatted(Formatting.GREEN), 0 ,20 ,20);
        PlayerCustomPacketsSender.changeSubtitle(player, Text.empty());
        var spawn = game.getTeamComponentsFor(player).spawn;
        spawn.spawnPlayer(player);
    }

    private PlayerOfferResult onPlayerJoin(PlayerOffer offer)
    {
        //check if the player was assigned to a team, we can't use the team manager because it's always refer to in-game players
        var offeredPlayer = offer.player();
        GameTeam team = null;
        for(var entry : teamPlayersMap.entries())
        {
            var player = entry.getValue();
            if(PlayerRef.of(offeredPlayer).equals(player))
            {
                team = entry.getKey();
                break;
            }
        }
        //do the player was even in game? or have he a bed?
        if(team == null || game.getTeamComponentsFor(team).bed.isBroken())
            return offer.accept(world, respawnPos).and(() -> spawnSpec(offeredPlayer)); //make him a spectator, we could also just deny him

        //he was in game, so we need to respawn him
        //final var teamFinal = team; //we need to make a final copy of the team because it's used in the lambda
        return offer.accept(world, respawnPos).and(() -> {
            spawnSpec(offeredPlayer);
            //game.addPlayerToTeam(offeredPlayer, teamFinal); //team manager should do this by itself
            var title = Text.translatable("death.bedwars.title").formatted(Formatting.RED);
            PlayerCustomPacketsSender.showTitle(offeredPlayer, title, 0, 20 * 6, 1);
            deadPlayers.add(new DeadPlayer(offeredPlayer, world.getTime()));
        });
    }
    void checkForWin()
    {
        GameTeam lastTeam = null;
        for(var team : game.getTeamManager())
        {
            if(game.getTeamManager().playersIn(team.key()).size() !=0)
            {
                if(lastTeam == null)
                    lastTeam = team;
                else
                {
                    return; //more than one team alive
                }
            }
        }
        activity.invoker(BedwarsEvents.TEAM_WIN).onTeamWin(lastTeam);
    }
}
