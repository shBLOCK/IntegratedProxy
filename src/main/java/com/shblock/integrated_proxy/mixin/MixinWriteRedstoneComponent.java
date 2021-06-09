package com.shblock.integrated_proxy.mixin;

import com.shblock.integrated_proxy.tileentity.TileAccessProxy;
import net.minecraft.tileentity.TileEntity;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.part.aspect.write.redstone.WriteRedstoneComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WriteRedstoneComponent.class)
public class MixinWriteRedstoneComponent {
    @Inject(at = @At("HEAD"), method = "Lorg/cyclops/integrateddynamics/part/aspect/write/redstone/WriteRedstoneComponent;setRedstoneLevel(Lorg/cyclops/integrateddynamics/api/part/PartTarget;IZ)V", cancellable = true, remap = false)
    private void setRedstoneLevel(PartTarget target, int level, boolean strongPower, CallbackInfo ci) {
//        TileEntity te = target.getTarget().getPos().getWorld().getTileEntity(target.getTarget().getPos().getBlockPos());
//        if (te instanceof TileAccessProxy) {
//            ((TileAccessProxy) te).setSideRedstonePower(target.getTarget().getSide(), level);
//        }
    }

    @Inject(at = @At("HEAD"), method = "Lorg/cyclops/integrateddynamics/part/aspect/write/redstone/WriteRedstoneComponent;deactivate(Lorg/cyclops/integrateddynamics/api/part/PartTarget;)V", remap = false)
    private void deactivate(PartTarget target, CallbackInfo ci) {
//        TileEntity te = target.getTarget().getPos().getWorld().getTileEntity(target.getTarget().getPos().getBlockPos());
//        if (te instanceof TileAccessProxy) {
//            ((TileAccessProxy) te).setSideRedstonePower(target.getTarget().getSide(), 0);
//        }
    }
}
