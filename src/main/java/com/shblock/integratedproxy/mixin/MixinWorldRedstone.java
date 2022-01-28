package com.shblock.integratedproxy.mixin;

import com.shblock.integratedproxy.IntegratedProxy;
import com.shblock.integratedproxy.storage.AccessProxyCollection;
import com.shblock.integratedproxy.tileentity.TileAccessProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.HashSet;

@Mixin(Level.class)
public abstract class MixinWorldRedstone {

  @Shadow
  @Nullable
  public abstract MinecraftServer getServer();

  @Shadow
  public abstract ResourceKey<Level> dimension();

  @Shadow
  @Nullable
  public abstract BlockEntity getBlockEntity(BlockPos pos);

  @Inject(at = @At("RETURN"), method = "getSignal", cancellable = true)
  public void getRedstonePower(BlockPos pos, Direction facing, CallbackInfoReturnable<Integer> callback) {
    MinecraftServer server = getServer();
    if (server == null) return;
    ServerLevel world = server.getLevel(dimension());
    if (world == null) return;
    AccessProxyCollection data = AccessProxyCollection.getInstance(world);
    HashSet<BlockPos> proxies = data.getProxiesFromTarget(pos.offset(facing.getOpposite().getNormal()));
    if (!proxies.isEmpty()) {
      int max_power = callback.getReturnValue();
      for (BlockPos proxy : proxies) {
        BlockEntity tile = getBlockEntity(proxy);
        if (tile instanceof TileAccessProxy) {
          max_power = Math.max(max_power, ((TileAccessProxy) tile).getRedstonePowerForTarget());
        } else {
          data.remove(proxy);
          IntegratedProxy.clog(org.apache.logging.log4j.Level.WARN, "Found a tile that's not AccessProxy in AccessProxyCollection, removing: " + proxy.toString());
        }
      }
      callback.setReturnValue(max_power);
    }
  }

  @Inject(at = @At("RETURN"), method = "getDirectSignalTo", cancellable = true)
  public void getStrongPower(BlockPos pos, CallbackInfoReturnable<Integer> callback) {
    MinecraftServer server = getServer();
    if (server == null) return;
    ServerLevel world = server.getLevel(dimension());
    if (world == null) return;
    AccessProxyCollection data = AccessProxyCollection.getInstance(world);
    HashSet<BlockPos> proxies = data.getProxiesFromTarget(pos);
    if (!proxies.isEmpty()) {
      int max_power = callback.getReturnValue();
      for (BlockPos proxy : proxies) {
        BlockEntity tile = getBlockEntity(proxy);
        if (tile instanceof TileAccessProxy) {
          max_power = Math.max(max_power, ((TileAccessProxy) tile).getStrongPowerForTarget());
        } else {
          data.remove(proxy);
          IntegratedProxy.clog(org.apache.logging.log4j.Level.WARN, "Found a tile that's not AccessProxy in AccessProxyCollection, removing: " + proxy.toString());
        }
      }
      callback.setReturnValue(max_power);
    }
  }
}
