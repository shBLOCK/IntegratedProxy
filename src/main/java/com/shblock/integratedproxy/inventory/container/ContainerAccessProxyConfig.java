package com.shblock.integratedproxy.inventory.container;

import com.shblock.integratedproxy.IntegratedProxy;
import com.shblock.integratedproxy.client.gui.GuiAccessProxy;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;

public class ContainerAccessProxyConfig extends GuiConfig<ContainerAccessProxy> {
    public ContainerAccessProxyConfig() {
        super(
                IntegratedProxy._instance,
                "access_proxy",
                eConfig -> new MenuType<>(ContainerAccessProxy::new)
        );
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerAccessProxy>> MenuScreens.ScreenConstructor<ContainerAccessProxy, U> getScreenFactory() {
        return new ScreenFactorySafe<>(GuiAccessProxy::new);
    }
}
