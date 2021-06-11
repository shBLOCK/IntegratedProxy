package com.shblock.integratedproxy.proxy;

import com.shblock.integratedproxy.IntegratedProxy;
import com.shblock.integratedproxy.network.packet.*;
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

        packetHandler.register(RemoveProxyRenderPacket.class);
        packetHandler.register(UpdateProxyRenderPacket.class);
        packetHandler.register(UpdateProxyDisplayValuePacket.class);
        packetHandler.register(UpdateProxyDisplayRotationPacket.class);
        packetHandler.register(UpdateDisableRenderPacket.class);
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();

        //MinecraftForge.EVENT_BUS.register(TileAccessProxy.class);
    }
}
