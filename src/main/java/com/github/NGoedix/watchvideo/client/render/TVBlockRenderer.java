package com.github.NGoedix.watchvideo.client.render;

import com.github.NGoedix.watchvideo.block.custom.TVBlock;
import com.github.NGoedix.watchvideo.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.watchvideo.util.displayers.IDisplay;
import com.github.NGoedix.watchvideo.util.math.*;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TVBlockRenderer implements BlockEntityRenderer<TVBlockEntity> {

    private static BufferedImage blackTextureBuffer = null;
    private static ImageRenderer blackTexture = null;

    public TVBlockRenderer(BlockEntityRendererProvider.Context dispatcher) {
        if (blackTextureBuffer == null) {
            blackTextureBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            blackTextureBuffer.setRGB(0, 0, Color.BLACK.getRGB());
            blackTexture = ImageAPI.renderer(blackTextureBuffer);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(TVBlockEntity frame) {
        return frame.getSizeX() > 16 || frame.getSizeY() > 16;
    }

    @Override
    public boolean shouldRender(TVBlockEntity frame, @NotNull Vec3 vec) {
        return Vec3.atCenterOf(frame.getBlockPos()).closerThan(vec, 128);
    }

    @Override
    public void render(TVBlockEntity frame, float pPartialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        if (frame.isURLEmpty()) {
            if (frame.display != null) frame.display.release();
            return;
        }

        IDisplay display = frame.requestDisplay();
        if (display == null) {
            if (!frame.isPlaying()) return;
            renderTexture(frame, null, ImageAPI.loadingGif().texture((int) (Minecraft.getInstance().level.getGameTime()), 1, true), pose, true);
            return;
        }

        int texture = display.prepare(frame.getUrl(), frame.getVolume() * Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER), frame.minDistance, frame.maxDistance, frame.isPlaying(), frame.isLoop(), frame.getTick());

        if (texture == -1) {
            return;
        }

        renderTexture(frame, display, blackTexture.texture(1, 1, false), pose, false);
        renderTexture(frame, display, texture, pose, true);
    }

    private void renderTexture(TVBlockEntity frame, IDisplay display, int texture, PoseStack pose, boolean aspectRatio) {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        RenderSystem.bindTexture(texture);
        RenderSystem.setShaderTexture(0, texture);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        Direction d = frame.getBlockState().getValue(TVBlock.FACING);
        if (d == Direction.NORTH) {
            d = Direction.SOUTH;
        } else if (d == Direction.SOUTH) {
            d = Direction.NORTH;
        }

        Facing facing = Facing.get(d);
        AlignedBox box = frame.getBox();

        // BEGIN ASPECT RATIO
        if (aspectRatio) {
            float videoAspectRatio = 1.0f;
            if (display != null) {
                Dimension dimensions = display.getDimensions();
                if (dimensions != null) {
                    // Adjust based on dimensions
                    videoAspectRatio = (float) (dimensions.getWidth() / (float) dimensions.getHeight());
                }
            }

            float height = box.maxY - box.minY;
            float width = 0F;
            switch (facing) {
                case WEST, EAST -> width = box.maxZ - box.minZ;
                case NORTH, SOUTH -> width = box.maxX - box.minX;
            }

            float screenAspectRatio = width / height;
            float w = height * videoAspectRatio;
            float h = width / videoAspectRatio;

            if(videoAspectRatio > screenAspectRatio) {
                box.setMax(Axis.Y, h);
                pose.translate(0, (height - h) / 2F, 0);
            } else {
                box.setMax(facing.axis == Axis.Z ? Axis.X : Axis.Z, w);
                pose.translate(facing.axis == Axis.Z ? (width - w) / 2F : 0, 0, facing.axis == Axis.Z ? 0 : (width - w) / 2F);
            }

            if (facing == Facing.SOUTH) {
                box.setMax(Axis.X, box.maxX - 0.02F);
            }

            // Calculate the difference between height and width
            float difference = height - width;

            // If the height is greater than width, adjust both dimensions.
            if (difference > 0) {
                // Adjust the dimensions of the box
                box.grow(Axis.Y, -difference/2); // Shrink the height
                if (facing.axis == Axis.Z) {
                    box.grow(Axis.X, difference/2); // Grow the width if facing axis is Z
                } else {
                    box.grow(Axis.Z, difference/2); // Grow the width if facing axis is not Z
                }
            }
        }
        // END ASPECT RATIO

        float offset = aspectRatio ? 0.001f : 0;

        // Incorporate widthFactor to adjust the box based on the texture's dimensions
        if (d == Direction.WEST || d == Direction.EAST) {
            box.grow(facing.axis, 0.99F + offset);
        } else {
            box.grow(facing.axis, -0.95F + offset);
        }
        BoxFace face = BoxFace.get(facing);

        pose.pushPose();

        if (d == Direction.NORTH) {
            pose.translate(-0.185, 0, 0);
        }

        if (d == Direction.SOUTH) {
            pose.translate(-0.185, 0, 0);
        }

        if (d == Direction.WEST) {
            pose.translate(0, 0, -0.185);
        }

        if (d == Direction.EAST) {
            pose.translate(0, 0, -0.185);
        }

        pose.translate(0.5, 0.5646, 0.5);
        pose.mulPose(facing.rotation().rotation((float) Math.toRadians(0)));
        pose.translate(-0.5, -0.5, -0.5);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f mat = pose.last().pose();
        Matrix3f mat3f = pose.last().normal();
        Vec3i normal = face.facing.normal;
        for (BoxCorner corner : face.corners)
            builder.vertex(mat, box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacing(face.getTexU()) ? 1 : 0, corner.isFacing(face.getTexV()) ? 1 : 0).color(-1)
                    .normal(mat3f, normal.getX(), normal.getY(), normal.getZ()).endVertex();
        tesselator.end();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        pose.popPose();

        // Reset OpenGL state
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }
}