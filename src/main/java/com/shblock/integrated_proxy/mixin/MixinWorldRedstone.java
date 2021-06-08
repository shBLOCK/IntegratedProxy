package com.shblock.integrated_proxy.mixin;

import com.shblock.integrated_proxy.tileentity.TileAccessProxy;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class MixinWorldRedstone {
    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/World;getRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)I", cancellable = true)
    private void getTarget(BlockPos pos, EnumFacing facing, CallbackInfoReturnable<Integer> callback) {
        callback.setReturnValue(10);
    }
}
