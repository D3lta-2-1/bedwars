package fr.delta.bedwars.game.shop.npc;

import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.game.shop.ShopMenu.ShopMenu;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.samo_lego.taterzens.api.professions.TaterzenProfession;

public class ShopProfession implements TaterzenProfession {
    static public final Identifier ID = new Identifier(Bedwars.ID, "bedwars_shop");
    private final ShopMenu menu;
    public ShopProfession(ShopMenu shopMenu)
    {
        this.menu = shopMenu;
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d pos, Hand hand) {
        ServerPlayerEntity playerEntity = player instanceof ServerPlayerEntity ? (ServerPlayerEntity)player : null;
        if(playerEntity != null) menu.open(playerEntity);
        return ActionResult.PASS;
    }
}
