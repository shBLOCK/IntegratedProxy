package com.shblock.integratedproxy.tileentity;

import com.shblock.integratedproxy.IPRegistryEntries;
import com.shblock.integratedproxy.IntegratedProxy;
import com.shblock.integratedproxy.id_network.AccessProxyNetworkElement;
import com.shblock.integratedproxy.inventory.container.ContainerAccessProxy;
import com.shblock.integratedproxy.network.packet.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
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
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
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
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.network.event.NetworkElementAddEvent;
import org.cyclops.integrateddynamics.core.network.event.NetworkElementRemoveEvent;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectableInventory;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    public boolean disable_render = false;

    public TileAccessProxy() {
        super(IPRegistryEntries.TILE_ACCESS_PROXY,4, 1);
        this.getInventory().addDirtyMarkListener(this);

        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, LazyOptional.of(() -> new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new AccessProxyNetworkElement(DimPos.of(world, blockPos));
            }
        }));
        this.variableContainer = new VariableContainerDefault();
        addCapabilityInternal(VariableContainerConfig.CAPABILITY, LazyOptional.of(() -> variableContainer));
        this.evaluator_x = new InventoryVariableEvaluator<>(getInventory(), SLOT_X, ValueTypes.INTEGER);
        this.evaluator_y = new InventoryVariableEvaluator<>(getInventory(), SLOT_Y, ValueTypes.INTEGER);
        this.evaluator_z = new InventoryVariableEvaluator<>(getInventory(), SLOT_Z, ValueTypes.INTEGER);
        this.evaluator_display = new InventoryVariableEvaluator<>(getInventory(), SLOT_DISPLAY, ValueTypes.CATEGORY_ANY);
    }



//    @Override
//    public boolean isItemValidForSlot(int index, ItemStack stack) {
//        return super.isItemValidForSlot(index, stack) && (stack.isEmpty() || stack.hasCapability(VariableFacadeHolderConfig.CAPABILITY, (EnumFacing)null));
//    }



//    @Override
//    public boolean canInsertItem(int slot, ItemStack itemStack, EnumFacing side) {
//        return false;
//    }
//
//    @Override
//    public boolean canExtractItem(int slot, ItemStack itemStack, EnumFacing side) {
//        return false;
//    }


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

    private void updateTarget() {
        if (!getWorld().isRemote) {
            DimPos old_target = this.target == null ? null : DimPos.of(this.target.getWorld(false), this.target.getBlockPos());
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

            if (!this.world.isRemote) {
                IntegratedProxy._instance.getPacketHandler().sendToAll(new UpdateProxyRenderPacket(DimPos.of(this.world, this.pos), this.target));
            }

            if (!this.target.equals(old_target)) {
                notifyTargetChange();
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
        if (!event.getPlayer().world.isRemote) {
            IntegratedProxy._instance.getPacketHandler().sendToPlayer(new UpdateProxyRenderPacket(DimPos.of(this.world, this.pos), this.target), (ServerPlayerEntity) event.getPlayer());
            IntegratedProxy._instance.getPacketHandler().sendToPlayer(new UpdateProxyDisplayValuePacket(DimPos.of(this.world, this.pos), getDisplayValue()), (ServerPlayerEntity) event.getPlayer());
            IntegratedProxy._instance.getPacketHandler().sendToPlayer(new UpdateProxyDisplayRotationPacket(DimPos.of(this.world, this.pos), this.display_rotations), (ServerPlayerEntity) event.getPlayer());
            IntegratedProxy._instance.getPacketHandler().sendToPlayer(new UpdateProxyDisableRenderPacket(DimPos.of(this.world, this.pos), this.disable_render), (ServerPlayerEntity) event.getPlayer());
        }
    }

//    @SubscribeEvent
//    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
//        if (event.player.world.isRemote) {
//            MinecraftForge.EVENT_BUS.unregister(this);
//        }
//    }

    private void notifyTargetChange() {
        if (this.isRemoved()) {
            unRegisterEventHandle();
            return;
        }
        for (Direction offset : Direction.values()) {
            this.world.neighborChanged(this.pos.offset(offset), getBlockState().getBlock(), this.pos);
        }
    }

    @SubscribeEvent
    public void onTargetChanged(BlockEvent.NeighborNotifyEvent event) {
        if (event.getPos().equals(this.target.getBlockPos()) && event.getWorld().equals(this.target.getWorld(false))) {
            notifyTargetChange();
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
