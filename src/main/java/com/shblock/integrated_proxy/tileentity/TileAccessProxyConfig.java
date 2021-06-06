package com.shblock.integrated_proxy.tileentity;

import com.google.common.collect.Sets;
import com.shblock.integrated_proxy.IPRegistryEntries;
import com.shblock.integrated_proxy.IntegratedProxy;
import net.minecraft.tileentity.TileEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;

public class TileAccessProxyConfig extends TileEntityConfig<TileAccessProxy> {
    public TileAccessProxyConfig() {
        super(
                IntegratedProxy._instance,
                "access_proxy",
                (eConfig) -> new TileEntityType<>(TileAccessProxy::new,
                        Sets.newHashSet(IPRegistryEntries.BLOCK_ACCESS_PROXY), null)
        );
    }
}
