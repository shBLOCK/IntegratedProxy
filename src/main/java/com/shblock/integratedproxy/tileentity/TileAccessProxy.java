package com.shblock.integratedproxy.tileentity;

import com.shblock.integratedproxy.IPRegistryEntries;
import com.shblock.integratedproxy.IntegratedProxy;
import com.shblock.integratedproxy.block.BlockAccessProxy;
import com.shblock.integratedproxy.block.BlockAccessProxyConfig;
import com.shblock.integratedproxy.id_network.AccessProxyNetworkElement;
import com.shblock.integratedproxy.inventory.container.ContainerAccessProxy;
import com.shblock.integratedproxy.network.packet.*;
import com.shblock.integratedproxy.storage.AccessProxyCollection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkEventListener;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerConfig;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerDefault;
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderConfig;
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.event.NetworkElementAddEvent;
import org.cyclops.integrateddynamics.core.network.event.NetworkElementRemoveEvent;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectableInventory;
import org.cyclops.integrateddynamics.item.ItemVariable;

import javax.annotation.Nullable;
import java.util.*;

public class TileAccessProxy extends TileCableConnectableInventory implements IDirtyMarkListener, INetworkEventListener<AccessProxyNetworkElement>, INamedContainerProvider {

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

    public TileAccessProxy() {
        super(IPRegistryEntries.TILE_ACCESS_PROXY,4, 1);
        getInventory().addDirtyMarkListener(this);

        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, LazyOptional.of(() -> new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
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
            public boolean isItemValidForSlot(int i, ItemStack stack) {
                return super.isItemValidForSlot(i, stack) && stack.getItem() instanceof ItemVariable;
            }

            @Override
            public boolean canInsertItem(int index, ItemStack itemStackIn, @Nullable Direction direction) {
                return false;
            }

            @Override
            public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
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
    public CompoundNBT writeToItemStack(CompoundNBT tag) {
        return new CompoundNBT();
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag = super.write(tag);
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
    public void read(CompoundNBT tag) {
        super.read(tag);
        this.pos_mode = tag.getInt("pos_mode");
        this.evaluator_x.setErrors(NBTClassType.readNbt(List.class, "errors_x", tag));
        this.evaluator_y.setErrors(NBTClassType.readNbt(List.class, "errors_y", tag));
        this.evaluator_z.setErrors(NBTClassType.readNbt(List.class, "errors_z", tag));
        this.evaluator_display.setErrors(NBTClassType.readNbt(List.class, "errors_display", tag));
        this.display_rotations = tag.getIntArray("display_rotations");
        if (tag.contains("displayValue", Constants.NBT.TAG_COMPOUND)) {
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
        markDirty();
        IntegratedProxy._instance.getPacketHandler().sendToAll(new UpdateProxyDisplayRotationPacket(DimPos.of(this.world, this.pos), this.display_rotations));
    }

    public void changeDisableRender() {
        this.disable_render = !this.disable_render;
        markDirty();
        IntegratedProxy._instance.getPacketHandler().sendToAll(new UpdateProxyDisableRenderPacket(DimPos.of(this.world, this.pos), this.disable_render));
    }

    protected void refreshVariables(boolean sendVariablesUpdateEvent) {
        this.variableContainer.refreshVariables(this.getNetwork(), getInventory(), sendVariablesUpdateEvent);
        this.evaluator_x.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
        this.evaluator_y.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
        this.evaluator_z.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
        this.evaluator_display.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
    }

    public int getVariableIntValue(InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator) throws EvaluationException {
        return evaluator.getVariable(getNetwork()).getValue().cast(ValueTypes.INTEGER).getRawValue();
    }

    private boolean isTargetOutOfRange(BlockPos target) {
        if (BlockAccessProxyConfig.range < 0) {
            return false;
        }
        return Math.abs(target.getX() - this.pos.getX()) > BlockAccessProxyConfig.range ||
                Math.abs(target.getY() - this.pos.getY()) > BlockAccessProxyConfig.range ||
                Math.abs(target.getZ() - this.pos.getZ()) > BlockAccessProxyConfig.range;
    }

    private void updateTarget() {
        if (!this.world.isRemote) {
            DimPos old_target = this.target == null ? null : DimPos.of(this.target.getWorldKey(), this.target.getBlockPos());
            try {
                if (this.pos_mode == 1) {
                    this.target = DimPos.of(
                            this.world,
                            new BlockPos(
                                    isVariableAvailable(this.evaluator_x) ? getVariableIntValue(this.evaluator_x) : this.pos.getX(),
                                    isVariableAvailable(this.evaluator_y) ? getVariableIntValue(this.evaluator_y) : this.pos.getY(),
                                    isVariableAvailable(this.evaluator_z) ? getVariableIntValue(this.evaluator_z) : this.pos.getZ()
                            )
                    );
                } else {
                    this.target = DimPos.of(
                            this.world,
                            new BlockPos(
                                    isVariableAvailable(this.evaluator_x) ? getVariableIntValue(this.evaluator_x) + this.pos.getX() : this.pos.getX(),
                                    isVariableAvailable(this.evaluator_y) ? getVariableIntValue(this.evaluator_y) + this.pos.getY() : this.pos.getY(),
                                    isVariableAvailable(this.evaluator_z) ? getVariableIntValue(this.evaluator_z) + this.pos.getZ() : this.pos.getZ()
                            )
                    );
                }
            } catch (EvaluationException e) {
                this.target = DimPos.of(this.world, this.pos);
            }

            if (isTargetOutOfRange(this.target.getBlockPos())) {
                this.target = DimPos.of(this.world, this.pos);
            }

            if (!this.target.equals(old_target)) {
                notifyTargetChange();
                IntegratedProxy._instance.getPacketHandler().sendToAll(new UpdateProxyRenderPacket(DimPos.of(this.world, this.pos), this.target));
                AccessProxyCollection.getInstance(this.world).set(this.pos, this.target.getBlockPos());
                updateTargetBlock();
                if (old_target != null) {
                    updateTargetBlock(this.world, old_target.getBlockPos());
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
            this.redstone_powers[side.getIndex()] = cap.getRedstoneLevel();
            if (cap.isStrong()) {
                this.strong_powers[side.getIndex()] = cap.getRedstoneLevel();
            } else {
                this.strong_powers[side.getIndex()] = 0;
            }
        } else {
            this.redstone_powers[side.getIndex()] = 0;
            this.strong_powers[side.getIndex()] = 0;
        }
        markDirty();
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
        if (!this.world.isRemote) {
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
        if (!this.world.isRemote) {
            IntegratedProxy._instance.getPacketHandler().sendToAll(new RemoveProxyRenderPacket(DimPos.of(this.world, this.pos)));
        }
    }

    public void unRegisterEventHandle() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (this.shouldSendUpdateEvent && this.getNetwork() != null) {
            this.shouldSendUpdateEvent = false;
            this.refreshVariables(true);
        }
        if (!world.isRemote) {
            boolean is_dirty = false;
            try {
                IValue value = this.evaluator_display.getVariable(getNetwork()).getValue();
                if (value != getDisplayValue()) {
                    is_dirty = true;
                }
                setDisplayValue(value);
            } catch (EvaluationException | NullPointerException e) {
                if (this.display_value != null) {
                    is_dirty = true;
                }
                setDisplayValue(null);
            }
            if (is_dirty) {
                markDirty();
                IntegratedProxy._instance.getPacketHandler().sendToAll(new UpdateProxyDisplayValuePacket(DimPos.of(this.world, this.pos), getDisplayValue()));
            }

            updateTarget();
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
            if (!event.getPlayer().world.isRemote) {
                IntegratedProxy._instance.getPacketHandler().sendToPlayer(new UpdateProxyRenderPacket(DimPos.of(this.world, this.pos), this.target), (ServerPlayerEntity) event.getPlayer());
                IntegratedProxy._instance.getPacketHandler().sendToPlayer(new UpdateProxyDisplayValuePacket(DimPos.of(this.world, this.pos), getDisplayValue()), (ServerPlayerEntity) event.getPlayer());
                IntegratedProxy._instance.getPacketHandler().sendToPlayer(new UpdateProxyDisplayRotationPacket(DimPos.of(this.world, this.pos), this.display_rotations), (ServerPlayerEntity) event.getPlayer());
                IntegratedProxy._instance.getPacketHandler().sendToPlayer(new UpdateProxyDisableRenderPacket(DimPos.of(this.world, this.pos), this.disable_render), (ServerPlayerEntity) event.getPlayer());
            }
        } catch (NullPointerException e) {
            IntegratedProxy.clog(Level.ERROR, "Failed to sync proxy render data with client!");
        }
    }

    public static void updateAfterBlockDestroy(World world, BlockPos pos) {
        for (Direction offset : Direction.values()) {
            world.neighborChanged(pos.offset(offset), IPRegistryEntries.BLOCK_ACCESS_PROXY, pos);
        }
        for (Direction offset : Direction.values()) {
            try {
                NetworkHelpers.initNetwork(world, pos.offset(offset), offset.getOpposite());
            } catch (NullPointerException | ConcurrentModificationException ignored) { }
        }
    }

    private void notifyTargetChange() {
        if (this.isRemoved()) {
            unRegisterEventHandle();
            return;
        }
        for (Direction offset : Direction.values()) {
            this.world.neighborChanged(this.pos.offset(offset), getBlockState().getBlock(), this.pos);
        }
        refreshFacePartNetwork();
    }

    public void refreshFacePartNetwork() { //refresh the network of parts on the 6 face of access proxy block
        for (Direction offset : Direction.values()) {
            try {
                NetworkHelpers.initNetwork(this.world, this.pos.offset(offset), offset.getOpposite());
            } catch (NullPointerException ignored) { }
        }
    }

    public void updateTargetBlock(World world, BlockPos pos) {
//        world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock());
//        for (Direction side : Direction.values()) {
//            world.notifyNeighborsOfStateChange(pos.offset(side), world.getBlockState(pos).getBlock());
//        }
        if (!world.isBlockLoaded(pos)) return;
        for (Direction facing : Direction.values()) {
            world.neighborChanged(pos, world.getBlockState(pos).getBlock(), pos.offset(facing));
        }
        for (Direction facing : Direction.values()) {
            if (world.getBlockState(pos.offset(facing)).getBlock() instanceof BlockAccessProxy) continue;
            world.neighborChanged(pos.offset(facing), world.getBlockState(pos.offset(facing)).getBlock(), pos);
        }
    }

    public void updateTargetBlock() {
        updateTargetBlock(this.world, this.target.getBlockPos());
    }

    @SubscribeEvent
    public void onTargetChanged(BlockEvent.NeighborNotifyEvent event) {
        if (this.target == null || isRemoved()) {
            return;
        }
        try {
            if (event.getPos().equals(this.target.getBlockPos()) && event.getWorld().equals(this.world)) {
                notifyTargetChange();
            }
        } catch (NullPointerException e) {
            IntegratedProxy.clog(Level.WARN, "NullPointerException at onTargetChanged!" + this.target.toString() + this.world.toString() + event.getWorld().toString());
        }
    }

    @SubscribeEvent
    public void onServerStop(FMLServerStoppingEvent event) {
        unRegisterEventHandle();
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("tile.blocks.integrated_proxy.access_proxy.name");
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerAccessProxy(id, playerInventory, this.getInventory(), Optional.of(this));
    }
    //TODO:rs_writer, light_panel
}
