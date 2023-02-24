package me.verya.bedwars.mixin;

import com.google.common.collect.ImmutableMultimap;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SwordItem.class)
public abstract class SwordItemMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMultimap$Builder;", ordinal = 1, remap = false))
    public ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> cancelAttackSpeedModifier(ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder, Object key, Object value) {
        EntityAttributeModifier entityAttributeModifier = (EntityAttributeModifier)value;
        var attackSpeed = entityAttributeModifier.getValue();
        if(attackSpeed >= 0)
        {
            builder.put((EntityAttribute)key, entityAttributeModifier);
        }
        return builder;
    }
}
