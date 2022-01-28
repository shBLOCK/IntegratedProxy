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

public class UpdateProxyDisplayRotationPacket extends PacketCodec {
    @CodecField
    private BlockPos proxy_pos;
    @CodecField
    private ResourceKey<Level> proxy_dim;
    @CodecField
    private CompoundTag rotation;

    public UpdateProxyDisplayRotationPacket() { }

    public UpdateProxyDisplayRotationPacket(DimPos proxy_pos, int[] rotation) {
        this.proxy_pos = proxy_pos.getBlockPos();
        this.proxy_dim = proxy_pos.getLevelKey();
        CompoundTag nbt = new CompoundTag();
        nbt.putIntArray("rot", rotation);
        this.rotation = nbt;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(Level world, Player player) {
        AccessProxyClientData.getInstance().putRotation(DimPos.of(this.proxy_dim, this.proxy_pos), this.rotation.getIntArray("rot"));
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) { }
}
