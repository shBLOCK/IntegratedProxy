package com.shblock.integratedproxy.inventory.container;

import com.shblock.integratedproxy.IntegratedProxy;
import com.shblock.integratedproxy.client.gui.GuiAccessProxy;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;

public class ContainerAccessProxyConfig extends GuiConfig<ContainerAccessProxy> {
    public ContainerAccessProxyConfig() {
        super(
                IntegratedProxy._instance,
                "access_proxy",
                eConfig -> new ContainerType<>(ContainerAccessProxy::new)
        );
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerAccessProxy>> ScreenManager.IScreenFactory<ContainerAccessProxy, U> getScreenFactory() {
        return new ScreenFactorySafe<>(GuiAccessProxy::new);
    }
}
