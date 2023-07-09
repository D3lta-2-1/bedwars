package fr.delta.bedwars.game.shop.npc;
import fr.delta.bedwars.Bedwars;
import fr.delta.bedwars.game.behaviour.ClaimManager;
import fr.delta.bedwars.game.shop.ShopMenu.ShopMenu;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.map_templates.BlockBounds;

public class ShopKeeper {

    public static void createShopKeeper(ServerWorld world, BlockBounds pos, ClaimManager claimManager, ShopMenu menu)
    {
        claimManager.addRegion(pos);
        var entity = new ShopKeeperEntity(Bedwars.SHOP_ENTITY, world, menu);
        entity.setNoGravity(true);
        entity.setPos(pos.centerBottom().getX(), pos.centerBottom().getY(), pos.centerBottom().getZ());
        entity.setPersistent();
        entity.setInvulnerable(true);
        world.getChunk(entity.getBlockPos());
        world.spawnEntity(entity);
        entity.refreshPositionAndAngles(pos.centerBottom().getX(), pos.centerBottom().getY(), pos.centerBottom().getZ(), 0, 0);
    }
}
