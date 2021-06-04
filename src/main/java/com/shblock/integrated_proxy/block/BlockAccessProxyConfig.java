package com.shblock.integrated_proxy.block;

import com.shblock.integrated_proxy.IntegratedProxy;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;

public class BlockAccessProxyConfig extends BlockConfig {

//    public static BlockAccessProxyConfig _instance;

    public BlockAccessProxyConfig() {
        super(
            IntegratedProxy._instance,
            "access_proxy",
            eConfig -> new BlockAccessProxy(
                    AbstractBlock.Properties.create(Material.ROCK)
            ),
            getDefaultItemConstructor(IntegratedProxy._instance)
        );
    }
}
