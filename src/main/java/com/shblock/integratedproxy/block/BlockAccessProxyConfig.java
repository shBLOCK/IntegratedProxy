package com.shblock.integratedproxy.block;

import com.shblock.integratedproxy.IntegratedProxy;
import com.shblock.integratedproxy.item.ItemBlockAccessProxy;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;

public class BlockAccessProxyConfig extends BlockConfig {

//    public static BlockAccessProxyConfig _instance;
    public static final String REGISTRY_NAME = IntegratedProxy.MODID + "access_proxy";

    @ConfigurableProperty(
            category = "block",
            comment = "The max range of access proxy (square range, not radius), -1:infinite",
            isCommandable = true,
            minimalValue = -1)
    public static int range = -1;

    public BlockAccessProxyConfig() {
        super(
            IntegratedProxy._instance,
            "access_proxy",
            eConfig -> new BlockAccessProxy(
                    AbstractBlock.Properties.create(Material.ROCK)
                            .hardnessAndResistance(5.0F)
                            .sound(SoundType.METAL)
            ),
            (eConfig, block) -> new ItemBlockAccessProxy(block, new Item.Properties()
                    .group(IntegratedProxy._instance.getDefaultItemGroup())
            )
        );
    }
}
