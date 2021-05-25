package com.shblock.integrated_proxy.block;

import com.shblock.integrated_proxy.IntegratedProxy;
import com.shblock.integrated_proxy.item.ItemBlockAccessProxy;
import net.minecraft.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;

public class BlockAccessProxyConfig extends BlockContainerConfig {

    public static BlockAccessProxyConfig _instance;

    public BlockAccessProxyConfig() {
        super(
            IntegratedProxy._instance,
            true,
            "access_proxy",
            null,
            BlockAccessProxy.class
        );
    }

    @Override
    public Class<? extends Item> getItemBlockClass() {
        return ItemBlockAccessProxy.class;
    }
}
