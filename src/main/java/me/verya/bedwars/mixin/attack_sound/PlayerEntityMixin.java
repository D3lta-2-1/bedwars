package me.verya.bedwars.mixin.attack_sound;


import me.verya.bedwars.Bedwars;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    public void disableAttackSounds(World world, PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch)
    {
        var gameSpace = GameSpaceManager.get().byWorld(world);
        if(gameSpace != null && gameSpace.getBehavior().testRule(Bedwars.ATTACK_SOUND) == ActionResult.FAIL)
        {
            if (sound != SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK &&
                    sound != SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE &&
                    sound != SoundEvents.ENTITY_PLAYER_ATTACK_STRONG && sound != SoundEvents.ENTITY_PLAYER_ATTACK_WEAK &&
                    sound != SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)
            {
                world.playSound(player, x, y, z, sound, category, volume, pitch);
            }
        }
        else
        {
            world.playSound(player, x, y, z, sound, category, volume, pitch);
        }
    }
}
