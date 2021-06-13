package com.shblock.integrated_proxy.mixin;

import com.shblock.integrated_proxy.IntegratedProxy;
import org.apache.logging.log4j.Level;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integratedtunnels.core.part.PartTypeInterfacePositionedAddon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PartTypeInterfacePositionedAddon.class)
public class MixinPartTypeInterfacePositionedAddon {
    @Shadow(remap = false)
    protected void addTargetToNetwork(INetwork network, PartPos pos, int priority, int channelInterface, PartTypeInterfacePositionedAddon.State state) {
        IntegratedProxy.clog(Level.ERROR, "addTargetToNetwork shadow doesn't work!");
    }

    @Shadow(remap = false)
    protected void removeTargetFromNetwork(INetwork network, PartPos pos, PartTypeInterfacePositionedAddon.State state) {
        IntegratedProxy.clog(Level.ERROR, "removeTargetFromNetwork shadow doesn't work!");
    }

    @Inject(at = @At("HEAD"), method = "Lorg/cyclops/integratedtunnels/core/part/PartTypeInterfacePositionedAddon;updateTargetInNetwork(Lorg/cyclops/integrateddynamics/api/network/INetwork;Lorg/cyclops/integrateddynamics/api/part/PartPos;IILorg/cyclops/integratedtunnels/core/part/PartTypeInterfacePositionedAddon$State;)V", remap = false)
    protected void updateTargetInNetwork(INetwork network, PartPos pos, int priority, int channelInterface, PartTypeInterfacePositionedAddon.State state, CallbackInfo ci) {
        IntegratedProxy.clog(Level.WARN, "updateTargetInNetwork");
        removeTargetFromNetwork(network, pos, state);
        addTargetToNetwork(network, pos, priority, channelInterface, state);
    }
}
