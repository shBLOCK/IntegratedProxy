package com.shblock.integratedproxy.helper;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.util.Constants;
import org.cyclops.cyclopscore.datastructure.DimPos;

public class DimPosHelper {
    public static CompoundNBT toNBT(DimPos pos) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putLong("pos", pos.getBlockPos().toLong());
        nbt.putString("dim", pos.getWorld());
        return nbt;
    }

    public static DimPos fromNBT(CompoundNBT nbt) {
        if (nbt == null) {
            return null;
        }
        if (nbt.contains("pos", Constants.NBT.TAG_LONG) && nbt.contains("dim", Constants.NBT.TAG_STRING)) {
            return DimPos.of(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(nbt.getString("dim"))), BlockPos.fromLong(nbt.getLong("pos")));
        }
        return null;
    }
}
