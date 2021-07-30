package com.shblock.integrated_proxy.mixin;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
//@IFMLLoadingPlugin.SortingIndex(-7500)
@IFMLLoadingPlugin.Name("IntegratedProxy Loading Plugin")
public class MixinLoader implements IFMLLoadingPlugin {
    public MixinLoader() {
        MixinBootstrap.init();
        MixinEnvironment.getEnvironment(MixinEnvironment.Phase.PREINIT).addConfiguration("mixins.integrated_proxy.loader.json");
        Logger.getGlobal().log(Level.INFO, "Added loader mixin");
        MixinEnvironment.getEnvironment(MixinEnvironment.Phase.DEFAULT).addConfiguration("mixins.integrated_proxy.world.json");
        Logger.getGlobal().log(Level.INFO, "Added world mixin");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) { }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
