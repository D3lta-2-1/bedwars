package fr.delta.bedwars.game.shop.entry.articles;

import fr.delta.bedwars.game.shop.entry.ShopEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wool implements ShopEntry {
    final TeamManager teamManager;
    static final private Map<DyeColor, ItemStack> DyeWoolMap = makeMap();
    final List<GameTeam> teamsInOrder;

    static Map<DyeColor, ItemStack> makeMap()
    {
        Map<DyeColor, ItemStack> map = new HashMap<>();
        map.put(DyeColor.BLACK, Items.BLACK_WOOL.getDefaultStack());
        map.put(DyeColor.BLUE,  Items.BLUE_WOOL.getDefaultStack());
        map.put(DyeColor.RED, Items.RED_WOOL.getDefaultStack());
        map.put(DyeColor.LIME,  Items.LIME_WOOL.getDefaultStack());
        map.put(DyeColor.GREEN, Items.GREEN_WOOL.getDefaultStack());
        map.put(DyeColor.GRAY,  Items.GRAY_WOOL.getDefaultStack());
        map.put(DyeColor.YELLOW, Items.YELLOW_WOOL.getDefaultStack());
        map.put(DyeColor.ORANGE,  Items.ORANGE_WOOL.getDefaultStack());
        map.put(DyeColor.PINK, Items.PINK_WOOL.getDefaultStack());
        map.put(DyeColor.PURPLE,  Items.PURPLE_WOOL.getDefaultStack());
        map.put(DyeColor.CYAN, Items.CYAN_WOOL.getDefaultStack());
        map.put(DyeColor.LIGHT_BLUE,  Items.LIGHT_BLUE_WOOL.getDefaultStack());
        map.put(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_WOOL.getDefaultStack());
        map.put(DyeColor.BROWN,  Items.BROWN_WOOL.getDefaultStack());
        map.put(DyeColor.MAGENTA, Items.MAGENTA_WOOL.getDefaultStack());
        map.put(DyeColor.WHITE,  Items.WHITE_WOOL.getDefaultStack());

        return map;
    }

    public Wool(TeamManager teamManager, List<GameTeam> teamsInOrder)
    {
        this.teamManager = teamManager;
        this.teamsInOrder = teamsInOrder;
    }

    public Cost getCost()
    {
        return new Cost(Items.IRON_INGOT, 4);
    }

    public MutableText getTitle()
    {
        return Text.translatable("shop.bedwars.wool");
    }

    public Item getItem()
    {
        return Items.WHITE_WOOL;
    }

    public ItemStack onBuy(ServerPlayerEntity player)
    {
        var teamKey = teamManager.teamFor(player);
        for(var team : teamsInOrder)
        {
            if(team.key() == teamKey)
            {
                return DyeWoolMap.get(team.config().blockDyeColor());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getCount() {
        return 16;
    }
}
