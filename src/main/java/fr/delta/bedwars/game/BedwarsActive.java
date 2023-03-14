package fr.delta.bedwars.game;

import com.google.common.collect.Multimap;
import eu.pb4.sidebars.api.Sidebar;
import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.BedwarsConfig;
import fr.delta.bedwars.game.behavior.ClaimManager;
import fr.delta.bedwars.game.behavior.DeathManager;
import fr.delta.bedwars.game.behavior.DefaultSword;
import fr.delta.bedwars.game.behavior.WinEventSender;
import fr.delta.bedwars.game.event.BedwarsEvents;
import fr.delta.bedwars.game.player.InventoryManager;
import fr.delta.bedwars.game.shop.data.ShopConfigs;
import fr.delta.bedwars.game.shop.npc.ShopKeeper;
import fr.delta.bedwars.game.ui.Messager;
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
import org.samo_lego.taterzens.npc.TaterzenNPC;
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
    final private OldAttackSpeed attackSpeedRestorer;
    final private DefaultSword defaultSwordManager;
    final private InventoryManager inventoryManager;
    final private Messager messager;
    final private Sidebar sidebar;
    final private Collection<TaterzenNPC> shopKeepers;
    final private WinEventSender winEventSender;


    BedwarsActive(GameSpace gameSpace, BedwarsMap gameMap, ServerWorld world, Multimap<GameTeam, ServerPlayerEntity> teamPlayers, List<GameTeam> teamsInOrder, BedwarsConfig config)
    {
        this.gameSpace = gameSpace;
        this.gameMap = gameMap;
        this.config = config;
        this.world = world;
        this.teamPlayersMap = teamPlayers;
        this.teamsInOrder = teamsInOrder;
        gameSpace.setActivity(gameActivity -> this.activity = gameActivity);
        setupGameRules();
        this.claim = new ClaimManager(gameMap, config, activity);
        this.teamManager = TeamManager.addTo(activity);
        setupTeam(teamPlayers);
        this.teamComponentsMap = makeTeamComponents(); //forge bed, spawn ect
        this.deathManager = new DeathManager(teamComponentsMap, teamManager, world, gameMap, activity);
        this.attackSpeedRestorer = new OldAttackSpeed(activity);
        this.defaultSwordManager = new DefaultSword(activity);
        this.inventoryManager = new InventoryManager(deathManager, teamPlayersMap, activity);
        this.sidebar = BedwarsSideBar.build(teamComponentsMap, teamManager, teamsInOrder, activity);
        this.messager = new Messager(teamManager, activity);
        this.shopKeepers = addShopkeepers(gameMap.ShopKeepers());
        this.winEventSender = new WinEventSender(teamsInOrder, teamManager, activity);
        activity.listen(BedwarsEvents.TEAM_WIN, this::onTeamWin);
        destroySpawn();
        startGame();
        breakBedForEmptyTeam();
    }
    private void setupGameRules()
    {
        //set gameRules
        activity.deny(GameRuleType.CRAFTING);
        activity.deny(GameRuleType.PORTALS);
        activity.allow(GameRuleType.PVP);
        activity.deny(GameRuleType.HUNGER);
        activity.allow(GameRuleType.FALL_DAMAGE);
        activity.allow(GameRuleType.BLOCK_DROPS);
        activity.allow(GameRuleType.THROW_ITEMS);
        activity.allow(GameRuleType.PLAYER_PROJECTILE_KNOCKBACK);
        activity.allow(GameRuleType.TRIDENTS_LOYAL_IN_VOID);
        activity.deny(GameRuleType.MODIFY_ARMOR);
        activity.deny(GameRuleType.SATURATED_REGENERATION);
        activity.deny(NotASword.ATTACK_SOUND);
        activity.allow(NotASword.OLD_KNOCKBACK);
        activity.deny(Bedwars.BED_INTERACTION);
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

    private Collection<TaterzenNPC> addShopkeepers(List<BlockBounds> shopkeepersBounds)
    {
        var entries = ShopConfigs.ENTRIES_REGISTRY.get(config.shopEntriesId());
        var categories = ShopConfigs.CATEGORIES_REGISTRY.get(config.shopCategoriesId());
        var menu = new ItemShopMenu(this, entries, categories, activity);
        var shopkeepers = new ArrayList<TaterzenNPC>();
        for(var shopkeeper : shopkeepersBounds)
        {
            shopkeepers.add(ShopKeeper.createShopKeeper(world, shopkeeper, claim, menu));
        }
        return shopkeepers;
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
        //todo : add messages into Messager Class
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

    public DefaultSword getDefaultSwordManager() { return defaultSwordManager; }

    public InventoryManager getInventoryManager()
    {
        return inventoryManager;
    }
}
