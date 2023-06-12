package com.github.NGoedix.videoplayer;

import com.github.NGoedix.videoplayer.block.ModBlocks;
import com.github.NGoedix.videoplayer.block.entity.ModBlockEntities;
import com.github.NGoedix.videoplayer.commands.PlayVideoCommand;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class VideoPlayer implements ModInitializer {

    public static final ItemGroup VIDEO_PLAYER_TAB = FabricItemGroup.builder(
            new Identifier(Constants.MOD_ID, "video_player_tab")
    ).displayName(Text.translatable("itemGroup.videoplayer.video_player_tab"))
            .icon(() -> new ItemStack(ModBlocks.TV_BLOCK)).entries((displayContext, entries, operator) -> {
                entries.add(new ItemStack(ModBlocks.TV_BLOCK));
            }).build();

    @Override
    public void onInitialize() {
        Constants.LOGGER.info("Initializing VideoPlayer");

        ModBlocks.registerModBlocks();
        ModBlockEntities.registerAllBlockEntities();

        PacketHandler.registerC2SPackets();
        CommandRegistrationCallback.EVENT.register(PlayVideoCommand::register);
    }
}
