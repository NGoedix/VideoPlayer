package com.github.NGoedix.watchvideo.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public abstract class AbstractPacket<T extends AbstractPacket<T>> {

    public abstract void handlePacket(NetworkEvent.Context context);
    protected void handleServerSide(ServerPlayer player) {}

    public abstract void write(FriendlyByteBuf buf);
    public abstract void read(FriendlyByteBuf buf);
}
