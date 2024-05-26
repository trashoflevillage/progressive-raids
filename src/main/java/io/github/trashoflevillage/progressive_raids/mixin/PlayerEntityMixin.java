package io.github.trashoflevillage.progressive_raids.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Arm;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    private static HashMap<String, Integer> omenLevels = new HashMap<>();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
            int lvl = getStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE).getAmplifier();
            if (lvl >= getOmenLevel()) setOmenLevel(lvl + 1);
        }

        if (hasStatusEffect(StatusEffects.BAD_OMEN)) {
            StatusEffectInstance effect = getStatusEffect(StatusEffects.BAD_OMEN);
            if (effect.getAmplifier() != getOmenLevel()) {
                removeStatusEffect(effect.getEffectType());
                addStatusEffect(new StatusEffectInstance(effect.getEffectType(), effect.getDuration(), getOmenLevel()));
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo info) {
        nbt.putInt("omenLevel", getOmenLevel());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo info) {
        if (!nbt.contains("omenLevel")) nbt.putInt("omenLevel", 0);
        setOmenLevel(nbt.getInt("omenLevel"));
    }

    public int getOmenLevel() {
        String n = getUuidAsString();
        if (!omenLevels.containsKey(n)) setOmenLevel(0);
        return omenLevels.get(n);
    }

    public void setOmenLevel(int lvl) {
        if (lvl < 0) lvl = 0;
        omenLevels.put(getUuidAsString(), lvl);
    }
}