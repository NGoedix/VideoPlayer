package com.lapaman.watchvideo.network;

import com.lapaman.watchvideo.Reference;
import com.lapaman.watchvideo.network.message.MessageVideo;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);

    public static void init() {
        INSTANCE.registerMessage(MessageVideo.class, MessageVideo.class, 0, Side.CLIENT);
    }
}
