package com.shblock.integrated_proxy.network.packet;

import com.shblock.integrated_proxy.client.render.world.AccessProxyTargetRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
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
    private int proxy_dim;
    @CodecField
    private NBTTagCompound nbt;

    public UpdateProxyDisplayValuePacket() { }

    public UpdateProxyDisplayValuePacket(DimPos proxy_pos, IValue value) {
        this.proxy_pos = proxy_pos.getBlockPos();
        this.proxy_dim = proxy_pos.getDimensionId();
        if (value == null) {
            this.nbt = new NBTTagCompound();
            return;
        }
        this.nbt = ValueHelpers.serialize(value);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        if (nbt.isEmpty()) {
            AccessProxyTargetRenderer.getInstance().putVariable(DimPos.of(this.proxy_dim, this.proxy_pos), null);
            return;
        }
        AccessProxyTargetRenderer.getInstance().putVariable(DimPos.of(this.proxy_dim, this.proxy_pos), ValueHelpers.deserialize(nbt));
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) { }
}
