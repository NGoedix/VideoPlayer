package com.github.NGoedix.videoplayer;

import com.github.NGoedix.videoplayer.block.ModBlocks;
import com.github.NGoedix.videoplayer.block.entity.ModBlockEntities;
import com.github.NGoedix.videoplayer.commands.PlayVideoCommand;
import com.github.NGoedix.videoplayer.commands.arguments.SymbolStringArgumentSerializer;
import com.github.NGoedix.videoplayer.commands.arguments.SymbolStringArgumentType;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.mixin.command.ArgumentTypesAccessor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class VideoPlayer implements ModInitializer {

    @Environment(EnvType.CLIENT)
    public static ImageRenderer IMG_PAUSED;

    @Environment(EnvType.CLIENT)
    public static ImageRenderer pausedImage() { return IMG_PAUSED; }

    public static final ItemGroup VIDEO_PLAYER_TAB = FabricItemGroup.builder(
            ).displayName(Text.translatable("itemGroup.videoplayer.video_player_tab"))
            .icon(() -> new ItemStack(ModBlocks.TV_BLOCK)).entries((displayContext, entries) -> {
                entries.add(new ItemStack(ModBlocks.TV_BLOCK));
            }).build();

    @Override
    public void onInitialize() {
        Constants.LOGGER.info("Initializing VideoPlayer");

        ModBlocks.registerModBlocks();
        ModBlockEntities.registerAllBlockEntities();
        ArgumentTypesAccessor.fabric_getClassMap().put(SymbolStringArgumentType.class, new SymbolStringArgumentSerializer());

        PacketHandler.registerC2SPackets();
        CommandRegistrationCallback.EVENT.register(PlayVideoCommand::register);
    }
}
