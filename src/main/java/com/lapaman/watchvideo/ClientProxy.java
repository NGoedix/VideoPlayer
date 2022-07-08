package com.lapaman.watchvideo;

import com.lapaman.watchvideo.client.gui.VideoContainer;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void postInit() {
        super.postInit();
    }

    public static void openGUI() {
        Minecraft.getMinecraft().displayGuiScreen(new VideoContainer());
    }

    public static void closeGUI() {
        Minecraft.getMinecraft().displayGuiScreen(null);
    }
}
