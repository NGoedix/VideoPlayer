package com.github.NGoedix.watchvideo.network.message;

import com.github.NGoedix.watchvideo.client.ClientHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenVideoManagerScreen implements IMessage<OpenVideoManagerScreen> {

    private BlockPos blockPos;
    private String url;
    private int tick;
    private int volume;
    private boolean loop;

    public OpenVideoManagerScreen() {}

    public OpenVideoManagerScreen(BlockPos blockPos, String url, int tick, int volume, boolean loop) {
        this.blockPos = blockPos;
        this.url = url;
        this.tick = tick;
        this.volume = volume;
        this.loop = loop;
    }

    @Override
    public void encode(OpenVideoManagerScreen message, PacketBuffer buffer) {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeUtf(message.url);
        buffer.writeInt(message.tick);
        buffer.writeInt(message.volume);
        buffer.writeBoolean(message.loop);
    }

    @Override
    public OpenVideoManagerScreen decode(PacketBuffer buffer) {
        return new OpenVideoManagerScreen(buffer.readBlockPos(), buffer.readUtf(), buffer.readInt(), buffer.readInt(), buffer.readBoolean());
    }

    @Override
    public void handle(OpenVideoManagerScreen message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            ClientHandler.openVideoGUI(message.blockPos, message.url, message.tick, message.volume, message.loop);
        });
    }
}
