package com.shblock.integratedproxy.storage;

import com.shblock.integratedproxy.IntegratedProxy;
import com.shblock.integratedproxy.helper.BlockPosHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AccessProxyCollection extends WorldSavedData {
    public static final String NAME = IntegratedProxy.MODID + "_access_proxy_collection";
    private HashMap<BlockPos, BlockPos> map = new HashMap<>();

    public AccessProxyCollection(String name) {
        this();
    }

    public AccessProxyCollection() {
        super(NAME);
    }

    public static AccessProxyCollection getInstance(World world) {
        DimensionSavedDataManager storage = ((ServerWorld) world).getSavedData();
        return storage.getOrCreate(AccessProxyCollection::new, NAME);
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
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (Map.Entry<BlockPos, BlockPos> entry : this.map.entrySet()) {
            list.add(new IntArrayNBT(BlockPosHelper.blockPosToSet(entry.getKey())));
            list.add(new IntArrayNBT(BlockPosHelper.blockPosToSet(entry.getValue())));
        }
        compound.put("map", list);
        return compound;
    }

    @Override
    public void read(CompoundNBT nbt) {
        this.map = new HashMap<>();
        ListNBT list = nbt.getList("map", Constants.NBT.TAG_INT_ARRAY);
        for (int i = 0;i < list.size() / 2;i++) {
            this.map.put(BlockPosHelper.setToBlockPos(list.getIntArray(i)), BlockPosHelper.setToBlockPos(list.getIntArray(i + 1)));
        }
    }
}
