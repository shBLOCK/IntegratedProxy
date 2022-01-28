package com.shblock.integratedproxy;

import com.shblock.integratedproxy.block.BlockAccessProxyConfig;
import com.shblock.integratedproxy.inventory.container.ContainerAccessProxyConfig;
import com.shblock.integratedproxy.proxy.ClientProxy;
import com.shblock.integratedproxy.proxy.CommonProxy;
import com.shblock.integratedproxy.tileentity.TileAccessProxyConfig;
import net.minecraft.world.item.CreativeModeTab;
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

    public static IntegratedProxy _instance;

    public IntegratedProxy() {
        super(MODID, (instance) -> _instance = instance);
    }

    @Override
    protected CreativeModeTab constructDefaultCreativeModeTab() {
        return new ItemGroupMod(this, () -> IPRegistryEntries.ITEM_ACCESS_PROXY);
    }

    @Override
    public void onConfigsRegister(ConfigHandler configHandler) {
        super.onConfigsRegister(configHandler);
        configHandler.addConfigurable(new BlockAccessProxyConfig());
        configHandler.addConfigurable(new TileAccessProxyConfig());
        configHandler.addConfigurable(new ContainerAccessProxyConfig());
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
