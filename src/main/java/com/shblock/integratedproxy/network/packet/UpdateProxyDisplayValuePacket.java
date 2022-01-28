package com.shblock.integratedproxy.network.packet;

import com.shblock.integratedproxy.client.data.AccessProxyClientData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;

public class UpdateProxyDisplayValuePacket extends PacketCodec {
    @CodecField
    private BlockPos proxy_pos;
    @CodecField
    private ResourceKey<Level> proxy_dim;
    @CodecField
    private CompoundTag nbt;

    public UpdateProxyDisplayValuePacket() { }

    public UpdateProxyDisplayValuePacket(DimPos proxy_pos, IValue value) {
        this.proxy_pos = proxy_pos.getBlockPos();
        this.proxy_dim = proxy_pos.getLevelKey();
        if (value == null) {
            this.nbt = new CompoundTag();
            return;
        }
        this.nbt = ValueHelpers.serialize(value);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(Level world, Player player) {
        if (nbt.isEmpty()) {
            AccessProxyClientData.getInstance().putVariable(DimPos.of(this.proxy_dim, this.proxy_pos), null);
            return;
        }
        AccessProxyClientData.getInstance().putVariable(DimPos.of(this.proxy_dim, this.proxy_pos), ValueHelpers.deserialize(nbt));
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) { }
}
