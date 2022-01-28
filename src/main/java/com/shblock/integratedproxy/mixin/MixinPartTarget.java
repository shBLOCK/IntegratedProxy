package com.shblock.integratedproxy.mixin;

import com.shblock.integratedproxy.tileentity.TileAccessProxy;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PartTarget.class)
public class MixinPartTarget {
    @Inject(at = @At("HEAD"), method = "getTarget()Lorg/cyclops/integrateddynamics/api/part/PartPos;", cancellable = true, remap = false)
    private void getTarget(CallbackInfoReturnable<PartPos> callback) {
        PartPos orginal_pos = target;
        BlockEntity te = orginal_pos.getPos().getLevel(false).getBlockEntity(orginal_pos.getPos().getBlockPos());
        if (te instanceof TileAccessProxy && ((TileAccessProxy) te).target != null) {
            callback.setReturnValue(PartPos.of(((TileAccessProxy) te).target, orginal_pos.getSide()));
        }
    }

    @Final
    @Shadow(remap = false)
    private PartPos target;
}
