package com.shblock.integrated_proxy.network.packet;

import com.shblock.integrated_proxy.client.render.world.AccessProxyTargetRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

public class UpdateProxyDisplayRotationPacket extends PacketCodec {
    @CodecField
    private BlockPos proxy_pos;
    @CodecField
    private RegistryKey<World> proxy_dim;
    @CodecField
    private CompoundNBT rotation;

    public UpdateProxyDisplayRotationPacket() { }

    public UpdateProxyDisplayRotationPacket(DimPos proxy_pos, int[] rotation) {
        this.proxy_pos = proxy_pos.getBlockPos();
        this.proxy_dim = proxy_pos.getWorldKey();
        CompoundNBT nbt = new CompoundNBT();
        nbt.putIntArray("rot", rotation);
        this.rotation = nbt;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, PlayerEntity player) {
        AccessProxyTargetRenderer.getInstance().putRotation(DimPos.of(this.proxy_dim, this.proxy_pos), this.rotation.getIntArray("rot"));
    }

    @Override
    public void actionServer(World world, ServerPlayerEntity player) { }
}
