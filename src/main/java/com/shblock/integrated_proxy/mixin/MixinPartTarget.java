package com.shblock.integrated_proxy.mixin;

import com.shblock.integrated_proxy.tileentity.TileAccessProxy;
import net.minecraft.tileentity.TileEntity;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("SpellCheckingInspection")
@Mixin(PartTarget.class)
public class MixinPartTarget {
    @Inject(at = @At("HEAD"), method = "getTarget()Lorg/cyclops/integrateddynamics/api/part/PartPos;", cancellable = true, remap = false)
    private void getTarget(CallbackInfoReturnable<PartPos> callback) {
        PartPos original_pos = target;
        if (original_pos.getPos().getWorld() == null) {
            System.out.println("can't get target World");
            return;
        }
        TileEntity te = original_pos.getPos().getWorld().getTileEntity(original_pos.getPos().getBlockPos());
        if (te instanceof TileAccessProxy && ((TileAccessProxy) te).target != null) {
            callback.setReturnValue(PartPos.of(((TileAccessProxy) te).target, original_pos.getSide()));
        }
    }

    @Final
    @Shadow(remap = false)
    private PartPos target;
}
