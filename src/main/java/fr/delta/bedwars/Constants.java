package fr.delta.bedwars;

import net.minecraft.block.Block;

import net.minecraft.block.Blocks;
import net.minecraft.util.DyeColor;

import java.util.*;

//as regions names specified here
public class Constants {
    static public final String WAITING_SPAWN = "waiting_spawn";

    //this order is the sidebar order
    static public final List<DyeColor> TEAM_COLORS = new ArrayList<>(Arrays.asList(
            DyeColor.RED,
            DyeColor.BLUE,
            DyeColor.LIME,
            DyeColor.YELLOW,
            DyeColor.CYAN,
            DyeColor.WHITE,
            DyeColor.PINK,
            DyeColor.GRAY,

            DyeColor.ORANGE,
            DyeColor.BLACK,
            DyeColor.PURPLE,
            DyeColor.GREEN,

            DyeColor.BROWN,
            DyeColor.LIGHT_BLUE,
            DyeColor.LIGHT_GRAY,
            DyeColor.MAGENTA

    ));
    //usage ex: "blue_spawn" and "blue_bed"
    static public final String SPAWN = "spawn";
    static public final String BED = "bed";
    static public final String FORGE = "forge";
    static public final String ITEM_SHOPKEEPER = "item_shopkeeper";
    static public final String TEAM_SHOPKEEPER = "team_shopkeeper";

    //TODO : must be completed with other flowers and fluids
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
            Blocks.CACTUS)
    );
}
