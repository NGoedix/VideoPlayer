package com.github.NGoedix.watchvideo.item.custom;

import com.github.NGoedix.watchvideo.block.ModBlocks;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.message.OpenRadioManagerScreen;
import com.github.NGoedix.watchvideo.util.displayers.Display;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.image.ImageCache;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandRadioItem extends BlockItem {

    private static final Map<ItemStack, Display> displays = new ConcurrentHashMap<>();
    private static final Map<ItemStack, ImageCache> caches = new ConcurrentHashMap<>();

    public HandRadioItem(Properties pProperties) {
        super(ModBlocks.HAND_RADIO_BLOCK.get(), pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        if (!pLevel.isClientSide) {
            ItemStack stack = pPlayer.getItemInHand(pHand);
            CompoundTag tag = stack.getOrCreateTag();

            String url = tag.getString("url");
            int volume = tag.getInt("volume");
            boolean isPlaying = tag.getBoolean("isPlaying");

            PacketHandler.sendTo(new OpenRadioManagerScreen(pPlayer.getItemInHand(pHand), url, volume, isPlaying), pPlayer);
        }

        return InteractionResultHolder.success(pPlayer.getItemInHand(pHand));
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null) {
            nbt = new CompoundTag();
        }
        return nbt;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt != null) {
            stack.setTag(nbt);
        }
    }

    public static void setUrl(ItemStack stack, String url) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("url", url);
    }

    public static String getUrl(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getString("url");
    }

    public static void setVolume(ItemStack stack, int volume) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("volume", volume);
    }

    public static int getVolume(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt("volume");
    }

    public static void setIsPlaying(ItemStack stack, boolean isPlaying) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("isPlaying", isPlaying);
    }

    public static boolean getIsPlaying(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getBoolean("isPlaying");
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (level.isClientSide) {
            Display display = requestDisplay(stack);
            if (display != null) {
                display.tick(0);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static Display requestDisplay(ItemStack stack) {
        final String url = getUrl(stack);
        if (isURLEmpty(url)) return null;

        final ImageCache cache = caches.computeIfAbsent(stack, s -> ImageAPI.getCache(URI.create(url), Minecraft.getInstance()));

        if (!cache.isVideo() && (!cache.isUsed() || cache.getStatus().equals(ImageCache.Status.FAILED))) {
            return null;
        }

        // TODO: When doing this, uncomment
//        return displays.computeIfAbsent(stack, s -> VideoDisplayer.createVideoDisplay(new Vec3d(0, 0, 0), url, true));
        return null;
    }

    private static boolean isURLEmpty(String url) {
        return url == null || url.isEmpty();
    }
}