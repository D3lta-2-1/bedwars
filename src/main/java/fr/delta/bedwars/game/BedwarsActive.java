package fr.delta.bedwars.game;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.codec.BedwarsConfig;
import fr.delta.bedwars.GameRules;
import fr.delta.bedwars.StageEvent.StageEvent;
import fr.delta.bedwars.StageEvent.GameEventManager;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamChat;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.*;

public class BedwarsActive {
    final private GameSpace gameSpace;
    final private BedwarsMap gameMap;
    final private BedwarsConfig config;
    final private ServerWorld world;
    private GameActivity activity;
    final private TeamManager teamManager;
    final private Multimap<GameTeam, PlayerRef> teamPlayersMap;
    final private Map<GameTeamKey, TeamComponents> teamComponentsMap;
    final private List<GameTeam> teamsInOrder;
    final private ClaimManager claim;
    final private DeathManager deathManager;
    final private SwordManager defaultSwordManager;
    final private CompassManager compassManager;
    final private InventoryManager inventoryManager;
    final private Multimap<String, ResourceGenerator> middleGeneratorsMap;


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
        gameSpace.setActivity(gameActivity -> activity = gameActivity);
        //var statisticBundle = gameSpace.getStatistics().bundle(Bedwars.ID);
        //statisticBundle.forPlayer().

        //set gameRules
        setupGameRules();

        //setup things that aren't related to teams
        this.claim = new ClaimManager(gameMap, config, activity);
        this.middleGeneratorsMap = addMiddleGenerator();
        this.defaultSwordManager = new SwordManager(this, activity);
        this.compassManager = new CompassManager(this, activity);

        //this must be setup before the teamManager to ensure event are fired in the right order
        this.deathManager = new DeathManager(this, teamPlayersMap, world, gameMap, config, activity);
        this.inventoryManager = new InventoryManager(deathManager, this, config, activity);
        new FeedbackMessager(this, teamPlayersMap, world, activity);

        //setup teamManager only here because of automated remove of player from team when leaving the gameSpace
        this.teamManager = TeamManager.addTo(activity);
        TeamChat.addTo(activity, teamManager);
        setupTeam(teamPlayers); //populate teamManager
        this.teamComponentsMap = makeTeamComponents(); //forge bed, spawn ect

        //split the inventory manager init in to part cause of event priority
        this.inventoryManager.init(teamPlayersMap);

        var queue = loadEvents();
        //things that aren't store as private members
        var stageManager = new GameEventManager(world, queue, this, activity);
        BedwarsSideBar.build(teamComponentsMap, teamManager, teamsInOrder, stageManager, this, activity);

        new WinChecker(teamsInOrder, teamManager, activity);
        new OldAttackSpeed(20D ,activity);
        new InvisibilityArmorHider(teamManager, activity);
        new ChestLocker(teamComponentsMap, teamManager, activity);
        addShopkeepers();
        destroySpawn();
        startGame();
        breakBedForEmptyTeam();

        //should be the last thing registered to enforce the order of the events
        BedwarsStatistic.addTo(gameSpace.getStatistics().bundle(Bedwars.ID), activity);
        activity.listen(BedwarsEvents.TEAM_WIN, this::onTeamWin);
    }
    private void setupGameRules()
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
        activity.deny(GameRules.BED_INTERACTION);
        activity.allow(GameRules.BLAST_PROOF_GLASS_RULE);
        activity.deny(GameRules.ENDER_PEARL_DAMAGE);
        activity.deny(GameRules.RECIPE_BOOK_USAGE);
        activity.allow(GameRules.AMPLIFIED_EXPLOSION_KNOCKBACK);
        activity.allow(GameRules.REDUCED_EXPLOSION_DAMAGE);
        activity.deny(GameRules.FIRE_SPREAD);
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

    private Map<GameTeamKey, TeamComponents> makeTeamComponents()
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

    private void addShopkeepers()
    {
        var entries = AdditionalDataLoader.SHOP_ENTRIES_REGISTRY.get(config.shopEntriesId());
        if(entries == null) throw new NullPointerException(config.shopEntriesId().toString() + " entries does not exist");
        AdditionalDataLoader.initialize(entries, this);
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

    private Multimap<String, ResourceGenerator> addMiddleGenerator()
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
    public GameTeam getTeamForPlayer(ServerPlayerEntity player)
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

    public SwordManager getDefaultSwordManager() { return defaultSwordManager; }
    public CompassManager getCompassManager() { return compassManager; }

    public InventoryManager getInventoryManager()
    {
        return inventoryManager;
    }

    public GameActivity getActivity() { return activity; }

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

    public void addPlayerToTeam(ServerPlayerEntity player, GameTeam team)
    {
        teamManager.addPlayerTo(player, team.key());
    }

    public void removePlayerFromTeam(ServerPlayerEntity player)
    {
        var team = teamManager.teamFor(player);
        teamManager.removePlayerFrom(player, team);
    }
    public PlayerSet getPlayersInTeam(GameTeam team)
    {
        return teamManager.playersIn(team.key());
    }

    public PlayerSet getPlayersInTeam(GameTeamKey team)
    {
        return teamManager.playersIn(team);
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
}
