package com.shblock.integrated_proxy.proxy;

import com.shblock.integrated_proxy.IntegratedProxy;
import com.shblock.integrated_proxy.network.packet.RemoveProxyRenderPacket;
import com.shblock.integrated_proxy.network.packet.UpdateProxyRenderPacket;
import com.shblock.integrated_proxy.tileentity.TileAccessProxy;
import net.minecraftforge.common.MinecraftForge;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.network.PacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;

public class CommonProxy extends CommonProxyComponent {
    @Override
    public ModBase getMod() {
        return IntegratedProxy._instance;
    }

    @Override
    public void registerPacketHandlers(PacketHandler packetHandler) {
        super.registerPacketHandlers(packetHandler);

        packetHandler.register(UpdateProxyRenderPacket.class);
        packetHandler.register(RemoveProxyRenderPacket.class);
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();

        //MinecraftForge.EVENT_BUS.register(TileAccessProxy.class);
    }
}
