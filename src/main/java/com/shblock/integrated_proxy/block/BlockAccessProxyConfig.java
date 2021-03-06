package com.shblock.integrated_proxy.block;

import com.shblock.integrated_proxy.IntegratedProxy;
import com.shblock.integrated_proxy.item.ItemBlockAccessProxy;
import com.shblock.integrated_proxy.tileentity.TileAccessProxy;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;

public class BlockAccessProxyConfig extends BlockContainerConfig {

    public static BlockAccessProxyConfig _instance;

    @ConfigurableProperty(
            category = ConfigurableTypeCategory.BLOCK,
            comment = "The max range of access proxy (square range, not radius), -1:infinite",
            isCommandable = true,
            minimalValue = -1)
    public static int range = -1;

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

    @Override
    public boolean isDisableable() {
        return false;
    }
}
