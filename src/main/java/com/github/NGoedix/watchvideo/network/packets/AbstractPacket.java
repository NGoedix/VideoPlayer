package com.github.NGoedix.watchvideo.network.packets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;


public abstract class AbstractPacket<T extends AbstractPacket<T>> {

    public abstract void handlePacket(NetworkEvent.Context context);
    protected void handleServerSide(ServerPlayerEntity player) {}

    public abstract void write(PacketBuffer buf);
    public abstract void read(PacketBuffer buf);
}
