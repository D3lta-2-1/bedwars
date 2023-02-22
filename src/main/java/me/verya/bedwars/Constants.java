package me.verya.bedwars;

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
            DyeColor.YELLOW,
            DyeColor.GREEN,
            DyeColor.LIME,
            DyeColor.ORANGE,
            DyeColor.LIGHT_BLUE,
            DyeColor.PURPLE,
            DyeColor.CYAN,
            DyeColor.MAGENTA,
            DyeColor.BROWN,
            DyeColor.WHITE,
            DyeColor.PINK,
            DyeColor.GRAY,
            DyeColor.BLACK,
            DyeColor.LIGHT_GRAY
    ));
    //usage ex: "blue_spawn" and "blue_bed"
    static public final String SPAWN = "spawn";
    static public final String BED = "bed";
    static public final String FORGE = "forge";
    static public final String SHOPKEEPER = "shopkeeper";

    //TODO : must be completed with other flowers and fluids
    static public final List<Block> BreakableBlocks = new ArrayList<>(Arrays.asList(
            Blocks.TALL_GRASS,
            Blocks.WATER,
            Blocks.POPPY)
    );
}
