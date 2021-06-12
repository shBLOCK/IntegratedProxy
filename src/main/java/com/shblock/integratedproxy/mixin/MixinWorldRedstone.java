package com.shblock.integratedproxy.mixin;

import com.shblock.integratedproxy.storage.AccessProxyCollection;
import com.shblock.integratedproxy.tileentity.TileAccessProxy;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.HashSet;

@Mixin(World.class)
public abstract class MixinWorldRedstone {

    @Shadow @Nullable public abstract MinecraftServer getServer();

    @Shadow public abstract RegistryKey<World> getDimensionKey();

    @Shadow @Nullable public abstract TileEntity getTileEntity(BlockPos pos);

    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/world/World;getRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;)I", cancellable = true)
    public void getRedstonePower(BlockPos pos, Direction facing, CallbackInfoReturnable<Integer> callback) {
        MinecraftServer server = getServer();
        if (server == null) return;
        ServerWorld world = server.getWorld(getDimensionKey());
        if (world == null) return;
        AccessProxyCollection data = AccessProxyCollection.getInstance(world);
        HashSet<BlockPos> proxies = data.getProxiesFromTarget(pos.offset(facing.getOpposite()));
        if (!proxies.isEmpty()) {
            int max_power = callback.getReturnValue();
            for (BlockPos proxy : proxies) {
                TileAccessProxy tile = (TileAccessProxy) getTileEntity(proxy);
                if (tile != null) {
                    max_power = Math.max(max_power, tile.getRedstonePowerForTarget());
                }
            }
            callback.setReturnValue(max_power);
        }
    }

    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/world/World;getStrongPower(Lnet/minecraft/util/math/BlockPos;)I", cancellable = true)
    public void getStrongPower(BlockPos pos, CallbackInfoReturnable<Integer> callback) {
        MinecraftServer server = getServer();
        if (server == null) return;
        ServerWorld world = server.getWorld(getDimensionKey());
        if (world == null) return;
        AccessProxyCollection data = AccessProxyCollection.getInstance(world);
        HashSet<BlockPos> proxies = data.getProxiesFromTarget(pos);
        if (!proxies.isEmpty()) {
            int max_power = callback.getReturnValue();
            for (BlockPos proxy : proxies) {
                TileAccessProxy tile = (TileAccessProxy) getTileEntity(proxy);
                if (tile != null) {
                    max_power = Math.max(max_power, tile.getStrongPowerForTarget());
                }
            }
            callback.setReturnValue(max_power);
        }
    }
}
