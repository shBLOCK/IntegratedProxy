package com.shblock.integrated_proxy.storage;

import com.shblock.integrated_proxy.IntegratedProxy;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

public class RemoteRedstoneData extends WorldSavedData {
    private static final String NAME = IntegratedProxy.MODID + "_remote_redstone";

    public RemoteRedstoneData(String name) {
        this();
    }

    public RemoteRedstoneData() {
        super(NAME);
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return null;
    }
}
