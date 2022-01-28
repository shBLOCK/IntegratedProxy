package com.shblock.integratedproxy.storage;

import com.shblock.integratedproxy.IntegratedProxy;
import com.shblock.integratedproxy.helper.BlockPosHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AccessProxyCollection extends SavedData {
    public static final String NAME = IntegratedProxy.MODID + "_access_proxy_collection";
    private HashMap<BlockPos, BlockPos> map = new HashMap<>();

    public AccessProxyCollection() {
    }

    public static AccessProxyCollection getInstance(Level world) {
        DimensionDataStorage storage = ((ServerLevel) world).getDataStorage();
        AccessProxyCollection data = storage.get(AccessProxyCollection::load, NAME);
        if (data == null) {
            data = new AccessProxyCollection();
            storage.set(NAME, data);
        }
        return data;
    }

    public void set(BlockPos proxy, BlockPos target) {
        this.map.put(proxy, target);
        setDirty();
    }

    public void remove(BlockPos proxy) {
        this.map.remove(proxy);
        setDirty();
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
    public CompoundTag save(CompoundTag compound) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, BlockPos> entry : this.map.entrySet()) {
            list.add(new IntArrayTag(BlockPosHelper.blockPosToSet(entry.getKey())));
            list.add(new IntArrayTag(BlockPosHelper.blockPosToSet(entry.getValue())));
        }
        compound.put("map", list);
        return compound;
    }

    public static AccessProxyCollection load(CompoundTag nbt) {
        AccessProxyCollection collection = new AccessProxyCollection();
        collection.map = new HashMap<>();
        ListTag list = nbt.getList("map", Tag.TAG_INT_ARRAY);
        for (int i = 0;i < list.size() / 2;i++) {
            collection.map.put(BlockPosHelper.setToBlockPos(list.getIntArray(i)), BlockPosHelper.setToBlockPos(list.getIntArray(i + 1)));
        }
        return collection;
    }
}
