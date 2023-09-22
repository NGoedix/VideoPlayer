package com.github.NGoedix.watchvideo.network.message;

import com.github.NGoedix.watchvideo.client.ClientHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class FrameVideoMessage implements IMessage<FrameVideoMessage> {

    private BlockPos pos;
    private boolean playing;
    private int tick;

    public FrameVideoMessage() {}

    public FrameVideoMessage(BlockPos pos, boolean playing, int tick) {
        this.pos = pos;
        this.playing = playing;
        this.tick = tick;
    }

    @Override
    public void encode(FrameVideoMessage message, PacketBuffer buffer) {
        buffer.writeBlockPos(message.pos);
        buffer.writeBoolean(message.playing);
        buffer.writeInt(message.tick);
    }

    @Override
    public FrameVideoMessage decode(PacketBuffer buffer) {
        return new FrameVideoMessage(buffer.readBlockPos(), buffer.readBoolean(), buffer.readInt());
    }

    @Override
    public void handle(FrameVideoMessage message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> ClientHandler.manageVideo(message.pos, message.playing, message.tick));
        supplier.get().setPacketHandled(true);
    }
}
