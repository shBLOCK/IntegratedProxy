package com.shblock.integrated_proxy.network.packet;

import com.shblock.integrated_proxy.client.render.world.AccessProxyTargetRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

public class UpdateProxyRenderPacket extends PacketCodec {
    @CodecField
    private BlockPos proxy_pos;
    @CodecField
    private int proxy_dim;
    @CodecField
    private BlockPos target_pos;
    @CodecField
    private int target_dim;

    public UpdateProxyRenderPacket() { }

    public UpdateProxyRenderPacket(DimPos proxy_pos, DimPos target_pos) {
        this.proxy_pos = proxy_pos.getBlockPos();
        this.proxy_dim = proxy_pos.getDimensionId();
        this.target_pos = target_pos.getBlockPos();
        this.target_dim = target_pos.getDimensionId();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        AccessProxyTargetRenderer.getInstance().put(
                DimPos.of(this.proxy_dim, this.proxy_pos),
                DimPos.of(this.target_dim, this.target_pos)
        );
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) { }
}
