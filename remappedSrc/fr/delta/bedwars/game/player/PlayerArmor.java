package fr.delta.bedwars.game.player;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerArmor {
    final private ServerPlayerEntity player;
    final private GameTeam team;

    public PlayerArmor(ServerPlayerEntity player, GameTeam team)
    {
        this.player = player;
        this.team = team;
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
            //Todo : fixe the sound played when the armor is equiped
           player.getInventory().armor.set(piece.getSlotType().getEntitySlotId(), builder.build());
        }
    }
    private List<ArmorItem> getArmor()
    {
         return new ArrayList<>(Arrays.asList(
                 (ArmorItem) Items.LEATHER_HELMET,
                 (ArmorItem) Items.LEATHER_CHESTPLATE,
                 (ArmorItem) Items.LEATHER_LEGGINGS,
                 (ArmorItem) Items.LEATHER_BOOTS
         ));
    }

}
