package com.shblock.integratedproxy.proxy;

import com.shblock.integratedproxy.IntegratedProxy;
import com.shblock.integratedproxy.client.data.AccessProxyClientData;
import com.shblock.integratedproxy.client.render.world.AccessProxyTargetRenderer;
import net.minecraftforge.common.MinecraftForge;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.ClientProxyComponent;

public class ClientProxy extends ClientProxyComponent {
    public ClientProxy() {
        super(new CommonProxy());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ModBase getMod() {
        return IntegratedProxy._instance;
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        MinecraftForge.EVENT_BUS.register(AccessProxyTargetRenderer.class);
        MinecraftForge.EVENT_BUS.register(AccessProxyClientData.getInstance());
    }
}
