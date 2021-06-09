package com.shblock.integrated_proxy.client.data;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

import java.util.HashMap;

public class AccessProxyClientData {
    private static final AccessProxyClientData _instance = new AccessProxyClientData();

    private final HashMap<DimPos, DimPos> target_map = new HashMap<>();
    private final HashMap<DimPos, IValue> variable_map = new HashMap<>();
    private final HashMap<DimPos, int[]> rotation_map = new HashMap<>();

    public static AccessProxyClientData getInstance() {
        return _instance;
    }

    public void putTarget(DimPos proxy, DimPos target) {
        this.target_map.put(proxy, target);
    }

    public void putVariable(DimPos proxy, IValue value) {
        this.variable_map.put(proxy, value);
    }

    public void putRotation(DimPos proxy, int[] value) {
        this.rotation_map.put(proxy, value);
    }

    public void remove(DimPos proxy) {
        this.target_map.remove(proxy);
        this.variable_map.remove(proxy);
        this.rotation_map.remove(proxy);
    }

    public HashMap<DimPos, DimPos> getTargetMap() {
        return this.target_map;
    }

    public DimPos getTarget(DimPos dimPos) {
        return this.target_map.get(dimPos);
    }

    public DimPos getTarget(BlockPos pos, int dim) {
        return this.target_map.get(DimPos.of(dim, pos));
    }

    public IValue getVariable(DimPos dimPos) {
        return this.variable_map.get(dimPos);
    }

    public IValue getVariable(BlockPos pos, int dim) {
        return this.variable_map.get(DimPos.of(dim, pos));
    }

    public int[] getRotation(DimPos dimPos) {
        return this.rotation_map.get(dimPos);
    }

    public int[] getRotation(BlockPos pos, int dim) {
        return this.rotation_map.get(DimPos.of(dim, pos));
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player.equals(Minecraft.getMinecraft().player) && event.player.world.isRemote) {
            this.target_map.clear();
            this.variable_map.clear();
            this.rotation_map.clear();
        }
    }
}
