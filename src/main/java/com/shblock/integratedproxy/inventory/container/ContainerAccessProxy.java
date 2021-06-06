package com.shblock.integratedproxy.inventory.container;

import com.shblock.integratedproxy.IPRegistryEntries;
import com.shblock.integratedproxy.helper.DimPosHelper;
import com.shblock.integratedproxy.tileentity.TileAccessProxy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;

import java.util.List;
import java.util.Optional;

public class ContainerAccessProxy extends InventoryContainer {
    private final Optional<TileAccessProxy> tileSupplier;

    public final int lastPosModeValueId;
    public final int lastTilePosNBTId;
    public final int lastXOkId;
    public final int lastYOkId;
    public final int lastZOkId;
    public final int lastDisplayOkId;
    public final int lastXErrorId;
    public final int lastYErrorId;
    public final int lastZErrorId;
    public final int lastDisplayErrorId;

    public ContainerAccessProxy(int id, PlayerInventory playerInventory) {
        this(id, playerInventory, new Inventory(4), Optional.empty());
    }

    public ContainerAccessProxy(int id, PlayerInventory playerInventory, IInventory inventory, Optional<TileAccessProxy> tileSupplier) {
        super(IPRegistryEntries.CONTAINER_ACCESS_PROXY, id, playerInventory, inventory);
        this.tileSupplier = tileSupplier;
        for (int i = 0;i < 4;i++) {
            addSlot(new SlotVariable(inventory, i, offsetX + 27 + i * 36, offsetY + 52));
        }
        addPlayerInventory(playerInventory, offsetX + 9, offsetY + 88);

        lastPosModeValueId = getNextValueId();
        lastTilePosNBTId = getNextValueId();
        lastXOkId = getNextValueId();
        lastYOkId = getNextValueId();
        lastZOkId = getNextValueId();
        lastDisplayOkId = getNextValueId();
        lastXErrorId = getNextValueId();
        lastYErrorId = getNextValueId();
        lastZErrorId = getNextValueId();
        lastDisplayErrorId = getNextValueId();
    }

//    public TileAccessProxy getTile() {
//        if (tileSupplier.isPresent()) {
//            return tileSupplier.get();
//        }
//
//        return (TileAccessProxy) getTilePos().getWorld(false).getTileEntity(getTilePos().getBlockPos());
//    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        tileSupplier.ifPresent(tile -> {
            ValueNotifierHelpers.setValue(this, lastTilePosNBTId, DimPosHelper.toNBT(DimPos.of(tile.getWorld(), tile.getPos())));
            ValueNotifierHelpers.setValue(this, lastXOkId, tile.variableIntegerOk(tile.evaluator_x) ? 1 : 0);
            ValueNotifierHelpers.setValue(this, lastYOkId, tile.variableIntegerOk(tile.evaluator_y) ? 1 : 0);
            ValueNotifierHelpers.setValue(this, lastZOkId, tile.variableIntegerOk(tile.evaluator_z) ? 1 : 0);
            ValueNotifierHelpers.setValue(this, lastDisplayOkId, tile.variableOk(tile.evaluator_display) ? 1 : 0);
            ValueNotifierHelpers.setValue(this, lastXErrorId, tile.evaluator_x.getErrors());
            ValueNotifierHelpers.setValue(this, lastYErrorId, tile.evaluator_y.getErrors());
            ValueNotifierHelpers.setValue(this, lastZErrorId, tile.evaluator_z.getErrors());
            ValueNotifierHelpers.setValue(this, lastDisplayErrorId, tile.evaluator_display.getErrors());
        });
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    public DimPos getTilePos() {
        return DimPosHelper.fromNBT((CompoundNBT) ValueNotifierHelpers.getValueNbt(this, lastTilePosNBTId));
    }

    public boolean variableOk(int valueId) {
        return ValueNotifierHelpers.getValueInt(this, valueId) == 1;
    }

    @Override
    protected void initializeValues() {
        tileSupplier.ifPresent(tile -> {
            ValueNotifierHelpers.setValue(this, lastPosModeValueId, tile.pos_mode);
            ValueNotifierHelpers.setValue(this, lastTilePosNBTId, DimPosHelper.toNBT(DimPos.of(tile.getWorld(), tile.getPos())));
            ValueNotifierHelpers.setValue(this, lastXOkId, tile.variableIntegerOk(tile.evaluator_x) ? 1 : 0);
            ValueNotifierHelpers.setValue(this, lastYOkId, tile.variableIntegerOk(tile.evaluator_y) ? 1 : 0);
            ValueNotifierHelpers.setValue(this, lastZOkId, tile.variableIntegerOk(tile.evaluator_z) ? 1 : 0);
            ValueNotifierHelpers.setValue(this, lastDisplayOkId, tile.variableOk(tile.evaluator_display) ? 1 : 0);
            ValueNotifierHelpers.setValue(this, lastXErrorId, tile.evaluator_x.getErrors());
            ValueNotifierHelpers.setValue(this, lastYErrorId, tile.evaluator_y.getErrors());
            ValueNotifierHelpers.setValue(this, lastZErrorId, tile.evaluator_z.getErrors());
            ValueNotifierHelpers.setValue(this, lastDisplayErrorId, tile.evaluator_display.getErrors());
        });
    }

    public int getLastPosModeValue() {
        return ValueNotifierHelpers.getValueInt(this, lastPosModeValueId);
    }

    public List<IFormattableTextComponent> getErrors(int id) {
        return ValueNotifierHelpers.getValueTextComponentList(this, id);
    }

    @Override
    public void onUpdate(int valueId, CompoundNBT value) {
        super.onUpdate(valueId, value);
        tileSupplier.ifPresent(tile -> {
            if (!tile.getWorld().isRemote) {
                if (valueId == lastPosModeValueId) {
                    tile.pos_mode = getLastPosModeValue();
                }
            }
        });
    }

//    @Override
//    public Slot createNewSlot(IInventory inventory, int index, int row, int column) {
//        if(inventory instanceof PlayerInventory) {
//            return super.createNewSlot(inventory, index, row, column);
//        }
//        return new SlotVariable(inventory, index, row, column);
//    }

    @Override
    protected int getSizeInventory() {
        return 4;
    }
}
