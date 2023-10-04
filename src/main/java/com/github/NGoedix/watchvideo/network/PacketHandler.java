package com.github.NGoedix.watchvideo.network;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.network.messages.FrameVideoMessage;
import com.github.NGoedix.watchvideo.network.messages.OpenVideoManagerScreen;
import com.github.NGoedix.watchvideo.network.messages.SendVideoMessage;
import com.github.NGoedix.watchvideo.network.messages.UploadVideoUpdateMessage;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    private static int packetId = 0;

    public static SimpleNetworkWrapper INSTANCE = null;

    public PacketHandler() {
    }

    public static int nextID() {
        return packetId++;
    }

    public static void registerMessages() {
        if (PacketHandler.INSTANCE != null)
            return;

        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);
        register();
    }

    private static void register() {
        INSTANCE.registerMessage(OpenVideoManagerScreen.Handler.class, OpenVideoManagerScreen.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(SendVideoMessage.Handler.class, SendVideoMessage.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(FrameVideoMessage.Handler.class, FrameVideoMessage.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(UploadVideoUpdateMessage.Handler.class, UploadVideoUpdateMessage.class, nextID(), Side.SERVER);
    }
}
