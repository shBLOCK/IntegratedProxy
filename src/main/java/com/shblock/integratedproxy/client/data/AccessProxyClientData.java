package com.shblock.integratedproxy.client.data;

import net.minecraft.client.Minecraft;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

import java.util.HashMap;

public class AccessProxyClientData {
    private static final AccessProxyClientData _instance = new AccessProxyClientData();

    private final HashMap<DimPos, DimPos> target_map = new HashMap<>();
    private final HashMap<DimPos, IValue> variable_map = new HashMap<>();
    private final HashMap<DimPos, int[]> rotation_map = new HashMap<>();
    private final HashMap<DimPos, Boolean> disable_map = new HashMap<>();

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

    public void putDisable(DimPos proxy, boolean disable) {
        this.disable_map.put(proxy, disable);
    }

    public void remove(DimPos proxy) {
        this.target_map.remove(proxy);
        this.variable_map.remove(proxy);
        this.rotation_map.remove(proxy);
        this.disable_map.remove(proxy);
    }

    public HashMap<DimPos, DimPos> getTargetMap() {
        return this.target_map;
    }

    public DimPos getTarget(DimPos dimPos) {
        return this.target_map.get(dimPos);
    }

    public DimPos getTarget(BlockPos pos, RegistryKey<World> dim) {
        return this.target_map.get(DimPos.of(dim, pos));
    }

    public IValue getVariable(DimPos dimPos) {
        return this.variable_map.get(dimPos);
    }

    public IValue getVariable(BlockPos pos, RegistryKey<World> dim) {
        return this.variable_map.get(DimPos.of(dim, pos));
    }

    public int[] getRotation(DimPos dimPos) {
        return this.rotation_map.get(dimPos);
    }

    public int[] getRotation(BlockPos pos, RegistryKey<World> dim) {
        return this.rotation_map.get(DimPos.of(dim, pos));
    }

    public boolean getDisable(DimPos dimPos) {
        return this.disable_map.getOrDefault(dimPos, false);
    }

    public boolean getDisable(BlockPos pos, RegistryKey<World> dim) {
        return this.disable_map.getOrDefault(DimPos.of(dim, pos), false);
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer().equals(Minecraft.getInstance().player) && event.getPlayer().world.isRemote) {
            this.target_map.clear();
            this.variable_map.clear();
            this.rotation_map.clear();
            this.disable_map.clear();
        }
    }
}
