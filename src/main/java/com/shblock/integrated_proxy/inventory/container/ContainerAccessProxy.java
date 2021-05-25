package com.shblock.integrated_proxy.inventory.container;

import com.shblock.integrated_proxy.tileentity.TileAccessProxy;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.container.TileInventoryContainerConfigurable;
import org.cyclops.cyclopscore.inventory.slot.SlotSingleItem;
import org.cyclops.integrateddynamics.item.ItemVariable;

public class ContainerAccessProxy extends TileInventoryContainerConfigurable<TileAccessProxy> {
    public final int lastPosModeValueId;
    public final int lastXOkId;
    public final int lastYOkId;
    public final int lastZOkId;
    public final int lastDimOkId;

    public ContainerAccessProxy(InventoryPlayer inventory, TileAccessProxy tile) {
        super(inventory, tile);
        for (int i = 0;i < 4;i++) {
            addSlotToContainer(createNewSlot(tile, i, offsetX + 27 + i * 36, offsetY + 52));
        }
        addPlayerInventory(inventory, offsetX + 9, offsetY + 88);

        lastPosModeValueId = getNextValueId();
        lastXOkId = getNextValueId();
        lastYOkId = getNextValueId();
        lastZOkId = getNextValueId();
        lastDimOkId = getNextValueId();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        ValueNotifierHelpers.setValue(this, lastXOkId, getTile().variableOk(getTile().evaluator_x) ? 1 : 0);
        ValueNotifierHelpers.setValue(this, lastYOkId, getTile().variableOk(getTile().evaluator_y) ? 1 : 0);
        ValueNotifierHelpers.setValue(this, lastZOkId, getTile().variableOk(getTile().evaluator_z) ? 1 : 0);
        ValueNotifierHelpers.setValue(this, lastDimOkId, getTile().variableOk(getTile().evaluator_dim) ? 1 : 0);
    }

    public boolean variableOk(int valueId) {
        return ValueNotifierHelpers.getValueInt(this, valueId) == 1;
    }

    @Override
    protected void initializeValues() {
        ValueNotifierHelpers.setValue(this, lastPosModeValueId, getTile().pos_mode);
    }

    public int getLastPosModeValue() {
        return ValueNotifierHelpers.getValueInt(this, lastPosModeValueId);
    }

    @Override
    public void onUpdate(int valueId, NBTTagCompound value) {
        super.onUpdate(valueId, value);
        if (!getTile().getWorld().isRemote) {
            if (valueId == lastPosModeValueId) {
                getTile().pos_mode = getLastPosModeValue();
            }
        }
    }

    @Override
    public Slot createNewSlot(IInventory inventory, int index, int row, int column) {
        if(inventory instanceof InventoryPlayer) {
            return super.createNewSlot(inventory, index, row, column);
        }
        return new SlotSingleItem(inventory, index, row, column, ItemVariable.getInstance());
    }
}
