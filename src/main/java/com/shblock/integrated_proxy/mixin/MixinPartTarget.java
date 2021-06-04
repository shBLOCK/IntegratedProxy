package com.shblock.integrated_proxy.mixin;

import com.shblock.integrated_proxy.tileentity.TileAccessProxy;
import net.minecraft.tileentity.TileEntity;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PartTarget.class)
public class MixinPartTarget {
    @Inject(at = @At("HEAD"), method = "getTarget()Lorg/cyclops/integrateddynamics/api/part/PartPos;", cancellable = true, remap = false)
    private void getTarget(CallbackInfoReturnable<PartPos> callback) {
        PartPos orginal_pos = target;
        TileEntity te = orginal_pos.getPos().getWorld().getTileEntity(orginal_pos.getPos().getBlockPos());
        if (te instanceof TileAccessProxy) {
            callback.setReturnValue(PartPos.of(((TileAccessProxy) te).target, orginal_pos.getSide()));
        }
    }

    @Final
    @Shadow(remap = false)
    private PartPos target;
}
