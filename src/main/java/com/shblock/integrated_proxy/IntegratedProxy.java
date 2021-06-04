package com.shblock.integrated_proxy;

import com.shblock.integrated_proxy.block.BlockAccessProxyConfig;
import com.shblock.integrated_proxy.proxy.ClientProxy;
import com.shblock.integrated_proxy.proxy.CommonProxy;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.init.ItemGroupMod;
import org.cyclops.cyclopscore.init.ModBaseVersionable;
import org.cyclops.cyclopscore.proxy.IClientProxy;
import org.cyclops.cyclopscore.proxy.ICommonProxy;

@Mod(IntegratedProxy.MODID)
public class IntegratedProxy extends ModBaseVersionable<IntegratedProxy> {

    public static final String MODID = "integrated_proxy";
//    public static final String NAME = "Integrated Proxy";

    public static IntegratedProxy _instance;

    public IntegratedProxy() {
        super(MODID, (instance) -> _instance = instance);
    }

//    @Override
//    public void postInit(FMLPostInitializationEvent event) {
//        super.postInit(event);
//    }
//
//    @Mod.EventHandler
//    @Override
//    public final void preInit(FMLPreInitializationEvent event) {
//        super.preInit(event);
//    }
//
//    @Mod.EventHandler
//    @Override
//    public final void init(FMLInitializationEvent event) {
//        super.init(event);
//    }
//
//    @Override
//    protected RecipeHandler constructRecipeHandler() {
//        return null;
//    }

    @Override
    public ItemGroup constructDefaultItemGroup() {
        return new ItemGroupMod(this, () -> Items.DIAMOND);
    }

    @Override
    public void onConfigsRegister(ConfigHandler configHandler) {
        super.onConfigsRegister(configHandler);
        configHandler.addConfigurable(new BlockAccessProxyConfig());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected IClientProxy constructClientProxy() {
        return new ClientProxy();
    }

    @Override
    protected ICommonProxy constructCommonProxy() {
        return new CommonProxy();
    }

    public static void clog(String message) {
        IntegratedProxy._instance.log(Level.INFO, message);
    }

    public static void clog(Level level, String message) {
        IntegratedProxy._instance.log(level, message);
    }
}
