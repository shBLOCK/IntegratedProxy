package com.shblock.integratedproxy;

import com.shblock.integratedproxy.block.BlockAccessProxy;
import com.shblock.integratedproxy.inventory.container.ContainerAccessProxy;
import com.shblock.integratedproxy.item.ItemBlockAccessProxy;
import com.shblock.integratedproxy.tileentity.TileAccessProxy;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class IPRegistryEntries {
    @ObjectHolder(IntegratedProxy.MODID + ":access_proxy")
    public static final ItemBlockAccessProxy ITEM_ACCESS_PROXY = null;

    @ObjectHolder(IntegratedProxy.MODID + ":access_proxy")
    public static final BlockAccessProxy BLOCK_ACCESS_PROXY = null;

    @ObjectHolder(IntegratedProxy.MODID + ":access_proxy")
    public static final TileEntityType<TileAccessProxy> TILE_ACCESS_PROXY = null;

    @ObjectHolder(IntegratedProxy.MODID + ":access_proxy")
    public static final ContainerType<ContainerAccessProxy> CONTAINER_ACCESS_PROXY = null;
}
