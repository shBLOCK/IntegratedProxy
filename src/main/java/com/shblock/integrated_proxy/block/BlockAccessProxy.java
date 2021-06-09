package com.shblock.integrated_proxy.block;

import com.shblock.integrated_proxy.client.gui.GuiAccessProxy;
import com.shblock.integrated_proxy.inventory.container.ContainerAccessProxy;
import com.shblock.integrated_proxy.storage.AccessProxyCollection;
import com.shblock.integrated_proxy.tileentity.TileAccessProxy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneConfig;
import org.cyclops.integrateddynamics.core.block.BlockContainerGuiCabled;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;

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
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        if (!world.isRemote) {
            AccessProxyCollection.getInstance(world).set(pos, pos);
        }
    }

    @Override
    protected void onPreBlockDestroyed(World world, BlockPos pos) {
        if (!world.isRemote) {
            if (world.getTileEntity(pos) == null) {
                return;
            }
            ((TileAccessProxy) world.getTileEntity(pos)).sendRemoveRenderPacket();
            ((TileAccessProxy) world.getTileEntity(pos)).unRegisterEventHandle();
            AccessProxyCollection.getInstance(world).remove(pos);
        }
        super.onPreBlockDestroyed(world, pos);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            if (player.isSneaking()) {
                return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
            } else {
                if (WrenchHelpers.isWrench(player, player.getHeldItem(hand), world, pos, side)) {
                    ((TileAccessProxy) world.getTileEntity(pos)).rotateDisplayValue(side);
                    return true;
                } else {
                    return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
                }
            }
        } else {
            if (WrenchHelpers.isWrench(player, player.getHeldItem(hand), world, pos, side)) {
                return true;
            } else {
                return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
            }
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos);
        if (!world.isRemote) {
            for (EnumFacing side : EnumFacing.VALUES) {
                IDynamicRedstone cap = TileHelpers.getCapability(DimPos.of(world, pos.offset(side)), side.getOpposite(), DynamicRedstoneConfig.CAPABILITY);
                if (cap != null) {
                    TileAccessProxy te = (TileAccessProxy) world.getTileEntity(pos);
                    if (te == null) {
                        return;
                    }
                    te.setSideRedstonePower(side, cap.getRedstoneLevel());
                    te.markDirty();
                } else {
                    TileAccessProxy te = (TileAccessProxy) world.getTileEntity(pos);
                    if (te == null) {
                        return;
                    }
                    te.setSideRedstonePower(side, 0);
                    te.markDirty();
                }
            }
        }
    }

    @Override
    public boolean isKeepNBTOnDrop() {
        return false;
    }
}
