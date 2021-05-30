package com.shblock.integrated_proxy.network.packet;

import com.shblock.integrated_proxy.client.render.world.AccessProxyTargetRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

import java.util.List;

public class UpdateProxyDisplayRotationPacket extends PacketCodec {
    @CodecField
    private BlockPos proxy_pos;
    @CodecField
    private int proxy_dim;
    @CodecField
    private NBTTagCompound rotation;

    public UpdateProxyDisplayRotationPacket() { }

    public UpdateProxyDisplayRotationPacket(DimPos proxy_pos, int[] rotation) {
        this.proxy_pos = proxy_pos.getBlockPos();
        this.proxy_dim = proxy_pos.getDimensionId();
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setIntArray("rot", rotation);
        this.rotation = nbt;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        AccessProxyTargetRenderer.getInstance().putRotation(DimPos.of(this.proxy_dim, this.proxy_pos), this.rotation.getIntArray("rot"));
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) { }
}
