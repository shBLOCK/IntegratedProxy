package com.shblock.integrated_proxy.storage;

import com.shblock.integrated_proxy.IntegratedProxy;
import com.shblock.integrated_proxy.helper.BlockPosHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AccessProxyCollection extends WorldSavedData {
    public static final String NAME = IntegratedProxy.MODID + "_access_proxy_collection";
    private HashMap<BlockPos, BlockPos> map = new HashMap<>();

//    public AccessProxyCollection(String name) {
//        this();
//    }

    public AccessProxyCollection() {
        super(NAME);
    }

    public static AccessProxyCollection getInstance(World world) {
        MapStorage storage = world.getPerWorldStorage();
        AccessProxyCollection instance = (AccessProxyCollection) storage.getOrLoadData(AccessProxyCollection.class, NAME);
        if (instance == null) {
            instance = new AccessProxyCollection();
            storage.setData(NAME, instance);
        }
        return instance;
    }

    public void set(BlockPos proxy, BlockPos target) {
        this.map.put(proxy, target);
        markDirty();
    }

    public void remove(BlockPos proxy) {
        this.map.remove(proxy);
        markDirty();
    }

    //get a list of access proxies that's pointing to the target pos
    //TODO:make this more efficient
    public HashSet<BlockPos> getProxiesFromTarget(BlockPos target) {
        HashSet<BlockPos> set = new HashSet<>();
        for (Map.Entry<BlockPos, BlockPos> entry : this.map.entrySet()) {
            if (entry.getValue().equals(target)) {
                set.add(entry.getKey());
            }
        }
        return set;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<BlockPos, BlockPos> entry : this.map.entrySet()) {
            list.appendTag(new NBTTagIntArray(BlockPosHelper.blockPosToSet(entry.getKey())));
            list.appendTag(new NBTTagIntArray(BlockPosHelper.blockPosToSet(entry.getValue())));
        }
        compound.setTag("map", list);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.map = new HashMap<>();
        NBTTagList list = nbt.getTagList("map", Constants.NBT.TAG_INT_ARRAY);
        for (int i = 0;i < list.tagCount() / 2;i++) {
            this.map.put(BlockPosHelper.setToBlockPos(list.getIntArrayAt(i)), BlockPosHelper.setToBlockPos(list.getIntArrayAt(i + 1)));
        }
    }
}
