package fr.delta.bedwars.game.shop.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.delta.bedwars.Constants;
import fr.delta.bedwars.game.BedwarsActive;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import java.util.Map;

public class ColoredBlockEntry extends ShopEntry  {

    public static Codec<ColoredBlockEntry> WOOL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Cost.CODEC.fieldOf("cost").forGetter(ColoredBlockEntry::getCostNoArgument),
            Codec.INT.fieldOf("count").forGetter(ColoredBlockEntry::getCount)
    ).apply(instance, (cost, count) -> new ColoredBlockEntry(cost, count, Constants.DYE_WOOL_MAP, Items.WHITE_WOOL, "shop.bedwars.wool")));

    public static Codec<ColoredBlockEntry> TERRACOTTA_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Cost.CODEC.fieldOf("cost").forGetter(ColoredBlockEntry::getCostNoArgument),
            Codec.INT.fieldOf("count").forGetter(ColoredBlockEntry::getCount)
    ).apply(instance, (cost, count) -> new ColoredBlockEntry(cost, count, Constants.DYE_TERRACOTTA_MAP, Items.TERRACOTTA, "shop.bedwars.terracotta")));

    public static Codec<ColoredBlockEntry> GLASS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Cost.CODEC.fieldOf("cost").forGetter(ColoredBlockEntry::getCostNoArgument),
            Codec.INT.fieldOf("count").forGetter(ColoredBlockEntry::getCount)
    ).apply(instance, (cost, count) -> new ColoredBlockEntry(cost, count, Constants.DYE_GLASS_MAP, Items.GLASS,"shop.bedwars.glass")));

    final private Cost cost;
    final private int count;
    final private Map<DyeColor, Item> dyeColorItemStackMap;
    final private String translationKey;
    final private Item display;

    public ColoredBlockEntry(Cost cost, int count, Map<DyeColor, Item> dyeColorItemStackMap, Item display, String translationKey) {
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
    public Item getDisplay(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        return display;
    }

    @Override
    public ItemStack onBuy(BedwarsActive bedwarsGame, ServerPlayerEntity player)
    {
        var team = bedwarsGame.getTeamForPlayer(player);
        if(team == null)
            return Items.WHITE_WOOL.getDefaultStack();
        return dyeColorItemStackMap.get(team.config().blockDyeColor()).getDefaultStack();
    }

    @Override
    public int getCount() {
        return count;
    }
}
