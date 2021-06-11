package com.shblock.integrated_proxy.network.packet;

import com.shblock.integrated_proxy.client.data.AccessProxyClientData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

public class UpdateProxyDisableRenderPacket extends PacketCodec {
    @CodecField
    private BlockPos proxy_pos;
    @CodecField
    private int proxy_dim;
    @CodecField
    private boolean disable;

    public UpdateProxyDisableRenderPacket() { }

    public UpdateProxyDisableRenderPacket(DimPos proxy_pos, boolean disable) {
        this.proxy_pos = proxy_pos.getBlockPos();
        this.proxy_dim = proxy_pos.getDimensionId();
        this.disable = disable;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        AccessProxyClientData.getInstance().putDisable(DimPos.of(this.proxy_dim, this.proxy_pos), this.disable);
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) { }
}
