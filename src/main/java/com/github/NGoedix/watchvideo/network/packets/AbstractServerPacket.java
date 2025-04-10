package com.github.NGoedix.watchvideo.network.packets;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public abstract class AbstractServerPacket<T extends AbstractServerPacket<T>> extends AbstractPacket<T> {

    @Override
    public final void handlePacket(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) {
                return; // No sender, don't handle the message
            }
            handleServerSide(sender);
        });
    }

    protected abstract void handleServerSide(ServerPlayer player);
}
