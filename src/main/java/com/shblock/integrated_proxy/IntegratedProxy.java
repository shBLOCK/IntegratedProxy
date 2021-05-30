package com.shblock.integrated_proxy;

import com.shblock.integrated_proxy.block.BlockAccessProxyConfig;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.init.ItemCreativeTab;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.init.RecipeHandler;
import org.cyclops.cyclopscore.proxy.ICommonProxy;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.Overwrite;

@Mod(
        modid = IntegratedProxy.MODID,
        name = IntegratedProxy.NAME,
        useMetadata = true,
        dependencies = "required-after:forge;required-after:cyclopscore;required-after:integrateddynamics;"
)
public class IntegratedProxy extends ModBase {

    public static final String MODID = "integrated_proxy";
    public static final String NAME = "Integrated Proxy";

    @SidedProxy(clientSide = "com.shblock.integrated_proxy.proxy.ClientProxy", serverSide = "com.shblock.integrated_proxy.proxy.CommonProxy")
    public static ICommonProxy proxy;

    @Mod.Instance(value = MODID)
    public static IntegratedProxy _instance;

    public IntegratedProxy() {
        super(MODID, NAME);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Mod.EventHandler
    @Override
    public final void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Mod.EventHandler
    @Override
    public final void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    protected RecipeHandler constructRecipeHandler() {
        return null;
    }

    @Override
    public CreativeTabs constructDefaultCreativeTab() {
        return new ItemCreativeTab(this, () -> Items.DIAMOND);
    }

    @Override
    public void onMainConfigsRegister(ConfigHandler configHandler) {
        configHandler.add(new BlockAccessProxyConfig());
    }

    @Override
    public ICommonProxy getProxy() {
        return proxy;
    }

    public static void clog(String message) {
        IntegratedProxy._instance.log(Level.INFO, message);
    }

    public static void clog(Level level, String message) {
        IntegratedProxy._instance.log(level, message);
    }
}
