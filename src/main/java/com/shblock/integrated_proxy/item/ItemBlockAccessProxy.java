package com.shblock.integrated_proxy.item;

import net.minecraft.block.Block;
import org.cyclops.cyclopscore.item.ItemBlockNBT;

public class ItemBlockAccessProxy extends ItemBlockNBT {
    public ItemBlockAccessProxy(Block block) {
        super(block);
        this.setMaxStackSize(64);
    }
}
