package com.shblock.integratedproxy.block;

import com.shblock.integratedproxy.IPRegistryEntries;
import com.shblock.integratedproxy.IntegratedProxy;
import com.shblock.integratedproxy.storage.AccessProxyCollection;
import com.shblock.integratedproxy.tileentity.TileAccessProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneConfig;
import org.cyclops.integrateddynamics.core.block.BlockWithEntityGuiCabled;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = IntegratedProxy.MODID)
public class BlockAccessProxy extends BlockWithEntityGuiCabled {
    public BlockAccessProxy(Properties properties) {
        super(properties, TileAccessProxy::new);
    }

//    @Override
//    public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos) {
//        return super.getCollisionShape(state, reader, pos);
//    }


    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, IPRegistryEntries.TILE_ACCESS_PROXY, TileAccessProxy::updateTileEntity);
    }

    @Override
    public void onPlace(BlockState blockState, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(blockState, world, pos, oldState, isMoving);
        if (!world.isClientSide) {
            AccessProxyCollection.getInstance(world).set(pos, pos);
            TileAccessProxy te = (TileAccessProxy) world.getBlockEntity(pos);
            te.target = DimPos.of(world, pos);
            for (Direction facing : Direction.values()) {
                if (te.target != null && world.isLoaded(te.target.getBlockPos()) && !te.isRemoved()) {
                    IDynamicRedstone cap = BlockEntityHelpers.getCapability(DimPos.of(world, pos.offset(facing.getNormal())), facing.getOpposite(), DynamicRedstoneConfig.CAPABILITY).orElse(null);
                    te.setSideRedstonePower(facing, cap);
                }
            }
        }
    }

    private void onDestroy(LevelReader world, BlockPos pos) {
        if (!world.isClientSide()) {
            TileAccessProxy te = (TileAccessProxy) world.getBlockEntity(pos);
            if (te == null) return;
            if (te.isRemoved() || te.target == null) return;
            te.sendRemoveRenderPacket();
            te.unRegisterEventHandle();
            AccessProxyCollection.getInstance((Level) world).remove(pos);
            te.updateTargetBlock();
            te.target = null;
            TileAccessProxy.updateAfterBlockDestroy((Level) world, pos);
        }
    }

    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos blockPos, Explosion explosion) {
        onDestroy(world, blockPos);
        super.onBlockExploded(state, world, blockPos, explosion);
    }

    @Override
    public void onRemove(BlockState oldState, Level world, BlockPos blockPos, BlockState newState, boolean isMoving) {
        onDestroy(world, blockPos);
        super.onRemove(oldState, world, blockPos, newState, isMoving);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        onDestroy(world, pos);
        return super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        if (!world.isClientSide) {
            if (player.isCrouching()) {
                if (player.getItemInHand(hand).isEmpty()) {
                    ((TileAccessProxy) world.getBlockEntity(pos)).changeDisableRender();
                    return InteractionResult.SUCCESS;
                }
                return super.use(blockState, world, pos, player, hand, rayTraceResult);
            } else {
                if (WrenchHelpers.isWrench(player, player.getItemInHand(hand), world, pos, rayTraceResult.getDirection())) {
                    ((TileAccessProxy) world.getBlockEntity(pos)).rotateDisplayValue(rayTraceResult.getDirection());
                    return InteractionResult.SUCCESS;
                } else {
                    return super.use(blockState, world, pos, player, hand, rayTraceResult);
                }
            }
        } else {
            if (WrenchHelpers.isWrench(player, player.getItemInHand(hand), world, pos, rayTraceResult.getDirection())) {
                return InteractionResult.SUCCESS;
            } else if (player.isCrouching()) {
                if (player.getItemInHand(hand).isEmpty()) {
                    return InteractionResult.SUCCESS;
                }
            } else {
                return super.use(blockState, world, pos, player, hand, rayTraceResult);
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChange(state, world, pos, fromPos);
        if (neighborBlock instanceof BlockAccessProxy) return;

        if (!world.isClientSide()) {
            Vec3i facing_vec = fromPos.subtract(new Vec3i(pos.getX(), pos.getY(), pos.getZ()));
            Direction facing = Direction.fromNormal(facing_vec.getX(), facing_vec.getY(), facing_vec.getZ());
            TileAccessProxy te = (TileAccessProxy) world.getBlockEntity(pos);
            if (te != null && te.target != null && world.isLoaded(te.target.getBlockPos()) && !te.isRemoved()) {
                IDynamicRedstone cap = BlockEntityHelpers.getCapability(DimPos.of(world, fromPos), facing.getOpposite(), DynamicRedstoneConfig.CAPABILITY).orElse(null);
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
            if (event.getPlayer() instanceof FakePlayer) {
                event.setCanceled(true);
            }
        }
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState p_49234_, Level p_49235_, BlockPos p_49236_) {
        return super.getMenuProvider(p_49234_, p_49235_, p_49236_);
    }
}
