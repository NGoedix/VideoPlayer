package com.github.NGoedix.watchvideo.commands;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RegisterCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event){
        PlayCustomVideoCommand.register(event.getDispatcher());
        PlayVideoCommand.register(event.getDispatcher());
        PlayMusicCommand.register(event.getDispatcher());
        StopVideoCommand.register(event.getDispatcher());
        StopMusicCommand.register(event.getDispatcher());
    }
}
