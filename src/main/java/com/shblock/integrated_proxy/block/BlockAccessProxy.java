package com.shblock.integrated_proxy.block;

import com.shblock.integrated_proxy.tileentity.TileAccessProxy;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.core.block.BlockTileGuiCabled;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;

public class BlockAccessProxy extends BlockTileGuiCabled {

//    private static BlockAccessProxy _instance;
//
//    public static BlockAccessProxy getInstance() {
//        return _instance;
//    }

    public BlockAccessProxy(Properties properties) {
        super(properties, TileAccessProxy::new);
    }

//    @Override
//    public Class<? extends Container> getContainer() {
//        return ContainerAccessProxy.class;
//    }
//
//    @Override
//    public Class<? extends GuiScreen> getGui() {
//        return GuiAccessProxy.class;
//    }

    private void onDestroy(IWorld world, BlockPos pos) {
        if (!world.isRemote()) {
            if (world.getTileEntity(pos) == null) {
                return;
            }
            ((TileAccessProxy) world.getTileEntity(pos)).sendRemoveRenderPacket();
            ((TileAccessProxy) world.getTileEntity(pos)).unRegisterEventHandle();
        }
    }

    @Override
    public void onPlayerDestroy(IWorld world, BlockPos pos, BlockState blockState) {
        onDestroy(world, pos);
        super.onPlayerDestroy(world, pos, blockState);
    }

    @Override
    public void onExplosionDestroy(World world, BlockPos pos, Explosion explosion) {
        onDestroy(world, pos);
        super.onExplosionDestroy(world, pos, explosion);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if (!world.isRemote) {
            if (player.isSneaking()) {
                return super.onBlockActivated(blockState, world, pos, player, hand, rayTraceResult);
            } else {
                if (WrenchHelpers.isWrench(player, player.getHeldItem(hand), world, pos, rayTraceResult.getFace())) {
                    ((TileAccessProxy) world.getTileEntity(pos)).rotateDisplayValue(rayTraceResult.getFace());
                    return ActionResultType.SUCCESS;
                } else {
                    return super.onBlockActivated(blockState, world, pos, player, hand, rayTraceResult);
                }
            }
        } else {
            if (WrenchHelpers.isWrench(player, player.getHeldItem(hand), world, pos, rayTraceResult.getFace())) {
                return ActionResultType.SUCCESS;
            } else {
                return super.onBlockActivated(blockState, world, pos, player, hand, rayTraceResult);
            }
        }
    }
}
