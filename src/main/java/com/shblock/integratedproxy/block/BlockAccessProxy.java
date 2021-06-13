package com.shblock.integratedproxy.block;

import com.shblock.integratedproxy.IntegratedProxy;
import com.shblock.integratedproxy.storage.AccessProxyCollection;
import com.shblock.integratedproxy.tileentity.TileAccessProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneConfig;
import org.cyclops.integrateddynamics.core.block.BlockTileGuiCabled;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integratedtunnels.core.ExtendedFakePlayer;

@Mod.EventBusSubscriber(modid = IntegratedProxy.MODID)
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


    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return super.getCollisionShape(state, reader, pos);
    }

    @Override
    public void onBlockAdded(BlockState blockState, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(blockState, world, pos, oldState, isMoving);
        if (!world.isRemote) {
            AccessProxyCollection.getInstance(world).set(pos, pos);
            TileAccessProxy te = (TileAccessProxy) world.getTileEntity(pos);
//            if (te == null) {
//                return;
//            }
            te.target = DimPos.of(world, pos);
        }
    }

    private void onDestroy(IWorld world, BlockPos pos) {
        if (!world.isRemote()) {
            TileAccessProxy te = (TileAccessProxy) world.getTileEntity(pos);
            if (te == null) return;
            if (te.isRemoved() || te.target == null) return;
            te.sendRemoveRenderPacket();
            te.unRegisterEventHandle();
            AccessProxyCollection.getInstance((World) world).remove(pos);
            te.updateTargetBlock();
            te.target = null;
            TileAccessProxy.updateAfterBlockDestroy((World) world, pos);
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
    public void onReplaced(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        onDestroy(world, pos);
        super.onReplaced(oldState, world, pos, newState, isMoving);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if (!world.isRemote) {
            if (player.isSneaking()) {
                if (player.getHeldItem(hand).isEmpty()) {
                    ((TileAccessProxy) world.getTileEntity(pos)).changeDisableRender();
                    return ActionResultType.SUCCESS;
                }
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
            } else if (player.isSneaking()) {
                if (player.getHeldItem(hand).isEmpty()) {
                    return ActionResultType.SUCCESS;
                }
            } else {
                return super.onBlockActivated(blockState, world, pos, player, hand, rayTraceResult);
            }
        }
        return ActionResultType.FAIL;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, isMoving);
        if (neighborBlock instanceof BlockAccessProxy) {
            return;
        }
        if (!world.isRemote) {
            Vector3i facing_vec = fromPos.subtract(new Vector3i(pos.getX(), pos.getY(), pos.getZ()));
            Direction facing = Direction.getFacingFromVector(facing_vec.getX(), facing_vec.getY(), facing_vec.getZ());
            TileAccessProxy te = (TileAccessProxy) world.getTileEntity(pos);
            if (te != null && world.isBlockLoaded(te.target.getBlockPos()) && te.target != null && !te.isRemoved()) {
                IDynamicRedstone cap = TileHelpers.getCapability(DimPos.of(world, fromPos), facing.getOpposite(), DynamicRedstoneConfig.CAPABILITY).orElse(null);
                if (te.setSideRedstonePower(facing, cap)) {
                    te.updateTargetBlock();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        if (!ModList.get().isLoaded("integratedtunnels")) return;
        if (event.getWorld().getBlockState(event.getPos()).getBlock() instanceof BlockAccessProxy) {
            if (event.getPlayer() instanceof ExtendedFakePlayer) {
                event.setCanceled(true);
            }
        }
    }
}
