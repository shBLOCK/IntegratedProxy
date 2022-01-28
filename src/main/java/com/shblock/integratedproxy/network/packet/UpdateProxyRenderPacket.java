package com.shblock.integratedproxy.network.packet;

import com.shblock.integratedproxy.client.data.AccessProxyClientData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

public class UpdateProxyRenderPacket extends PacketCodec {
    @CodecField
    private BlockPos proxy_pos;
    @CodecField
    private ResourceKey<Level> proxy_dim;
    @CodecField
    private BlockPos target_pos;
    @CodecField
    private ResourceKey<Level> target_dim;

    public UpdateProxyRenderPacket() { }

    public UpdateProxyRenderPacket(DimPos proxy_pos, DimPos target_pos) {
        this.proxy_pos = proxy_pos.getBlockPos();
        this.proxy_dim = proxy_pos.getLevelKey();
        this.target_pos = target_pos.getBlockPos();
        this.target_dim = target_pos.getLevelKey();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(Level world, Player player) {
        AccessProxyClientData.getInstance().putTarget(
                DimPos.of(this.proxy_dim, this.proxy_pos),
                DimPos.of(this.target_dim, this.target_pos)
        );
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) { }
}
