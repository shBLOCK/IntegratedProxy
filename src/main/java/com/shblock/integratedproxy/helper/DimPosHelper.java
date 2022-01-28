package com.shblock.integratedproxy.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.cyclopscore.datastructure.DimPos;

public class DimPosHelper {
    public static CompoundTag toNBT(DimPos pos) {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong("pos", pos.getBlockPos().asLong());
        nbt.putString("dim", pos.getLevel());
        return nbt;
    }

    public static DimPos fromNBT(CompoundTag nbt) {
        if (nbt == null) {
            return null;
        }
        if (nbt.contains("pos", Tag.TAG_LONG) && nbt.contains("dim", Tag.TAG_STRING)) {
            return DimPos.of(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString("dim"))), BlockPos.of(nbt.getLong("pos")));
        }
        return null;
    }
}
