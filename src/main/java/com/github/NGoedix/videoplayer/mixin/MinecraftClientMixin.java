package com.github.NGoedix.videoplayer.mixin;

import com.github.NGoedix.videoplayer.util.cache.TextureCache;
import com.github.NGoedix.videoplayer.util.displayers.VideoDisplayer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void onDisconnect(CallbackInfo ci) {
        for (TextureCache cache : TextureCache.CACHE.values()) {
            cache.remove();
        }
        TextureCache.CACHE.clear();
        VideoDisplayer.unload();
    }
}
