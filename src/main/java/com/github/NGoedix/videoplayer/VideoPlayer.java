package com.github.NGoedix.videoplayer;

import com.github.NGoedix.videoplayer.block.ModBlocks;
import com.github.NGoedix.videoplayer.block.entity.ModBlockEntities;
import com.github.NGoedix.videoplayer.commands.PlayVideoCommand;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class VideoPlayer implements ModInitializer {

    @Environment(EnvType.CLIENT)
    public static ImageRenderer IMG_PAUSED;

    @Environment(EnvType.CLIENT)
    public static ImageRenderer pausedImage() { return IMG_PAUSED; }

    public static final ItemGroup VIDEO_PLAYER_TAB = FabricItemGroupBuilder.build(new Identifier(Constants.MOD_ID, "video_player_tab"),
            () -> new ItemStack(ModBlocks.TV_BLOCK));

    @Override
    public void onInitialize() {
        Constants.LOGGER.info("Initializing VideoPlayer");

        ModBlocks.registerModBlocks();
        ModBlockEntities.registerAllBlockEntities();

        PacketHandler.registerC2SPackets();
        CommandRegistrationCallback.EVENT.register(PlayVideoCommand::register);
    }
}
