package com.shblock.integratedproxy.id_network;

import com.shblock.integratedproxy.IntegratedProxy;
import com.shblock.integratedproxy.tileentity.TileAccessProxy;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.IEventListenableNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.TileNetworkElement;

import javax.annotation.Nullable;
import java.util.Optional;

public class AccessProxyNetworkElement extends TileNetworkElement<TileAccessProxy> implements IEventListenableNetworkElement<TileAccessProxy> {

    public  AccessProxyNetworkElement(DimPos pos) {
        super(pos);
    }

    @Override
    public boolean onNetworkAddition(INetwork network) {
        if (super.onNetworkAddition(network)) {
            IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network).orElse(null);
            if (partNetwork == null) {
                return false;
            }
            if (!partNetwork.addVariableContainer(getPos())) {
                IntegratedProxy.clog(Level.WARN, "The access proxy is already exist: " + getPos());
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onNetworkRemoval(INetwork network) {
        super.onNetworkRemoval(network);
        IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network).orElse(null);
        if (partNetwork != null) {
            partNetwork.removeVariableContainer(getPos());
        }
    }

    @Nullable
    @Override
    public Optional<TileAccessProxy> getNetworkEventListener() {
        return getTile();
    }

    @Override
    protected Class<TileAccessProxy> getTileClass() {
        return TileAccessProxy.class;
    }

    @Override
    public void setPriorityAndChannel(INetwork network, int priority, int channel) {

    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getChannel() {
        return IPositionedAddonsNetwork.DEFAULT_CHANNEL;
    }
}
