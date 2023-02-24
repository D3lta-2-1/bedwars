package me.verya.bedwars.game.shop;
import me.verya.bedwars.game.behavior.ClaimManager;
import me.verya.bedwars.game.shop.ShopMenu.ShopMenu;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.stimuli.event.entity.EntityUseEvent;

public class ShopKeeper {
    private final Entity entity;
    private final ShopMenu menu;

    public ShopKeeper(ServerWorld world, BlockBounds pos, ClaimManager claimManager, ShopMenu menu, GameActivity activity)
    {
        claimManager.addRegion(pos);
        VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, world);
        villager.refreshPositionAndAngles(pos.centerBottom().getX(), pos.centerBottom().getY(), pos.centerBottom().getZ(), 0, 0);
        villager.setAiDisabled(true);
        villager.setInvulnerable(true);
        villager.setNoGravity(true);
        villager.setSilent(true);
        this.menu = menu;
        this.entity = villager;
        world.getChunk(villager.getBlockPos());
        world.spawnEntity(villager);
        activity.listen(EntityUseEvent.EVENT, this::onEntityUseEvent);
    }

    ActionResult onEntityUseEvent(ServerPlayerEntity player, Entity entity, Hand hand, EntityHitResult hitResult)
    {
        if(entity != this.entity) return ActionResult.PASS;
        menu.open(player);
        return ActionResult.FAIL;
    }
}
