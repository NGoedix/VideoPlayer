package com.github.NGoedix.watchvideo.commands;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RegisterCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event){
        PlayVideoCommand.register(event.getDispatcher());
    }
}
