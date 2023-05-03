package fr.delta.bedwars;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;

import java.util.*;

//as regions names specified here
public class Constants {
    static public final String WAITING_SPAWN = "waiting_spawn";

    //this order is the sidebar order
    static public final List<DyeColor> TEAM_COLORS = new ArrayList<>(Arrays.asList(
            //hypixel order
            DyeColor.RED,
            DyeColor.BLUE,
            DyeColor.LIME,
            DyeColor.YELLOW,
            DyeColor.CYAN,
            DyeColor.WHITE,
            DyeColor.PINK,
            DyeColor.GRAY,
            //extended colors
            DyeColor.ORANGE,
            DyeColor.BLACK,
            DyeColor.PURPLE,
            DyeColor.GREEN,
            //other colors
            DyeColor.BROWN,
            DyeColor.LIGHT_BLUE,
            DyeColor.LIGHT_GRAY,
            DyeColor.MAGENTA

    ));
    //usage ex: "blue_spawn" and "blue_bed"
    static public final String SPAWN = "spawn";
    static public final String BED = "bed";
    static public final String FORGE = "forge";
    static public final String EFFECT_POOL = "effect_pool";
    static public final String ITEM_SHOPKEEPER = "item_shopkeeper";
    static public final String TEAM_SHOPKEEPER = "team_shopkeeper";
    static public final List<Block> BreakableBlocks = new ArrayList<>(Arrays.asList(
            Blocks.TALL_GRASS,
            Blocks.WATER,
            Blocks.POPPY,
            Blocks.FIRE,
            Blocks.DANDELION,
            Blocks.OXEYE_DAISY,
            Blocks.CORNFLOWER,
            Blocks.LILY_OF_THE_VALLEY,
            Blocks.LILAC,
            Blocks.ROSE_BUSH,
            Blocks.SUNFLOWER,
            Blocks.LARGE_FERN,
            Blocks.TALL_SEAGRASS,
            Blocks.SEAGRASS,
            Blocks.BLUE_ORCHID,
            Blocks.ALLIUM,
            Blocks.AZURE_BLUET,
            Blocks.RED_TULIP,
            Blocks.ORANGE_TULIP,
            Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP,
            Blocks.PEONY,
            Blocks.LILY_PAD,
            Blocks.SUGAR_CANE,
            Blocks.CACTUS,
            Blocks.BAMBOO,
            Blocks.BAMBOO_SAPLING,
            Blocks.BROWN_MUSHROOM,
            Blocks.RED_MUSHROOM,
            Blocks.COBWEB,
            Blocks.GRASS,
            Blocks.FERN)
    );

    public static final Map<DyeColor, Item> DYE_WOOL_MAP = ImmutableMap.<DyeColor, Item>builder()
            .put(DyeColor.BLACK, Items.BLACK_WOOL)
            .put(DyeColor.BLUE, Items.BLUE_WOOL)
            .put(DyeColor.RED, Items.RED_WOOL)
            .put(DyeColor.LIME, Items.LIME_WOOL)
            .put(DyeColor.GREEN, Items.GREEN_WOOL)
            .put(DyeColor.GRAY, Items.GRAY_WOOL)
            .put(DyeColor.YELLOW, Items.YELLOW_WOOL)
            .put(DyeColor.ORANGE, Items.ORANGE_WOOL)
            .put(DyeColor.PINK, Items.PINK_WOOL)
            .put(DyeColor.PURPLE, Items.PURPLE_WOOL)
            .put(DyeColor.CYAN, Items.CYAN_WOOL)
            .put(DyeColor.LIGHT_BLUE, Items.LIGHT_BLUE_WOOL)
            .put(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_WOOL)
            .put(DyeColor.BROWN, Items.BROWN_WOOL)
            .put(DyeColor.MAGENTA, Items.MAGENTA_WOOL)
            .put(DyeColor.WHITE, Items.WHITE_WOOL)
            .build();

    public static final Map<DyeColor, Item> DYE_TERRACOTTA_MAP = ImmutableMap.<DyeColor, Item>builder()
            .put(DyeColor.BLACK, Items.BLACK_TERRACOTTA)
            .put(DyeColor.BLUE, Items.BLUE_TERRACOTTA)
            .put(DyeColor.RED, Items.RED_TERRACOTTA)
            .put(DyeColor.LIME, Items.LIME_TERRACOTTA)
            .put(DyeColor.GREEN, Items.GREEN_TERRACOTTA)
            .put(DyeColor.GRAY, Items.GRAY_TERRACOTTA)
            .put(DyeColor.YELLOW, Items.YELLOW_TERRACOTTA)
            .put(DyeColor.ORANGE, Items.ORANGE_TERRACOTTA)
            .put(DyeColor.PINK, Items.PINK_TERRACOTTA)
            .put(DyeColor.PURPLE, Items.PURPLE_TERRACOTTA)
            .put(DyeColor.CYAN, Items.CYAN_TERRACOTTA)
            .put(DyeColor.LIGHT_BLUE, Items.LIGHT_BLUE_TERRACOTTA)
            .put(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_TERRACOTTA)
            .put(DyeColor.BROWN, Items.BROWN_TERRACOTTA)
            .put(DyeColor.MAGENTA, Items.MAGENTA_TERRACOTTA)
            .put(DyeColor.WHITE, Items.WHITE_TERRACOTTA)
            .build();

    public static final Map<DyeColor, Item> DYE_GLASS_MAP = ImmutableMap.<DyeColor, Item>builder()
            .put(DyeColor.BLACK, Items.BLACK_STAINED_GLASS)
            .put(DyeColor.BLUE, Items.BLUE_STAINED_GLASS)
            .put(DyeColor.RED, Items.RED_STAINED_GLASS)
            .put(DyeColor.LIME, Items.LIME_STAINED_GLASS)
            .put(DyeColor.GREEN, Items.GREEN_STAINED_GLASS)
            .put(DyeColor.GRAY, Items.GRAY_STAINED_GLASS)
            .put(DyeColor.YELLOW, Items.YELLOW_STAINED_GLASS)
            .put(DyeColor.ORANGE, Items.ORANGE_STAINED_GLASS)
            .put(DyeColor.PINK, Items.PINK_STAINED_GLASS)
            .put(DyeColor.PURPLE, Items.PURPLE_STAINED_GLASS)
            .put(DyeColor.CYAN, Items.CYAN_STAINED_GLASS)
            .put(DyeColor.LIGHT_BLUE, Items.LIGHT_BLUE_STAINED_GLASS)
            .put(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_STAINED_GLASS)
            .put(DyeColor.BROWN, Items.BROWN_STAINED_GLASS)
            .put(DyeColor.MAGENTA, Items.MAGENTA_STAINED_GLASS)
            .put(DyeColor.WHITE, Items.WHITE_STAINED_GLASS)
            .build();

}
