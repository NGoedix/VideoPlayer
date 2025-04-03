package com.github.NGoedix.watchvideo.network.packets;

import com.github.NGoedix.watchvideo.network.PacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;


public abstract class AbstractClientPacket<T extends AbstractClientPacket<T>> extends AbstractPacket<T> {

    @Override
    public final void handlePacket(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isServer()) {
                ServerPlayerEntity sender = context.getSender();
                handleServerSide(sender);
                if (sender != null) {
                    PacketHandler.sendToClient(this, sender.level, sender.blockPosition());
                }
            } else {
                handleClientSide();
            }
        });
        context.setPacketHandled(true);
    }

    protected abstract void handleClientSide();
}
