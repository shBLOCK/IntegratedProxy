package com.shblock.integratedproxy.tileentity;

import com.google.common.collect.Sets;
import com.shblock.integratedproxy.IPRegistryEntries;
import com.shblock.integratedproxy.IntegratedProxy;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;

public class TileAccessProxyConfig extends BlockEntityConfig<TileAccessProxy> {
    public TileAccessProxyConfig() {
        super(
                IntegratedProxy._instance,
                "access_proxy",
                (eConfig) -> new BlockEntityType<>(TileAccessProxy::new,
                        Sets.newHashSet(IPRegistryEntries.BLOCK_ACCESS_PROXY), null)
        );
    }
}
