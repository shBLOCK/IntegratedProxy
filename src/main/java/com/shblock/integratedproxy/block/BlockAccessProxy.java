package com.shblock.integratedproxy.block;

import com.shblock.integratedproxy.tileentity.TileAccessProxy;
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
            System.out.println("remove render");
            ((TileAccessProxy) world.getTileEntity(pos)).sendRemoveRenderPacket();
            ((TileAccessProxy) world.getTileEntity(pos)).unRegisterEventHandle();
        }
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState blockState, PlayerEntity player) {
        onDestroy(world, pos);
        super.onBlockHarvested(world, pos, blockState, player);
    }

    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
        onDestroy(world, pos);
        super.onBlockExploded(state, world, pos, explosion);
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
