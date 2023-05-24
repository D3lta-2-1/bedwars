package fr.delta.bedwars.game;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.BedwarsActiveTracker;
import fr.delta.bedwars.codec.BedwarsConfig;
import fr.delta.bedwars.GameRules;
import fr.delta.bedwars.StageEvent.StageEvent;
import fr.delta.bedwars.StageEvent.GameEventManager;
import fr.delta.bedwars.data.ShopEntryGetter;
import fr.delta.bedwars.game.behaviour.*;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.player.InventoryManager;
import fr.delta.bedwars.game.resourceGenerator.ResourceGenerator;
import fr.delta.bedwars.game.shop.ShopMenu.TeamShopMenu;
import fr.delta.bedwars.data.AdditionalDataLoader;
import fr.delta.bedwars.game.shop.npc.ShopKeeper;
import fr.delta.bedwars.game.ui.FeedbackMessager;
import fr.delta.bedwars.game.teamComponent.TeamComponents;
import fr.delta.bedwars.game.shop.ShopMenu.ItemShopMenu;
import fr.delta.bedwars.game.ui.BedwarsSideBar;
import fr.delta.bedwars.game.map.BedwarsMap;
import fr.delta.notasword.NotASword;
import fr.delta.notasword.OldAttackSpeed;
import net.minecraft.block.Blocks;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamChat;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;

import java.util.*;

public class BedwarsActive {
    final private GameSpace gameSpace;
    private GameActivity activity;
    final private BedwarsMap gameMap;
    final private BedwarsConfig config;
    final private ServerWorld world;
    private TeamManager teamManager;
    final private Multimap<GameTeam, PlayerRef> teamPlayersMap;
    private Map<GameTeamKey, TeamComponents> teamComponentsMap;
    final private List<GameTeam> teamsInOrder;
    private ClaimManager claim;
    private DeathManager deathManager;
    private SwordManager defaultSwordManager;
    private CompassManager compassManager;
    private InventoryManager inventoryManager;
    private Multimap<String, ResourceGenerator> middleGeneratorsMap;

    BedwarsActive(GameSpace gameSpace, BedwarsMap gameMap, ServerWorld world, Multimap<GameTeam, PlayerRef> teamPlayers, List<GameTeam> teamsInOrder, BedwarsConfig config)
    {
        //set members
        this.gameSpace = gameSpace;
        this.gameMap = gameMap;
        this.config = config;
        this.world = world;
        this.teamPlayersMap = teamPlayers;
        this.teamsInOrder = teamsInOrder;
        //recreate new activity
        //a lot of this field have to be final, but because the use of a lambda, they can't be
        //I saw in other game that a static function is used to create the activity, In my case, I can't do that because I need to ensure some priority in the event
        gameSpace.setActivity(activity -> {
            //set gameRules
            this.activity = activity;
            //setup things that aren't related to teams
            this.claim = new ClaimManager(gameMap, config, activity);
            this.middleGeneratorsMap = addMiddleGenerator(activity);
            this.defaultSwordManager = new SwordManager(this, activity);
            this.compassManager = new CompassManager(this, activity);
            this.teamManager = TeamManager.addTo(activity);
            this.deathManager = new DeathManager(this, teamPlayers, world, gameMap, config, activity);
            setupTeam(teamPlayers); //populate teamManager
            this.teamComponentsMap = makeTeamComponents(activity); //forge bed, spawn ect
            this.inventoryManager = new InventoryManager(deathManager, this, config, activity);

            new FeedbackMessager(this, teamPlayers, activity);
            TeamChat.addTo(activity, teamManager);

            var queue = loadEvents();
            //things that aren't store as private members
            var stageManager = new GameEventManager(world, queue, this, activity);
            var sideBarWidget = BedwarsSideBar.build(teamComponentsMap, teamManager, teamsInOrder, stageManager, this);
            GlobalWidgets.addTo(activity).addWidget(sideBarWidget);

            OldAttackSpeed.add(20D, activity);
            new InvisibilityArmorHider(teamManager, activity);
            new ChestLocker(teamComponentsMap, teamManager, activity);
            activity.listen(PlayerDamageEvent.EVENT, (player, source, amount) -> { //disable friendly fire
                var protectedFrom = List.of(DamageTypes.PLAYER_ATTACK, DamageTypes.ARROW); //could be extended to other damage type
                for(var type : protectedFrom)
                {
                    if(!source.isOf(type)) continue;
                    var attackerEntity = source.getAttacker();
                    if(attackerEntity instanceof ServerPlayerEntity attacker)
                    {
                        var attackerTeam = teamManager.teamFor(attacker);
                        var playerTeam = teamManager.teamFor(player);
                        if(attackerTeam == playerTeam) return ActionResult.FAIL;
                    }
                }
                return ActionResult.PASS;
            });

            addShopkeepers(activity);
            destroySpawn();
            startGame();
            breakBedForEmptyTeam();

            BedwarsStatistic.addTo(gameSpace.getStatistics().bundle(Bedwars.ID), activity);
            setupGameRules(activity);
            //should be the last thing registered to enforce the order of the events
            activity.listen(BedwarsEvents.TEAM_WIN, this::onTeamWin);
        });
        BedwarsActiveTracker.getInstance().addGame(gameSpace, this);
    }
    private static void setupGameRules(GameActivity activity)
    {
        activity.deny(GameRuleType.CRAFTING);
        activity.deny(GameRuleType.PORTALS);
        activity.deny(GameRuleType.HUNGER);
        activity.allow(GameRuleType.PLAYER_PROJECTILE_KNOCKBACK);
        activity.allow(GameRuleType.TRIDENTS_LOYAL_IN_VOID);
        activity.deny(GameRuleType.MODIFY_ARMOR);
        activity.deny(GameRuleType.SATURATED_REGENERATION);

        activity.deny(NotASword.ATTACK_SOUND);
        activity.allow(NotASword.OLD_KNOCKBACK);
        activity.allow(NotASword.FAST_ATTACK);
        activity.deny(NotASword.SWEEPING_EDGE);

        activity.deny(GameRules.BED_INTERACTION);
        activity.allow(GameRules.BLAST_PROOF_GLASS_RULE);
        activity.deny(GameRules.ENDER_PEARL_DAMAGE);
        activity.deny(GameRules.RECIPE_BOOK_USAGE);
        activity.allow(GameRules.AMPLIFIED_EXPLOSION_KNOCKBACK);
        activity.allow(GameRules.REDUCED_EXPLOSION_DAMAGE);
        activity.deny(GameRules.FIRE_SPREAD);
        activity.allow(GameRules.REDUCED_FALL_DAMAGE);
        GameProperties.add(activity);
    }

    private Queue<StageEvent> loadEvents()
    {
        var queue = new LinkedList<StageEvent>();
        for(var gameEventId : config.events())
        {
            var gameEvent = AdditionalDataLoader.GAME_EVENT_REGISTRY.get(gameEventId);
            if(gameEvent == null)
            {
                Bedwars.LOGGER.warn("GameEvent {} not found", gameEventId);
                continue;
            }
            queue.add(gameEvent);
        }
        return queue;
    }

    private void setupTeam(Multimap<GameTeam, PlayerRef> teamPlayers)
    {
        for (GameTeam team : teamPlayers.keySet()) {
            //add players to their team
            teamManager.addTeam(team);
            for (var player : teamPlayers.get(team)) {
                if(player == null) continue;
                teamManager.addPlayerTo(player, team.key());
            }
        }
    }

    private Map<GameTeamKey, TeamComponents> makeTeamComponents(GameActivity activity)
    {
        var forgeConfig = AdditionalDataLoader.FORGE_CONFIG_REGISTRY.get(config.forgeConfigId());
        if(forgeConfig == null)
            throw new NullPointerException(config.forgeConfigId().toString() + " forge does not exist");
        var builder = new TeamComponents.Builder(teamManager, activity, world, claim, deathManager, forgeConfig, gameMap);
        var teamComponentsMap = new HashMap<GameTeamKey, TeamComponents>();
        for(var team : teamManager)
        {
            teamComponentsMap.put(team.key(), builder.createFor(team));
        }
        return teamComponentsMap;
    }

    private void destroySpawn()
    {
        for(var pos : gameMap.waiting())
        {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            gameMap.template().setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }

    private void addShopkeepers(GameActivity activity)
    {
        var entries = new ShopEntryGetter(AdditionalDataLoader.SHOP_ENTRIES_REGISTRY, config.shopEntriesPagesId(), this);

        var categories = AdditionalDataLoader.SHOP_CATEGORIES_REGISTRY.get(config.shopCategoriesId());
        if(categories == null) throw new NullPointerException(config.shopCategoriesId().toString() + " category does not exist");

        var itemShopMenu = new ItemShopMenu(this, entries, categories.ItemShopCategories(), activity);
        for(var shopkeeper : gameMap.itemShopKeepers())
        {
           ShopKeeper.createShopKeeper(world, shopkeeper, claim, itemShopMenu);
        }

        var teamShopMenu = new TeamShopMenu(this, entries, categories.teamUpgrade(), categories.traps(),activity);
        for(var shopkeeper : gameMap.teamShopKeepers())
        {
            ShopKeeper.createShopKeeper(world, shopkeeper, claim, teamShopMenu);
        }
    }

    private Multimap<String, ResourceGenerator> addMiddleGenerator(GameActivity activity)
    {
        Multimap<String, ResourceGenerator> middleGeneratorsMap = ArrayListMultimap.create();
        for(var generatorTypeId : config.generatorTypeIdList())
        {
            var generatorType = AdditionalDataLoader.GENERATOR_TYPE_REGISTRY.get(generatorTypeId);
            if(generatorType == null)
            {
                Bedwars.LOGGER.warn("GeneratorType {} not found", generatorTypeId);
                continue;
            }
            for(var bounds : gameMap.generatorsRegions().get(generatorType.getInternalId()))
            {
                middleGeneratorsMap.put(generatorType.getInternalId(), generatorType.createGenerator(bounds, world, claim, activity));
            }
        }
        return middleGeneratorsMap;
    }

    private void startGame() {
        for(var team : teamManager) {
            var spawn = teamComponentsMap.get(team.key()).spawn;
            for (var player : teamManager.playersIn(team.key())) {
                spawn.spawnPlayer(player);
            }
        }
    }

    void breakBedForEmptyTeam()
    {
        for(var team : teamsInOrder)
        {
            if(teamManager.playersIn(team.key()).size() == 0)
            {
                teamComponentsMap.get(team.key()).bed.breakIt(world, null);
            }
        }
    }

    private void onTeamWin(GameTeam team)
    {
        //end this phase and start tne End phase
        new BedwarsEnd(gameSpace, world);
    }

    //accessors
    public GameTeam getTeamForPlayer(PlayerRef player)
    {
        var teamKey = teamManager.teamFor(player);
        for(var team : teamsInOrder)
        {
            if(team.key() == teamKey)
            {
                return team;
            }
        }
        return null;
    }

    public GameTeam getTeamForPlayer(ServerPlayerEntity player)
    {
       return getTeamForPlayer(PlayerRef.of(player));
    }

    public SwordManager getDefaultSwordManager() { return defaultSwordManager; }
    public CompassManager getCompassManager() { return compassManager; }

    public InventoryManager getInventoryManager()
    {
        return inventoryManager;
    }

    public Multimap<GameTeam, PlayerRef> getTeamPlayersMap() {
        return teamPlayersMap;
    }

    public Multimap<String, ResourceGenerator> getGeneratorsMap() {
        return middleGeneratorsMap;
    }

    public TeamComponents getTeamComponentsFor(ServerPlayerEntity player){
        var team = teamManager.teamFor(player);
        if(team == null) return null;
        return teamComponentsMap.get(team);
    }
    public TeamComponents getTeamComponentsFor(GameTeam team){
        return teamComponentsMap.get(team.key());
    }

    public void removePlayerFromTeam(PlayerRef player)
    {
        teamManager.removePlayer(player);
    }
    public PlayerSet getPlayersInTeam(GameTeam team)
    {
        return teamManager.playersIn(team.key());
    }

    public PlayerSet getPlayersInTeam(GameTeamKey team)
    {
        return teamManager.playersIn(team);
    }

    public PlayerSet getPlayers()
    {
        return gameSpace.getPlayers();
    }

    public TeamManager getTeamManager()
    {
        return teamManager;
    }

    public ServerWorld getWorld()
    {
        return world;
    }

    public List<GameTeam> getTeamsInOrder()
    {
        return teamsInOrder;
    }

    public GameActivity getActivity()
    {
        return activity;
    }

    public boolean couldABlockBePlacedHere(BlockPos pos)
    {
        return claim.isInMapLimits(pos) && !claim.isInClaimedRegion(pos);
    }
    }
