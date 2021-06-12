package com.shblock.integrated_proxy;

import com.shblock.integrated_proxy.block.BlockAccessProxyConfig;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.config.extendedconfig.BlockItemConfigReference;
import org.cyclops.cyclopscore.init.ItemCreativeTab;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.init.RecipeHandler;
import org.cyclops.cyclopscore.proxy.ICommonProxy;

@Mod(
        modid = IntegratedProxy.MODID,
        name = IntegratedProxy.NAME,
        useMetadata = true,
        dependencies = "required-after:forge;required-after:cyclopscore;required-after:integrateddynamics;",
        guiFactory = "com.shblock.integrated_proxy.GuiConfigOverview$ExtendedConfigGuiFactory"
)
@Mod.EventBusSubscriber(modid = IntegratedProxy.MODID)
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
        return new ItemCreativeTab(this, new BlockItemConfigReference(BlockAccessProxyConfig.class));
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

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID)) {
            ConfigManager.sync(MODID, Config.Type.INSTANCE);
            clog("Changed config: range: " + BlockAccessProxyConfig.range);
        }
    }
}
