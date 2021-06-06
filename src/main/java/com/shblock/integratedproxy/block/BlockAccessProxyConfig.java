package com.shblock.integratedproxy.block;

import com.shblock.integratedproxy.IntegratedProxy;
import com.shblock.integratedproxy.item.ItemBlockAccessProxy;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;

public class BlockAccessProxyConfig extends BlockConfig {

//    public static BlockAccessProxyConfig _instance;
    public static final String REGISTRY_NAME = IntegratedProxy.MODID + "access_proxy";

    public BlockAccessProxyConfig() {
        super(
            IntegratedProxy._instance,
            "access_proxy",
            eConfig -> new BlockAccessProxy(
                    AbstractBlock.Properties.create(Material.ROCK)
            ),
            (eConfig, block) -> new ItemBlockAccessProxy(block, new Item.Properties()
                    .group(IntegratedProxy._instance.getDefaultItemGroup())
            )
        );
    }
}
