package com.shblock.integratedproxy.tileentity;

import com.shblock.integratedproxy.IPRegistryEntries;
import com.shblock.integratedproxy.IntegratedProxy;
import com.shblock.integratedproxy.block.BlockAccessProxy;
import com.shblock.integratedproxy.block.BlockAccessProxyConfig;
import com.shblock.integratedproxy.id_network.AccessProxyNetworkElement;
import com.shblock.integratedproxy.inventory.container.ContainerAccessProxy;
import com.shblock.integratedproxy.network.packet.*;
import com.shblock.integratedproxy.storage.AccessProxyCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkEventListener;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerConfig;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerDefault;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityCableConnectableInventory;
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.network.event.NetworkElementAddEvent;
import org.cyclops.integrateddynamics.core.network.event.NetworkElementRemoveEvent;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.item.ItemVariable;
import org.cyclops.integrateddynamics.part.PartTypeBlockReader;
import org.cyclops.integratedtunnels.core.part.PartTypeInterfacePositionedAddon;

import javax.annotation.Nullable;
import java.util.*;

public class TileAccessProxy extends BlockEntityCableConnectableInventory implements IDirtyMarkListener, INetworkEventListener<AccessProxyNetworkElement>, MenuProvider {

    public static final int SLOT_X = 0;
    public static final int SLOT_Y = 1;
    public static final int SLOT_Z = 2;
    public static final int SLOT_DISPLAY = 3;

    public final InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator_x;
    public final InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator_y;
    public final InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator_z;
    public final InventoryVariableEvaluator<IValue> evaluator_display;
    private IValue display_value;
    private final IVariableContainer variableContainer;
    private boolean shouldSendUpdateEvent = false;

    public DimPos target;
    public int pos_mode = 0;
    public int[] display_rotations = new int[]{0, 0, 0, 0, 0, 0};
    private int[] redstone_powers = new int[]{0, 0, 0, 0, 0, 0};
    private int[] strong_powers = new int[]{0, 0, 0, 0, 0, 0};
    public boolean disable_render = false;

    public TileAccessProxy(BlockPos blockPos, BlockState blockState) {
        super(IPRegistryEntries.TILE_ACCESS_PROXY, blockPos, blockState,4, 1);
        getInventory().addDirtyMarkListener(this);

        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, LazyOptional.of(() -> new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(Level world, BlockPos blockPos) {
                return new AccessProxyNetworkElement(DimPos.of(world, blockPos));
            }
        }));
        this.variableContainer = new VariableContainerDefault();
        addCapabilityInternal(VariableContainerConfig.CAPABILITY, LazyOptional.of(() -> this.variableContainer));
        this.evaluator_x = new InventoryVariableEvaluator<>(getInventory(), SLOT_X, ValueTypes.INTEGER);
        this.evaluator_y = new InventoryVariableEvaluator<>(getInventory(), SLOT_Y, ValueTypes.INTEGER);
        this.evaluator_z = new InventoryVariableEvaluator<>(getInventory(), SLOT_Z, ValueTypes.INTEGER);
        this.evaluator_display = new InventoryVariableEvaluator<>(getInventory(), SLOT_DISPLAY, ValueTypes.CATEGORY_ANY);
    }

    @Override
    protected SimpleInventory createInventory(int inventorySize, int stackSize) {
        return new SimpleInventory(inventorySize, stackSize) {
            @Override
            public boolean canPlaceItem(int i, ItemStack stack) {
                return super.canPlaceItem(i, stack) && stack.getItem() instanceof ItemVariable;//TODO: fix the issue here?
            }

            @Override
            public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, @Nullable Direction direction) {
                return false;
            }

            @Override
            public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
                return false;
            }

            @Override
            public int[] getSlotsForFace(Direction side) {
                return new int[]{};
            }

            @Override
            public IItemHandler getItemHandler() {
                return new InvWrapper(this) {
                    @Override
                    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                        return stack;
                    }

                    @Override
                    public ItemStack extractItem(int slot, int amount, boolean simulate) {
                        return ItemStack.EMPTY;
                    }
                };
            }
        };
    }

    @Override
    public CompoundTag writeToItemStack(CompoundTag tag) {
        return new CompoundTag();
    }



    @Override
    public CompoundTag save(CompoundTag tag) {
        tag = super.save(tag);
        tag.putInt("pos_mode", this.pos_mode);
        NBTClassType.writeNbt(List.class, "errors_x", evaluator_x.getErrors(), tag);
        NBTClassType.writeNbt(List.class, "errors_y", evaluator_y.getErrors(), tag);
        NBTClassType.writeNbt(List.class, "errors_z", evaluator_z.getErrors(), tag);
        NBTClassType.writeNbt(List.class, "errors_display", evaluator_display.getErrors(), tag);
        tag.putIntArray("display_rotations", this.display_rotations);
        if (getDisplayValue() != null) {
//            tag.setString("displayValueType", value.getType().getTranslationKey());
//            tag.setString("displayValue", ValueHelpers.serializeRaw(value));
            tag.put("displayValue", ValueHelpers.serialize(getDisplayValue()));
        }
        tag.putIntArray("rs_power", this.redstone_powers);
        tag.putIntArray("strong_power", this.strong_powers);
        tag.putBoolean("disable_render", this.disable_render);

        return tag;
    }

    @Override
    public void read(CompoundTag tag) {
        super.read(tag);
        this.pos_mode = tag.getInt("pos_mode");
        this.evaluator_x.setErrors(NBTClassType.readNbt(List.class, "errors_x", tag));
        this.evaluator_y.setErrors(NBTClassType.readNbt(List.class, "errors_y", tag));
        this.evaluator_z.setErrors(NBTClassType.readNbt(List.class, "errors_z", tag));
        this.evaluator_display.setErrors(NBTClassType.readNbt(List.class, "errors_display", tag));
        this.display_rotations = tag.getIntArray("display_rotations");
        if (tag.contains("displayValue", Tag.TAG_COMPOUND)) {
            setDisplayValue(ValueHelpers.deserialize(tag.getCompound("displayValue")));
        } else {
            setDisplayValue(null);
        }
        if (tag.contains("rs_power")) {
            this.redstone_powers = tag.getIntArray("rs_power");
        }
        if (tag.contains("strong_power")) {
            this.strong_powers = tag.getIntArray("strong_power");
        }
        if (tag.contains("disable_render")) {
            this.disable_render = tag.getBoolean("disable_render");
        }

        this.shouldSendUpdateEvent = true;
    }

    public IValue getDisplayValue() {
        return this.display_value;
    }

    public void setDisplayValue(IValue displayValue) {
        this.display_value = displayValue;
    }

    public void rotateDisplayValue(Direction side) {
        int ord = side.ordinal();
        this.display_rotations[ord] ++;
        if (this.display_rotations[ord] >= 4) {
            this.display_rotations[ord] = 0;
        }
        setChanged();
        IntegratedProxy._instance.getPacketHandler().sendToAll(new UpdateProxyDisplayRotationPacket(DimPos.of(this.level, this.worldPosition), this.display_rotations));
    }

    public void changeDisableRender() {
        this.disable_render = !this.disable_render;
        setChanged();
        IntegratedProxy._instance.getPacketHandler().sendToAll(new UpdateProxyDisableRenderPacket(DimPos.of(this.level, this.worldPosition), this.disable_render));
    }

    protected void refreshVariables(boolean sendVariablesUpdateEvent) {
        this.evaluator_x.refreshVariable(getNetwork(), false);
        this.evaluator_y.refreshVariable(getNetwork(), false);
        this.evaluator_z.refreshVariable(getNetwork(), false);
        this.evaluator_display.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
    }

    public int getVariableIntValue(InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator) throws EvaluationException {
        return evaluator.getVariable(getNetwork()).getValue().cast(ValueTypes.INTEGER).getRawValue();
    }

    private boolean isTargetOutOfRange(BlockPos target) {
        if (BlockAccessProxyConfig.range < 0) {
            return false;
        }
        return Math.abs(target.getX() - this.worldPosition.getX()) > BlockAccessProxyConfig.range ||
                Math.abs(target.getY() - this.worldPosition.getY()) > BlockAccessProxyConfig.range ||
                Math.abs(target.getZ() - this.worldPosition.getZ()) > BlockAccessProxyConfig.range;
    }

    private void updateTarget() {
        if (!this.level.isClientSide) {
            DimPos old_target = this.target == null ? null : DimPos.of(this.target.getLevel(), this.target.getBlockPos());
            try {
                if (this.pos_mode == 1) {
                    this.target = DimPos.of(
                            this.level,
                            new BlockPos(
                                    isVariableAvailable(this.evaluator_x) ? getVariableIntValue(this.evaluator_x) : this.worldPosition.getX(),
                                    isVariableAvailable(this.evaluator_y) ? getVariableIntValue(this.evaluator_y) : this.worldPosition.getY(),
                                    isVariableAvailable(this.evaluator_z) ? getVariableIntValue(this.evaluator_z) : this.worldPosition.getZ()
                            )
                    );
                } else {
                    this.target = DimPos.of(
                            this.level,
                            new BlockPos(
                                    isVariableAvailable(this.evaluator_x) ? getVariableIntValue(this.evaluator_x) + this.worldPosition.getX() : this.worldPosition.getX(),
                                    isVariableAvailable(this.evaluator_y) ? getVariableIntValue(this.evaluator_y) + this.worldPosition.getY() : this.worldPosition.getY(),
                                    isVariableAvailable(this.evaluator_z) ? getVariableIntValue(this.evaluator_z) + this.worldPosition.getZ() : this.worldPosition.getZ()
                            )
                    );
                }
            } catch (EvaluationException e) {
                this.target = DimPos.of(this.level, this.worldPosition);
            }

            if (isTargetOutOfRange(this.target.getBlockPos())) {
                this.target = DimPos.of(this.level, this.worldPosition);
            }

            if (!this.target.equals(old_target)) {
                if (old_target != null) notifyTargetChange();
                IntegratedProxy._instance.getPacketHandler().sendToAll(new UpdateProxyRenderPacket(DimPos.of(this.level, this.worldPosition), this.target));
                AccessProxyCollection.getInstance(this.level).set(this.worldPosition, this.target.getBlockPos());
                updateTargetBlock();
                if (old_target != null) {
                    updateTargetBlock(this.level, old_target.getBlockPos());
                }
            }
        }
    }

    private boolean isVariableAvailable(InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator) {
        if (getNetwork() == null) {
            return false;
        }
        if (evaluator.getVariable(getNetwork()) == null) {
            return false;
        }
        return evaluator.hasVariable() && evaluator.getErrors().isEmpty();
    }

    public boolean variableOk(InventoryVariableEvaluator<IValue> evaluator) {
        if (getNetwork() == null) {
            return false;
        }
        if (evaluator.getVariable(getNetwork()) == null) {
            return false;
        }
        return evaluator.hasVariable() &&
                evaluator.getErrors().isEmpty();
    }

    public boolean variableIntegerOk(InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator) {
        if (getNetwork() == null) {
            return false;
        }
        if (evaluator.getVariable(getNetwork()) == null) {
            return false;
        }
        return evaluator.hasVariable() &&
                evaluator.getVariable(getNetwork()).getType() instanceof ValueTypeInteger &&
                evaluator.getErrors().isEmpty();
    }

    public boolean setSideRedstonePower(Direction side, IDynamicRedstone cap) {
        int[] old_strong = this.strong_powers.clone();
        int[] old_power = this.redstone_powers.clone();
        if (cap != null) {
            this.redstone_powers[side.get3DDataValue()] = cap.getRedstoneLevel();
            if (cap.isDirect()) {
                this.strong_powers[side.get3DDataValue()] = cap.getRedstoneLevel();
            } else {
                this.strong_powers[side.get3DDataValue()] = 0;
            }
        } else {
            this.redstone_powers[side.get3DDataValue()] = 0;
            this.strong_powers[side.get3DDataValue()] = 0;
        }
        setChanged();
        return this.redstone_powers != old_power || this.strong_powers != old_strong;
    }

    public int getRedstonePowerForTarget() {
        int power = 0;
        for (int i : this.redstone_powers) {
            power = Math.max(power, i);
        }
        return power;
    }

    public int getStrongPowerForTarget() {
        int power = 0;
        for (int i : this.strong_powers) {
            power = Math.max(power, i);
        }
        return power;
    }

    @Override
    public void onDirty() {
        if (!this.level.isClientSide) {
            refreshVariables(true);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!MinecraftHelpers.isClientSideThread()) {
            this.shouldSendUpdateEvent = true;
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        unRegisterEventHandle();
    }



    public void sendRemoveRenderPacket() {
        if (!this.level.isClientSide) {
            IntegratedProxy._instance.getPacketHandler().sendToAll(new RemoveProxyRenderPacket(DimPos.of(this.level, this.worldPosition)));
        }
    }

    public void unRegisterEventHandle() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    public static void updateTileEntity(Level level, BlockPos pos, BlockState blockState, TileAccessProxy tile) {
        if (tile.shouldSendUpdateEvent && tile.getNetwork() != null) {
            tile.shouldSendUpdateEvent = false;
            tile.refreshVariables(true);
        }
        if (!level.isClientSide) {
            boolean is_dirty = false;
            try {
                IValue value = tile.evaluator_display.getVariable(tile.getNetwork()).getValue();
                if (value != tile.getDisplayValue()) {
                    is_dirty = true;
                }
                tile.setDisplayValue(value);
            } catch (EvaluationException | NullPointerException e) {
                if (tile.display_value != null) {
                    is_dirty = true;
                }
                tile.setDisplayValue(null);
            }
            if (is_dirty) {
                tile.setChanged();
                IntegratedProxy._instance.getPacketHandler().sendToAll(new UpdateProxyDisplayValuePacket(DimPos.of(tile.level, tile.worldPosition), tile.getDisplayValue()));
            }

            tile.updateTarget();
        }
    }

    @Override
    public void afterNetworkReAlive() {
        refreshVariables(true);
        updateTarget();
    }

    @Override
    public boolean hasEventSubscriptions() {
        return true;
    }

    @Override
    public Set<Class<? extends INetworkEvent>> getSubscribedEvents() {
        Set<Class<? extends INetworkEvent>> set = new HashSet<>();
        set.add(VariableContentsUpdatedEvent.class);
        set.add(NetworkElementAddEvent.class);
        set.add(NetworkElementRemoveEvent.class);
        return set;
    }

    @Override
    public void onEvent(INetworkEvent event, AccessProxyNetworkElement networkElement) {
        refreshVariables(false);
        updateTarget();
        sendUpdate();
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (this.isRemoved()) {
            unRegisterEventHandle();
            return;
        }
        try {
            if (!event.getPlayer().level.isClientSide) {
                IntegratedProxy._instance.getPacketHandler().sendToPlayer(new UpdateProxyRenderPacket(DimPos.of(this.level, this.worldPosition), this.target), (ServerPlayer) event.getPlayer());
                IntegratedProxy._instance.getPacketHandler().sendToPlayer(new UpdateProxyDisplayValuePacket(DimPos.of(this.level, this.worldPosition), getDisplayValue()), (ServerPlayer) event.getPlayer());
                IntegratedProxy._instance.getPacketHandler().sendToPlayer(new UpdateProxyDisplayRotationPacket(DimPos.of(this.level, this.worldPosition), this.display_rotations), (ServerPlayer) event.getPlayer());
                IntegratedProxy._instance.getPacketHandler().sendToPlayer(new UpdateProxyDisableRenderPacket(DimPos.of(this.level, this.worldPosition), this.disable_render), (ServerPlayer) event.getPlayer());
            }
        } catch (NullPointerException e) {
            IntegratedProxy.clog(org.apache.logging.log4j.Level.ERROR, "Failed to sync proxy render data with client!");
        }
    }

    public static void updateAfterBlockDestroy(Level world, BlockPos pos) {
        for (Direction offset : Direction.values()) {
            world.neighborChanged(pos.offset(offset.getNormal()), IPRegistryEntries.BLOCK_ACCESS_PROXY, pos);
        }
//        refreshFacePartNetwork(world, pos);
    }

    private void notifyTargetChange() {
        try {
            if (this.isRemoved()) {
                unRegisterEventHandle();
                return;
            }
            for (Direction offset : Direction.values()) {
                this.level.neighborChanged(this.worldPosition.offset(offset.getNormal()), getBlockState().getBlock(), this.worldPosition);
            }
            refreshFacePartNetwork();
        } catch (NullPointerException e) {
            IntegratedProxy.clog(org.apache.logging.log4j.Level.WARN, "NullPointerException at onTargetChanged!   " + this.target.toString() + "   " + this.level.toString());
        }
    }

    public void refreshFacePartNetwork() { //refresh the network of parts on the 6 face of access proxy block
        refreshFacePartNetwork(this.level, this.worldPosition);
    }

    public static void refreshFacePartNetwork(Level world, BlockPos pos) { //refresh the network of parts on the 6 face of access proxy block
        for (Direction offset : Direction.values()) {
            try {
                INetwork network = NetworkHelpers.getNetwork(world, pos.offset(offset.getNormal()), null).orElse(null);
                if (network != null && !network.isKilled()) continue;
                PartHelpers.PartStateHolder partStateHolder = PartHelpers.getPart(PartPos.of(world, pos.offset(offset.getNormal()), offset.getOpposite()));
                if (partStateHolder != null) {
                    if (ModList.get().isLoaded("integratedtunnels")) {
                        if (partStateHolder.getPart() instanceof PartTypeInterfacePositionedAddon) {
                            NetworkHelpers.initNetwork(world, pos.offset(offset.getNormal()), null);
                        }
                    }
                    if (partStateHolder.getPart() instanceof PartTypeBlockReader) {
                        NetworkHelpers.initNetwork(world, pos.offset(offset.getNormal()), null);
                    }
                }
            } catch (NullPointerException | ConcurrentModificationException e) {
                IntegratedProxy.clog(org.apache.logging.log4j.Level.WARN, "refreshFacePartNetwork failed");
                e.printStackTrace();
            }
        }
    }

    public void updateTargetBlock(Level world, BlockPos pos) {
//        world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock());
//        for (Direction side : Direction.values()) {
//            world.notifyNeighborsOfStateChange(pos.offset(side), world.getBlockState(pos).getBlock());
//        }
        if (!world.isLoaded(pos)) return;
        for (Direction facing : Direction.values()) {
            world.neighborChanged(pos, world.getBlockState(pos).getBlock(), pos.offset(facing.getNormal()));
        }
        for (Direction facing : Direction.values()) {
            if (world.getBlockState(pos.offset(facing.getNormal())).getBlock() instanceof BlockAccessProxy) continue;
            world.neighborChanged(pos.offset(facing.getNormal()), world.getBlockState(pos.offset(facing.getNormal())).getBlock(), pos);
        }
    }

    public void updateTargetBlock() {
        updateTargetBlock(this.level, this.target.getBlockPos());
    }

    public void onTargetChanged(LevelAccessor world, BlockPos pos) {
        if (this.target == null || isRemoved() || this.target.getBlockPos() == this.worldPosition) {
            return;
        }
        if (pos.equals(this.target.getBlockPos()) && world.equals(this.level)) {
            notifyTargetChange();
        }
    }

    @SubscribeEvent
    public void onNeighborNotifyEvent(BlockEvent.NeighborNotifyEvent event) {
        if (this.target != null && !isRemoved()) {
            if (event.getPos().equals(this.target.getBlockPos()) && event.getWorld().equals(this.level)) {
                onTargetChanged(event.getWorld(), event.getPos());
            }
            for (Direction facing : event.getNotifiedSides()) {
                BlockPos offset_pos = event.getPos().offset(facing.getNormal());
                if (offset_pos.equals(this.target.getBlockPos())) {
                    onTargetChanged(event.getWorld(), offset_pos);
                }
            }
        }
    }

    @SubscribeEvent
    public void onCropGrowEvent(BlockEvent.CropGrowEvent.Post event) {
        if (!event.getWorld().isClientSide()) {
            onTargetChanged(event.getWorld(), event.getPos());
        }
    }

    @SubscribeEvent
    public void onServerStop(ServerStoppingEvent event) {
        unRegisterEventHandle();
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("tile.blocks.integrated_proxy.access_proxy.name");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerAccessProxy(id, playerInventory, this.getInventory(), Optional.of(this));
    }
}
