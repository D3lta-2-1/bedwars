package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

import java.util.HashMap;
import java.util.Map;

public class WoolEntry extends ShopEntry  {

    public static Codec<WoolEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Cost.CODEC.fieldOf("cost").forGetter(WoolEntry::getCostNoArgument),
            Codec.INT.fieldOf("count").forGetter(WoolEntry::getCount)
    ).apply(instance, WoolEntry::new));
    public WoolEntry(Cost cost, int count) {
        this.cost = cost;
        this.count = count;
    }

    public Cost getCostNoArgument() {
        return cost;
    }
    final private Cost cost;
    final private int count;
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
        return cost;

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
        var stack = dyeColorItemStackMap.get(team.config().blockDyeColor()).copy();
        if(stack.isEmpty()) System.out.println("caca");
        return stack;
    }

    @Override
    public int getCount() {
        return count;
    }
}
