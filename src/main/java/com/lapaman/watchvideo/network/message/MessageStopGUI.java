package com.lapaman.watchvideo.network.message;

import com.lapaman.watchvideo.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageStopGUI implements IMessage, IMessageHandler<MessageStopGUI, IMessage> {

    public MessageStopGUI() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public IMessage onMessage(MessageStopGUI message, MessageContext ctx) {
        ClientProxy.closeGUI();
        return null;
    }
}
