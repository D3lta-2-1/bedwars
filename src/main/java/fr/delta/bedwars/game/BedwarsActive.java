package fr.delta.bedwars.game;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import fr.delta.bedwars.BedwarsConfig;
import fr.delta.bedwars.GameRules;
import fr.delta.bedwars.StageEvent.GameEventManager;
import fr.delta.bedwars.game.behaviour.*;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.player.InventoryManager;
import fr.delta.bedwars.game.resourceGenerator.ResourceGenerator;
import fr.delta.bedwars.game.shop.data.ShopConfigs;
import fr.delta.bedwars.game.shop.npc.ShopKeeper;
import fr.delta.bedwars.game.ui.FeedbackMessager;
import fr.delta.bedwars.TextUtilities;
import fr.delta.bedwars.game.component.TeamComponents;
import fr.delta.bedwars.game.shop.ShopMenu.ItemShopMenu;
import fr.delta.bedwars.game.ui.BedwarsSideBar;
import fr.delta.bedwars.game.map.BedwarsMap;
import fr.delta.notasword.NotASword;
import fr.delta.notasword.OldAttackSpeed;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

import java.util.*;

public class BedwarsActive {
    final private GameSpace gameSpace;
    final private BedwarsMap gameMap;
    final private BedwarsConfig config;
    final private ServerWorld world;
    private GameActivity activity;
    final private TeamManager teamManager;
    final private Multimap<GameTeam, ServerPlayerEntity> teamPlayersMap;
    final private Map<GameTeamKey, TeamComponents> teamComponentsMap;
    final private List<GameTeam> teamsInOrder;
    final private ClaimManager claim;
    final private DeathManager deathManager;
    final private DefaultSwordManager defaultSwordManager;
    final private InventoryManager inventoryManager;
    final private Multimap<String, ResourceGenerator> middleGeneratorsMap;

    BedwarsActive(GameSpace gameSpace, BedwarsMap gameMap, ServerWorld world, Multimap<GameTeam, ServerPlayerEntity> teamPlayers, List<GameTeam> teamsInOrder, BedwarsConfig config)
    {
        //set members
        this.gameSpace = gameSpace;
        this.gameMap = gameMap;
        this.config = config;
        this.world = world;
        this.teamPlayersMap = teamPlayers;
        this.teamsInOrder = teamsInOrder;
        gameSpace.setActivity(gameActivity -> activity = gameActivity);
        setupGameRules(); //set gameRules
        this.claim = new ClaimManager(gameMap, config, activity);
        this.teamManager = TeamManager.addTo(activity);
        setupTeam(teamPlayers); //populate teamManager
        this.teamComponentsMap = makeTeamComponents(); //forge bed, spawn ect
        this.deathManager = new DeathManager(teamComponentsMap, teamManager, world, gameMap, config, activity);
        this.defaultSwordManager = new DefaultSwordManager(activity);
        this.inventoryManager = new InventoryManager(deathManager, teamPlayersMap, teamManager, teamComponentsMap, defaultSwordManager, activity);
        this.middleGeneratorsMap = addMiddleGenerator();
        addShopkeepers(gameMap.ShopKeepers());
        var stageManager = new GameEventManager(world, new LinkedList<>(config.events()), this, activity);
        BedwarsSideBar.build(teamComponentsMap, teamManager, teamsInOrder, stageManager, this, activity);
        new FeedbackMessager(teamManager, activity);
        new WinEventSender(teamsInOrder, teamManager, activity);
        new OldAttackSpeed(20D ,activity);
        new InvisibilityArmorHider(teamManager, activity);
        new ChestLocker(teamComponentsMap, teamManager, activity);
        destroySpawn();
        startGame();
        breakBedForEmptyTeam();
        activity.listen(BedwarsEvents.TEAM_WIN, this::onTeamWin);
    }
    private void setupGameRules()
    {
        //set gameRules
        activity.deny(GameRuleType.CRAFTING);
        activity.deny(GameRuleType.PORTALS);
        activity.deny(GameRuleType.HUNGER);
        activity.allow(GameRuleType.PLAYER_PROJECTILE_KNOCKBACK);
        activity.allow(GameRuleType.TRIDENTS_LOYAL_IN_VOID);
        activity.deny(GameRuleType.MODIFY_ARMOR);
        activity.deny(GameRuleType.SATURATED_REGENERATION);
        activity.deny(NotASword.ATTACK_SOUND);
        activity.allow(NotASword.OLD_KNOCKBACK);
        activity.deny(GameRules.BED_INTERACTION);
        activity.allow(GameRules.BLAST_PROOF_GLASS_RULE);
        activity.deny(GameRules.ENDER_PEARL_DAMAGE);
        activity.deny(GameRules.RECIPE_BOOK_USAGE);
        activity.allow(GameRules.AMPLIFIED_EXPLOSION_KNOCKBACK);
        activity.allow(GameRules.REDUCED_EXPLOSION_DAMAGE);
        GameProperties.add(activity);
    }

    private void setupTeam(Multimap<GameTeam, ServerPlayerEntity> teamPlayers)
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
        var builder = new TeamComponents.Builder(teamManager, activity, world, claim, config, gameMap);
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

    private void addShopkeepers(List<BlockBounds> shopkeepersBounds)
    {
        var entries = ShopConfigs.ENTRIES_REGISTRY.get(config.shopEntriesId());
        if(entries == null) throw new NullPointerException("entries is null");
        ShopConfigs.initialize(entries, this);
        var categories = ShopConfigs.CATEGORIES_REGISTRY.get(config.shopCategoriesId());
        var menu = new ItemShopMenu(this, entries, categories, activity);
        for(var shopkeeper : shopkeepersBounds)
        {
           ShopKeeper.createShopKeeper(world, shopkeeper, claim, menu);
        }
    }

    private Multimap<String, ResourceGenerator> addMiddleGenerator()
    {
        Multimap<String, ResourceGenerator> middleGeneratorsMap = ArrayListMultimap.create();
        for(var generatorType : config.generatorTypeList())
        {
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
        //todo : add messages into Messenger Class
        gameSpace.getPlayers().sendMessage(TextUtilities.getTranslation("name", team.config().blockDyeColor().name()).append(" win"));
        new BedwarsEnd(gameSpace, world, teamPlayersMap, team);
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

    public DefaultSwordManager getDefaultSwordManager() { return defaultSwordManager; }

    public InventoryManager getInventoryManager()
    {
        return inventoryManager;
    }

    public GameActivity getActivity() { return activity; }

    public Multimap<GameTeam, ServerPlayerEntity> getTeamPlayersMap() {
        return teamPlayersMap;
    }

    public Multimap<String, ResourceGenerator> getGeneratorsMap() {
        return middleGeneratorsMap;
    }
}
