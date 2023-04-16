package fr.delta.bedwars.game.player;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DyeColor;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlayerArmorManager {
    final private DyeColor armorColor;
    final private Map<Enchantment, Integer> enchantments;

    public enum ArmorLevel
    {
        LEATHER,
        CHAIN,
        IRON,
        DIAMOND;

        public boolean smallerThan(ArmorLevel level)
        {
            return this.ordinal() < level.ordinal();
        }

        static public ArmorLevel createFromString(String level)
        {
            for(var armorLevel : ArmorLevel.values())
            {
                if(armorLevel.name().toLowerCase().equals(level))
                    return armorLevel;
            }
            return ArmorLevel.LEATHER;
        }
    }
    private ArmorLevel armorLevel;

    public PlayerArmorManager(DyeColor armorColor, Map<Enchantment, Integer> enchantments)
    {
        this.armorColor = armorColor;
        this.enchantments = enchantments;
        this.armorLevel = ArmorLevel.LEATHER;
    }

    public void setLevel(ServerPlayerEntity player, ArmorLevel level)
    {
        this.armorLevel = level;
        updateArmor(player);
    }

    public ArmorLevel getLevel()
    {
        return armorLevel;
    }

    public void updateArmor(ServerPlayerEntity player)
    {
        var armor = getArmor();
        for(var piece : armor)
        {
            var builder = ItemStackBuilder.of(piece).setUnbreakable().setDyeColor(armorColor.getSignColor());
            if(piece.getSlotType() == EquipmentSlot.HEAD)
            {
                builder.addEnchantment(Enchantments.AQUA_AFFINITY, 1);
            }
            for(var enchantment : this.enchantments.entrySet())
            {
                if(enchantment.getKey().isAcceptableItem(builder.build()))
                    builder.addEnchantment(enchantment.getKey(), enchantment.getValue());
            }
            //Todo : fixe the sound played when the armor is equipped
            player.getInventory().armor.set(piece.getSlotType().getEntitySlotId(), builder.build());
        }
    }
    private List<ArmorItem> getArmor()
    {
        return switch (armorLevel) {
            case DIAMOND -> Arrays.asList(
                    (ArmorItem) Items.LEATHER_CHESTPLATE,
                    (ArmorItem) Items.LEATHER_HELMET,
                    (ArmorItem) Items.DIAMOND_LEGGINGS,
                    (ArmorItem) Items.DIAMOND_BOOTS);
            case IRON -> Arrays.asList(
                    (ArmorItem) Items.LEATHER_CHESTPLATE,
                    (ArmorItem) Items.LEATHER_HELMET,
                    (ArmorItem) Items.IRON_LEGGINGS,
                    (ArmorItem) Items.IRON_BOOTS);
            case CHAIN -> Arrays.asList(
                    (ArmorItem) Items.LEATHER_CHESTPLATE,
                    (ArmorItem) Items.LEATHER_HELMET,
                    (ArmorItem) Items.CHAINMAIL_LEGGINGS,
                    (ArmorItem) Items.CHAINMAIL_BOOTS);
            default -> Arrays.asList(
                    (ArmorItem) Items.LEATHER_CHESTPLATE,
                    (ArmorItem) Items.LEATHER_HELMET,
                    (ArmorItem) Items.LEATHER_LEGGINGS,
                    (ArmorItem) Items.LEATHER_BOOTS);
        };

    }

}
