package com.shblock.integratedproxy.network.packet;

import com.shblock.integratedproxy.client.data.AccessProxyClientData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

public class UpdateDisableRenderPacket extends PacketCodec {
    @CodecField
    private BlockPos proxy_pos;
    @CodecField
    private RegistryKey<World> proxy_dim;
    @CodecField
    private boolean disable;

    public UpdateDisableRenderPacket() { }

    public UpdateDisableRenderPacket(DimPos proxy_pos, boolean disable) {
        this.proxy_pos = proxy_pos.getBlockPos();
        this.proxy_dim = proxy_pos.getWorldKey();
        this.disable = disable;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, PlayerEntity player) {
        AccessProxyClientData.getInstance().putDisable(DimPos.of(this.proxy_dim, this.proxy_pos), this.disable);
    }

    @Override
    public void actionServer(World world, ServerPlayerEntity player) { }
}
