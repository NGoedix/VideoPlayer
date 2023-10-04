package com.github.NGoedix.watchvideo.network.messages;

import com.github.NGoedix.watchvideo.VideoPlayer;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class SendVideoMessage implements IMessage
{

	private String url;
	private int volume;
	private boolean controlBlocked;

	public SendVideoMessage() {}

	public SendVideoMessage(String url, int volume, boolean controlBlocked) {
		this.url = url;
		this.volume = volume;
		this.controlBlocked = controlBlocked;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		int l = buffer.readInt();
		this.url = String.valueOf(buffer.readCharSequence(l, StandardCharsets.UTF_8));
		this.volume = buffer.readInt();
		this.controlBlocked = buffer.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(url.length());
		buffer.writeCharSequence(url, StandardCharsets.UTF_8);
		buffer.writeInt(volume);
		buffer.writeBoolean(controlBlocked);
	}

	public static class Handler implements IMessageHandler<SendVideoMessage, IMessage> {

		@Override
		public IMessage onMessage(SendVideoMessage message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private void handle(SendVideoMessage message, MessageContext ctx)
		{
			VideoPlayer.proxy.openVideo(message.url, message.volume, message.controlBlocked);
		}
	}
}