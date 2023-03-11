package fr.delta.bedwars.game.player;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import java.util.Arrays;
import java.util.List;

public class PlayerArmorManager {
    final private ServerPlayerEntity player;
    final private GameTeam team;

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
    }
    private ArmorLevel armorLevel;

    public PlayerArmorManager(ServerPlayerEntity player, GameTeam team)
    {
        this.player = player;
        this.team = team;
        this.armorLevel = ArmorLevel.LEATHER;
    }

    public void setLevel(ArmorLevel level)
    {
        this.armorLevel = level;
        updateArmor();
    }

    public ArmorLevel getLevel()
    {
        return armorLevel;
    }

    public void updateArmor()
    {
        var armor = getArmor();
        var config = team.config();
        for(var piece : armor)
        {
            var builder = ItemStackBuilder.of(piece).setUnbreakable().setDyeColor(config.dyeColor().getRgb());
            if(piece.getSlotType() == EquipmentSlot.HEAD)
            {
                builder.addEnchantment(Enchantments.AQUA_AFFINITY, 1);
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
