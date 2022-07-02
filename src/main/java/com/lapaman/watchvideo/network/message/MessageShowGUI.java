package com.lapaman.watchvideo.network.message;

import com.lapaman.watchvideo.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageShowGUI implements IMessage, IMessageHandler<MessageShowGUI, IMessage> {

    public MessageShowGUI() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public IMessage onMessage(MessageShowGUI message, MessageContext ctx) {
        ClientProxy.openGUI();
        return null;
    }
}
