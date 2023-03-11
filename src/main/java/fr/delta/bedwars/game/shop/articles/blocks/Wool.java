package fr.delta.bedwars.game.shop.articles.blocks;

import fr.delta.bedwars.game.BedwarsActive;
import fr.delta.bedwars.game.shop.articles.ShopEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

import java.util.HashMap;
import java.util.Map;

public class Wool implements ShopEntry {
    static public ShopEntry INSTANCE = new Wool();
    static final private Map<DyeColor, ItemStack> dyeColorItemStackMap = makeMap();
    static private Map<DyeColor, ItemStack> makeMap()
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

    @Override
    public Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return new Cost(Items.IRON_INGOT, 4);

    }
    @Override
    public MutableText getName()
    {
        return Text.translatable("shop.bedwars.wool");
    }

    @Override
    public Item getDisplay()
    {
        return Items.WHITE_WOOL;
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        var team = bedwarsGame.getTeamForPlayer(player);
        if(team == null)
            return Items.WHITE_WOOL.getDefaultStack();
        return dyeColorItemStackMap.get(team.config().blockDyeColor());
    }

    @Override
    public int getCount() {
        return 16;
    }
}
