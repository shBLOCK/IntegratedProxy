package com.shblock.integrated_proxy.helper;

import net.minecraft.util.math.BlockPos;

public class BlockPosHelper {
    public static int[] blockPosToSet(BlockPos pos) {
        return new int[]{pos.getX(), pos.getY(), pos.getZ()};
    }

    public static BlockPos setToBlockPos(int[] set) {
        return new BlockPos(set[0], set[1], set[2]);
    }
}
