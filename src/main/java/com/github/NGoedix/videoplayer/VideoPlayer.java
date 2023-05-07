package com.github.NGoedix.videoplayer;

import com.github.NGoedix.videoplayer.commands.PlayVideoCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class VideoPlayer implements ModInitializer {
    @Override
    public void onInitialize() {
        Constants.LOGGER.info("Initializing VideoPlayer");
        CommandRegistrationCallback.EVENT.register(PlayVideoCommand::register);
    }
}
