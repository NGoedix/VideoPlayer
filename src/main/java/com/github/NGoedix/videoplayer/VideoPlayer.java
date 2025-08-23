package com.github.NGoedix.videoplayer;

import com.github.NGoedix.videoplayer.block.ModBlocks;
import com.github.NGoedix.videoplayer.block.entity.ModBlockEntities;
import com.github.NGoedix.videoplayer.commands.*;
import com.github.NGoedix.videoplayer.commands.arguments.SymbolStringArgumentSerializer;
import com.github.NGoedix.videoplayer.commands.arguments.SymbolStringArgumentType;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class VideoPlayer implements ModInitializer {

    public static final CreativeModeTab VIDEO_PLAYER_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "tab"), FabricItemGroup.builder().title(Component.translatable("itemGroup.videoplayer.items"))
            .icon(() -> new ItemStack(ModBlocks.TV_BLOCK)).displayItems((displayContext, entries) -> {
                entries.accept(new ItemStack(ModBlocks.TV_BLOCK));
                entries.accept(new ItemStack(ModBlocks.RADIO_BLOCK));
            }).build());

    @Override
    public void onInitialize() {
        Reference.LOGGER.info("Initializing VideoPlayer");

        ModBlocks.registerModBlocks();
        ModBlockEntities.registerAllBlockEntities();
        ArgumentTypeRegistry.registerArgumentType(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "symbol_string"), SymbolStringArgumentType.class, new SymbolStringArgumentSerializer());

        PacketHandler.registerPackets();
        PacketHandler.registerC2SPackets();

        CommandRegistrationCallback.EVENT.register(PlayVideoCommand::register);
        CommandRegistrationCallback.EVENT.register(PlayCustomVideoCommand::register);
        CommandRegistrationCallback.EVENT.register(StopVideoCommand::register);
        CommandRegistrationCallback.EVENT.register(PlayMusicCommand::register);
        CommandRegistrationCallback.EVENT.register(StopMusicCommand::register);
    }
}
