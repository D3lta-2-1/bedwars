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
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import java.util.HashMap;
import java.util.Map;

public class ColoredBlockEntry extends ShopEntry  {

    public static Codec<ColoredBlockEntry> WOOL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Cost.CODEC.fieldOf("cost").forGetter(ColoredBlockEntry::getCostNoArgument),
            Codec.INT.fieldOf("count").forGetter(ColoredBlockEntry::getCount)
    ).apply(instance, (cost, count) -> new ColoredBlockEntry(cost, count, woolMap(), Items.WHITE_WOOL, "shop.bedwars.wool")));

    public static Codec<ColoredBlockEntry> TERRACOTTA_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Cost.CODEC.fieldOf("cost").forGetter(ColoredBlockEntry::getCostNoArgument),
            Codec.INT.fieldOf("count").forGetter(ColoredBlockEntry::getCount)
    ).apply(instance, (cost, count) -> new ColoredBlockEntry(cost, count, terracottaMap(), Items.TERRACOTTA, "shop.bedwars.terracotta")));

    public static Codec<ColoredBlockEntry> GLASS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Cost.CODEC.fieldOf("cost").forGetter(ColoredBlockEntry::getCostNoArgument),
            Codec.INT.fieldOf("count").forGetter(ColoredBlockEntry::getCount)
    ).apply(instance, (cost, count) -> new ColoredBlockEntry(cost, count, glassMap(), Items.GLASS,"shop.bedwars.glass")));

    static private Map<DyeColor, ItemStack> woolMap()
    {
        Map<DyeColor, ItemStack> map = new HashMap<>(16);
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

    static private Map<DyeColor, ItemStack> terracottaMap()
    {
        Map<DyeColor, ItemStack> map = new HashMap<>(16);
        map.put(DyeColor.BLACK, Items.BLACK_TERRACOTTA.getDefaultStack());
        map.put(DyeColor.BLUE,  Items.BLUE_TERRACOTTA.getDefaultStack());
        map.put(DyeColor.RED, Items.RED_TERRACOTTA.getDefaultStack());
        map.put(DyeColor.LIME,  Items.LIME_TERRACOTTA.getDefaultStack());
        map.put(DyeColor.GREEN, Items.GREEN_TERRACOTTA.getDefaultStack());
        map.put(DyeColor.GRAY,  Items.GRAY_TERRACOTTA.getDefaultStack());
        map.put(DyeColor.YELLOW, Items.YELLOW_TERRACOTTA.getDefaultStack());
        map.put(DyeColor.ORANGE,  Items.ORANGE_TERRACOTTA.getDefaultStack());
        map.put(DyeColor.PINK, Items.PINK_TERRACOTTA.getDefaultStack());
        map.put(DyeColor.PURPLE,  Items.PURPLE_TERRACOTTA.getDefaultStack());
        map.put(DyeColor.CYAN, Items.CYAN_TERRACOTTA.getDefaultStack());
        map.put(DyeColor.LIGHT_BLUE,  Items.LIGHT_BLUE_TERRACOTTA.getDefaultStack());
        map.put(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_TERRACOTTA.getDefaultStack());
        map.put(DyeColor.BROWN,  Items.BROWN_TERRACOTTA.getDefaultStack());
        map.put(DyeColor.MAGENTA, Items.MAGENTA_TERRACOTTA.getDefaultStack());
        map.put(DyeColor.WHITE,  Items.WHITE_TERRACOTTA.getDefaultStack());

        return map;
    }

    static private Map<DyeColor, ItemStack> glassMap()
    {
        Map<DyeColor, ItemStack> map = new HashMap<>(16);
        map.put(DyeColor.BLACK, Items.BLACK_STAINED_GLASS.getDefaultStack());
        map.put(DyeColor.BLUE,  Items.BLUE_STAINED_GLASS.getDefaultStack());
        map.put(DyeColor.RED, Items.RED_STAINED_GLASS.getDefaultStack());
        map.put(DyeColor.LIME,  Items.LIME_STAINED_GLASS.getDefaultStack());
        map.put(DyeColor.GREEN, Items.GREEN_STAINED_GLASS.getDefaultStack());
        map.put(DyeColor.GRAY,  Items.GRAY_STAINED_GLASS.getDefaultStack());
        map.put(DyeColor.YELLOW, Items.YELLOW_STAINED_GLASS.getDefaultStack());
        map.put(DyeColor.ORANGE,  Items.ORANGE_STAINED_GLASS.getDefaultStack());
        map.put(DyeColor.PINK, Items.PINK_STAINED_GLASS.getDefaultStack());
        map.put(DyeColor.PURPLE,  Items.PURPLE_STAINED_GLASS.getDefaultStack());
        map.put(DyeColor.CYAN, Items.CYAN_STAINED_GLASS.getDefaultStack());
        map.put(DyeColor.LIGHT_BLUE,  Items.LIGHT_BLUE_STAINED_GLASS.getDefaultStack());
        map.put(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_STAINED_GLASS.getDefaultStack());
        map.put(DyeColor.BROWN,  Items.BROWN_STAINED_GLASS.getDefaultStack());
        map.put(DyeColor.MAGENTA, Items.MAGENTA_STAINED_GLASS.getDefaultStack());
        map.put(DyeColor.WHITE,  Items.WHITE_STAINED_GLASS.getDefaultStack());

        return map;
    }

    final private Cost cost;
    final private int count;
    final private Map<DyeColor, ItemStack> dyeColorItemStackMap;
    final private String translationKey;
    final private Item display;

    public ColoredBlockEntry(Cost cost, int count, Map<DyeColor, ItemStack> dyeColorItemStackMap, Item display, String translationKey) {
        this.cost = cost;
        this.count = count;
        this.dyeColorItemStackMap = dyeColorItemStackMap;
        this.translationKey = translationKey;
        this.display = display;
    }


    public Cost getCostNoArgument() {
        return cost;
    }

    @Override
    public Cost getCost(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return cost;
    }
    @Override
    public MutableText getName(BedwarsActive BedwarsGame, ServerPlayerEntity player)
    {
        return Text.translatable(translationKey);
    }

    @Override
    public Item getDisplay(BedwarsActive BedwarsGame, ServerPlayerEntity player)
    {
        return display;
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        var team = bedwarsGame.getTeamForPlayer(player);
        if(team == null)
            return Items.WHITE_WOOL.getDefaultStack();
        return dyeColorItemStackMap.get(team.config().blockDyeColor()).copy();
    }

    @Override
    public int getCount() {
        return count;
    }
}
