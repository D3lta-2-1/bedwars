package me.verya.bedwars.customItem;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;


public class OldSword extends SwordItem implements PolymerItem {
    public static SwordItem WOODEN_SWORD = new OldSword(ToolMaterials.WOOD, 3, new Settings(), Items.WOODEN_SWORD);
    public static SwordItem GOLDEN_SWORD = new OldSword(ToolMaterials.GOLD, 3, new Settings(), Items.GOLDEN_SWORD);
    public static SwordItem STONE_SWORD = new OldSword(ToolMaterials.STONE, 3, new Settings(), Items.STONE_SWORD);
    public static SwordItem IRON_SWORD = new OldSword(ToolMaterials.IRON, 3, new Settings(), Items.IRON_SWORD);
    public static SwordItem DIAMOND_SWORD = new OldSword(ToolMaterials.DIAMOND, 3, new Settings(), Items.DIAMOND_SWORD);
    public static SwordItem NETHERITE_SWORD = new OldSword(ToolMaterials.NETHERITE, 3, new Settings(), Items.NETHERITE_SWORD);
    final private Item clientSideItem;
    public OldSword(ToolMaterial toolMaterial, int attackDamage, Item.Settings settings, Item clientSideItem)
    {
        super(toolMaterial, attackDamage, -1, settings);
        this.clientSideItem = clientSideItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return clientSideItem;
    }

}
