package com.shblock.integrated_proxy;

import net.minecraft.client.gui.GuiScreen;
import org.cyclops.cyclopscore.client.gui.config.ExtendedConfigGuiFactoryBase;
import org.cyclops.cyclopscore.client.gui.config.GuiConfigOverviewBase;
import org.cyclops.cyclopscore.init.ModBase;

public class GuiConfigOverview extends GuiConfigOverviewBase {
    public GuiConfigOverview(GuiScreen parentScreen) {
        super(IntegratedProxy._instance, parentScreen);
    }

    @Override
    public ModBase getMod() {
        return IntegratedProxy._instance;
    }

    public static class ExtendedConfigGuiFactory extends ExtendedConfigGuiFactoryBase {

        @Override
        public Class<? extends GuiConfigOverviewBase> mainConfigGuiClass() {
            return GuiConfigOverview.class;
        }
    }
}
