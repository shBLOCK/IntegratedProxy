package com.shblock.integrated_proxy.block;

import com.shblock.integrated_proxy.client.gui.GuiAccessProxy;
import com.shblock.integrated_proxy.inventory.container.ContainerAccessProxy;
import com.shblock.integrated_proxy.tileentity.TileAccessProxy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.integrateddynamics.core.block.BlockContainerGuiCabled;

public class BlockAccessProxy extends BlockContainerGuiCabled {

    private static BlockAccessProxy _instance;

    public static BlockAccessProxy getInstance() {
        return _instance;
    }

    public BlockAccessProxy(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig, TileAccessProxy.class);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerAccessProxy.class;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiAccessProxy.class;
    }



    @Override
    protected void onPreBlockDestroyed(World world, BlockPos pos) {
        if (!world.isRemote) {
            if (world.getTileEntity(pos) == null) {
                return;
            }
            ((TileAccessProxy) world.getTileEntity(pos)).sendRemoveRenderPacket();
            ((TileAccessProxy) world.getTileEntity(pos)).unRegisterEventHandle();
        }
        super.onPreBlockDestroyed(world, pos);
    }
}
