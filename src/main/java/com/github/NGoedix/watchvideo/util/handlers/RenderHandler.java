package com.github.NGoedix.watchvideo.util.handlers;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Reference;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = com.github.NGoedix.watchvideo.Reference.MOD_ID)
public class RenderHandler {

    @SubscribeEvent
    public static void registerEntityRenders(ModelRegistryEvent event) {

    }
}
