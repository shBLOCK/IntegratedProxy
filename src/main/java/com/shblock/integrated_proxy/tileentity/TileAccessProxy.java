package com.shblock.integrated_proxy.tileentity;

import com.shblock.integrated_proxy.IntegratedProxy;
import com.shblock.integrated_proxy.id_network.AccessProxyNetworkElement;
import com.shblock.integrated_proxy.network.packet.RemoveProxyRenderPacket;
import com.shblock.integrated_proxy.network.packet.UpdateProxyDisplayRotationPacket;
import com.shblock.integrated_proxy.network.packet.UpdateProxyDisplayValuePacket;
import com.shblock.integrated_proxy.network.packet.UpdateProxyRenderPacket;
import com.typesafe.config.ConfigException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
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
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderConfig;
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectableInventory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TileAccessProxy extends TileCableConnectableInventory implements IDirtyMarkListener, INetworkEventListener<AccessProxyNetworkElement> {

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

    public TileAccessProxy() {
        super(4, "variables", 1);
        this.inventory.addDirtyMarkListener(this);

        this.addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderSingleton() {
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new AccessProxyNetworkElement(DimPos.of(world, blockPos));
            }
        });
        this.variableContainer = new VariableContainerDefault();
        this.addCapabilityInternal(VariableContainerConfig.CAPABILITY, this.variableContainer);
        this.evaluator_x = new InventoryVariableEvaluator<>(this, SLOT_X, ValueTypes.INTEGER);
        this.evaluator_y = new InventoryVariableEvaluator<>(this, SLOT_Y, ValueTypes.INTEGER);
        this.evaluator_z = new InventoryVariableEvaluator<>(this, SLOT_Z, ValueTypes.INTEGER);
        this.evaluator_display = new InventoryVariableEvaluator<>(this, SLOT_DISPLAY, ValueTypes.CATEGORY_ANY);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return super.isItemValidForSlot(index, stack) && (stack.isEmpty() || stack.hasCapability(VariableFacadeHolderConfig.CAPABILITY, (EnumFacing)null));
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, EnumFacing side) {
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemStack, EnumFacing side) {
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setInteger("pos_mode", this.pos_mode);
        NBTClassType.writeNbt(List.class, "errors_x", evaluator_x.getErrors(), tag);
        NBTClassType.writeNbt(List.class, "errors_y", evaluator_y.getErrors(), tag);
        NBTClassType.writeNbt(List.class, "errors_z", evaluator_z.getErrors(), tag);
        NBTClassType.writeNbt(List.class, "errors_display", evaluator_display.getErrors(), tag);
        tag.setIntArray("display_rotations", this.display_rotations);

        if (getDisplayValue() != null) {
//            tag.setString("displayValueType", value.getType().getTranslationKey());
//            tag.setString("displayValue", ValueHelpers.serializeRaw(value));
            tag.setTag("displayValue", ValueHelpers.serialize(getDisplayValue()));
        }

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.pos_mode = tag.getInteger("pos_mode");
        this.evaluator_x.setErrors(NBTClassType.readNbt(List.class, "errors_x", tag));
        this.evaluator_y.setErrors(NBTClassType.readNbt(List.class, "errors_y", tag));
        this.evaluator_z.setErrors(NBTClassType.readNbt(List.class, "errors_z", tag));
        this.evaluator_display.setErrors(NBTClassType.readNbt(List.class, "errors_display", tag));
        this.display_rotations = tag.getIntArray("display_rotations");
//        if (tag.hasKey("displayValueType", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal()) && tag.hasKey("displayValue", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal())) {
//            IValueType valueType = ValueTypes.REGISTRY.getValueType(tag.getString("displayValueType"));
//            if (valueType != null) {
//                String serializedValue = tag.getString("displayValue");
//                L10NHelpers.UnlocalizedString deserializationError = valueType.canDeserialize(serializedValue);
//                if (deserializationError == null) {
//                    this.setDisplayValue(ValueHelpers.deserializeRaw(valueType, serializedValue));
//                }
//            }
//        } else {
//            this.setDisplayValue(null);
//        }
        if (tag.hasKey("displayValue", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal())) {
            setDisplayValue(ValueHelpers.deserialize(tag.getCompoundTag("displayValue")));
        } else {
            setDisplayValue(null);
        }

        this.shouldSendUpdateEvent = true;
    }

    public IValue getDisplayValue() {
        return this.display_value;
    }

    public void setDisplayValue(IValue displayValue) {
        this.display_value = displayValue;
    }

    public void rotateDisplayValue(EnumFacing side) {
        int ord = side.ordinal();
        this.display_rotations[ord] ++;
        if (this.display_rotations[ord] >= 4) {
            this.display_rotations[ord] = 0;
        }
        markDirty();
        IntegratedProxy._instance.getPacketHandler().sendToAll(new UpdateProxyDisplayRotationPacket(DimPos.of(this.world, this.pos), this.display_rotations));
    }

    protected void refreshVariables(boolean sendVariablesUpdateEvent) {
        this.variableContainer.refreshVariables(this.getNetwork(), this.inventory, sendVariablesUpdateEvent);
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
            DimPos old_target = this.target == null ? null : DimPos.of(this.target.getDimensionId(), this.target.getBlockPos());
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
        if (evaluator.getVariable(getNetwork()) == null) {
            return false;
        }
        return evaluator.hasVariable() && evaluator.getErrors().isEmpty();
    }

    public boolean variableOk(InventoryVariableEvaluator<IValue> evaluator) {
        if (evaluator.getVariable(getNetwork()) == null) {
            return false;
        }
        return evaluator.hasVariable() &&
                evaluator.getErrors().isEmpty();
    }

    public boolean variableIntegerOk(InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator) {
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
        if (!MinecraftHelpers.isClientSide()) {
            this.shouldSendUpdateEvent = true;
            MinecraftForge.EVENT_BUS.register(this);
        }
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
        return set;
    }

    @Override
    public void onEvent(INetworkEvent event, AccessProxyNetworkElement networkElement) {
        if (event instanceof VariableContentsUpdatedEvent) {
            this.refreshVariables(false);
            updateTarget();
            sendUpdate();
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.player.world.isRemote) {
            IntegratedProxy._instance.getPacketHandler().sendToPlayer(new UpdateProxyRenderPacket(DimPos.of(this.world, this.pos), this.target), (EntityPlayerMP) event.player);
            IntegratedProxy._instance.getPacketHandler().sendToPlayer(new UpdateProxyDisplayValuePacket(DimPos.of(this.world, this.pos), getDisplayValue()), (EntityPlayerMP) event.player);
            IntegratedProxy._instance.getPacketHandler().sendToPlayer(new UpdateProxyDisplayRotationPacket(DimPos.of(this.world, this.pos), this.display_rotations), (EntityPlayerMP) event.player);
        }
    }

//    @SubscribeEvent
//    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
//        if (event.player.world.isRemote) {
//            MinecraftForge.EVENT_BUS.unregister(this);
//        }
//    }

    private void notifyTargetChange() {
        for (EnumFacing offset : EnumFacing.VALUES) {
            this.world.neighborChanged(this.pos.offset(offset), getBlockType(), this.pos);
        }
    }

    @SubscribeEvent
    public void onTargetChanged(BlockEvent.NeighborNotifyEvent event) {
        if (event.getPos().equals(this.target.getBlockPos()) && event.getWorld().equals(this.target.getWorld())) {
            notifyTargetChange();
        }
    }
    //TODO:rs_writer, light_panel

}
