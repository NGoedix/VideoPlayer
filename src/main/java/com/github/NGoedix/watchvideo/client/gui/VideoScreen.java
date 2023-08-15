package com.github.NGoedix.watchvideo.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.lib720.caprica.vlcj.player.base.State;
import me.lib720.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import me.lib720.watermod.ThreadCore;
import me.srrapero720.watermedia.api.player.MediaPlayerBase;
import me.srrapero720.watermedia.api.player.VideoPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;
import java.util.concurrent.locks.ReentrantLock;

public class VideoScreen extends ContainerScreen<Container> {
    boolean init = false;
    boolean playing = false;
    private final MediaPlayerBase player;

    int videoWidth = 1;
    int videoHeight = 1;

    int texture = -1;
    ReentrantLock lock = new ReentrantLock();
    boolean firstUpdate = false;
    boolean requiresUpdate = false;
    IntBuffer buffer;


    public VideoScreen(String url) {
        super(new DummyContainer(), Minecraft.getInstance().player != null ? Minecraft.getInstance().player.inventory : null, new StringTextComponent(""));
        Minecraft minecraft = Minecraft.getInstance();

        player = new VideoPlayer(null, (mediaPlayer, byteBuffers, bufferFormat) -> {
            lock.lock();
            try {
                buffer.put(byteBuffers[0].asIntBuffer());
                buffer.rewind();
                requiresUpdate = true;
            } finally {
                lock.unlock();
            }
        }, (sourceWidth, sourceHeight) -> {
            lock.lock();
            try {
                videoWidth = sourceWidth;
                videoHeight = sourceHeight;
                firstUpdate = true;
                requiresUpdate = true;
                buffer = GLAllocation.createByteBuffer(sourceWidth * sourceHeight * 4).asIntBuffer();
            } finally {
                lock.unlock();
            }
            return new BufferFormat("RGBA", sourceWidth, sourceHeight, new int[]{sourceWidth * 4}, new int[]{sourceHeight});
        });
        player.setVolume(200);
        player.start(url);
    }

    @Override
    protected void renderBg(MatrixStack pMatrixStack, float pPartialTicks, int pX, int pY) {
        if (!player.isValid()) return;
        if (player.isPlaying()) { playing = true; }
        if (playing && (player.getRawPlayerState().equals(State.ENDED) || player.getRawPlayerState().equals(State.STOPPED))) {
            onClose();
            return;
        }

        prepare(); // BINDS TEXTURE TOO
        int screenWidth = Minecraft.getInstance().screen.width;
        int screenHeight = Minecraft.getInstance().screen.height;

        RenderSystem.bindTexture(texture);
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        AbstractGui.blit(pMatrixStack, 0, 0, 0.0F, 0.0F, screenWidth, screenHeight, screenWidth, screenHeight);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.disableBlend();
    }

    public void prepare() {
        lock.lock();
        try {
            if (texture == -1) texture = GlStateManager._genTexture();
            if (requiresUpdate) {
                GlStateManager._pixelStore(3314, 0);
                GlStateManager._pixelStore(3316, 0);
                GlStateManager._pixelStore(3315, 0);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
                if (firstUpdate) {
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, videoWidth, videoHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                    firstUpdate = false;
                } else GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, videoWidth, videoHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                requiresUpdate = false;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void renderLabels(MatrixStack pMatrixStack, int pX, int pY) {}

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == 256) {
            this.onClose();
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }


    @Override
    public void onClose() {
        playing = false;
        super.onClose();
        Minecraft.getInstance().getSoundManager().resume();
        ThreadCore.threadTry(() -> {
            Thread.sleep(5000);
            GlStateManager._deleteTexture(texture);
            Minecraft.getInstance().execute(player::release);
        }, null, null);
    }
}

