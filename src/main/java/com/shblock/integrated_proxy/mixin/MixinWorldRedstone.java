package com.shblock.integrated_proxy.mixin;

import com.shblock.integrated_proxy.storage.AccessProxyCollection;
import com.shblock.integrated_proxy.tileentity.TileAccessProxy;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.HashSet;

@Mixin(World.class)
public abstract class MixinWorldRedstone {
    @Shadow @Nullable public abstract TileEntity getTileEntity(BlockPos pos);

    @Shadow public abstract MapStorage getPerWorldStorage();

    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/world/World;getRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I", cancellable = true)
    public void getRedstonePower(BlockPos pos, EnumFacing facing, CallbackInfoReturnable<Integer> callback) {
        MapStorage storage = getPerWorldStorage();
        AccessProxyCollection data = (AccessProxyCollection) storage.getOrLoadData(AccessProxyCollection.class, AccessProxyCollection.NAME);
        if (data == null) {
            data = new AccessProxyCollection();
            storage.setData(AccessProxyCollection.NAME, data);
        }
        HashSet<BlockPos> proxies = data.getProxiesFromTarget(pos.offset(facing));
        if (!proxies.isEmpty()) {
            int max_power = callback.getReturnValue();
            for (BlockPos proxy : proxies) {
                TileAccessProxy tile = (TileAccessProxy) this.getTileEntity(proxy);
                if (tile != null) {
                    max_power = Math.max(max_power, tile.getRedstonePowerForTarget());
                }
            }
            callback.setReturnValue(max_power);
        }
    }
}
