package io.github.trashoflevillage.progressive_raids.mixin;

import io.github.trashoflevillage.progressive_raids.StateSaverAndLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (!getWorld().isClient() && hasStatusEffect(StatusEffects.BAD_OMEN)) {
            StatusEffectInstance effect = getStatusEffect(StatusEffects.BAD_OMEN);
            StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(getWorld().getServer());
            if (effect.getAmplifier() != serverState.omenLevel) {
                removeStatusEffect(effect.getEffectType());
                addStatusEffect(new StatusEffectInstance(effect.getEffectType(), effect.getDuration(), serverState.omenLevel));
            }
        }
    }
}
