package io.github.trashoflevillage.progressive_raids.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.trashoflevillage.progressive_raids.StateSaverAndLoader;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Raid.class)
public abstract class RaidMixin {
    @Shadow public abstract int getBadOmenLevel();

    @Shadow public abstract boolean hasWon();

    @Shadow public abstract World getWorld();

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (!getWorld().isClient() && hasWon()) {
            int savedOmenLevel = getOmenLevel();
            if (getBadOmenLevel() >= savedOmenLevel && savedOmenLevel < 255) {
                setOmenLevel(getBadOmenLevel());
            }
        }
    }

    @ModifyReturnValue(method = "getMaxAcceptableBadOmenLevel", at = @At("RETURN"))
    public int getMaxAcceptableBadOmenLevel(int original) {
        return 255;
    }

    public int getOmenLevel() {
        MinecraftServer server = getWorld().getServer();
        if (server != null) {
            StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(server);
            return serverState.omenLevel;
        }
        return 0;
    }

    public void setOmenLevel(int lvl) {
        if (lvl > 255) lvl = 255;
        MinecraftServer server = getWorld().getServer();
        if (server != null) {
            StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(server);
            serverState.omenLevel = lvl;

            PacketByteBuf data = PacketByteBufs.create();
            data.writeInt(serverState.omenLevel);

//            getServer().execute(() -> {
//                ServerPlayNetworking.send(this, DIRT_BROKEN, data);
//            });
        }
    }
}