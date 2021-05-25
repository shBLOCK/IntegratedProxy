package com.shblock.integrated_proxy.proxy;

import com.shblock.integrated_proxy.IntegratedProxy;
import com.shblock.integrated_proxy.client.render.world.AccessProxyTargetRenderer;
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
        MinecraftForge.EVENT_BUS.register(AccessProxyTargetRenderer.getInstance());
    }
}
