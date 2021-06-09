package com.shblock.integrated_proxy.network.packet;

import com.shblock.integrated_proxy.client.data.AccessProxyClientData;
import com.shblock.integrated_proxy.client.render.world.AccessProxyTargetRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

public class RemoveProxyRenderPacket extends PacketCodec {
    @CodecField
    private BlockPos proxy_pos;
    @CodecField
    private int proxy_dim;

    public RemoveProxyRenderPacket() { }

    public RemoveProxyRenderPacket(DimPos proxy_pos) {
        this.proxy_pos = proxy_pos.getBlockPos();
        this.proxy_dim = proxy_pos.getDimensionId();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        AccessProxyClientData.getInstance().remove(
                DimPos.of(this.proxy_dim, this.proxy_pos)
        );
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) { }
}
