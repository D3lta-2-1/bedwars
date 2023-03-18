package fr.delta.bedwars.custom.items;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FireBall extends Item implements PolymerItem {
    public FireBall(Item.Settings settings)
    {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!world.isClient) {
            var yaw = user.getYaw();
            var pitch = user.getPitch();
            var playerVel = user.getVelocity();
            float velX = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F) + (float)playerVel.x;
            float velY = -MathHelper.sin(pitch* 0.017453292F);
            float velZ = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F) + (float)playerVel.z;
            FireballEntity fireball = new FireballEntity(world, user, velX, velY, velZ, 2);
            fireball.setPos(user.getX(), user.getEyeY() - 0.2, user.getZ());
            // fireball.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 0);
            fireball.setItem(itemStack);
            fireball.setOnFire(false);
            world.spawnEntity(fireball);
        }
        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.FIREWORK_STAR;
    }
}
