package com.shblock.integratedproxy.network.packet;

import com.shblock.integratedproxy.client.render.world.AccessProxyTargetRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;

public class UpdateProxyDisplayValuePacket extends PacketCodec {
    @CodecField
    private BlockPos proxy_pos;
    @CodecField
    private RegistryKey<World> proxy_dim;
    @CodecField
    private CompoundNBT nbt;

    public UpdateProxyDisplayValuePacket() { }

    public UpdateProxyDisplayValuePacket(DimPos proxy_pos, IValue value) {
        this.proxy_pos = proxy_pos.getBlockPos();
        this.proxy_dim = proxy_pos.getWorldKey();
        if (value == null) {
            this.nbt = new CompoundNBT();
            return;
        }
        this.nbt = ValueHelpers.serialize(value);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, PlayerEntity player) {
        if (nbt.isEmpty()) {
            AccessProxyTargetRenderer.getInstance().putVariable(DimPos.of(this.proxy_dim, this.proxy_pos), null);
            return;
        }
        AccessProxyTargetRenderer.getInstance().putVariable(DimPos.of(this.proxy_dim, this.proxy_pos), ValueHelpers.deserialize(nbt));
    }

    @Override
    public void actionServer(World world, ServerPlayerEntity player) { }
}
