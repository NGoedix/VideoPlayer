package com.github.NGoedix.watchvideo.client.render;

import com.github.NGoedix.watchvideo.block.custom.TVBlock;
import com.github.NGoedix.watchvideo.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.watchvideo.util.displayers.DisplayerApi;
import com.github.NGoedix.watchvideo.util.math.AlignedBox;
import com.github.NGoedix.watchvideo.util.math.BoxCorner;
import com.github.NGoedix.watchvideo.util.math.BoxFace;
import com.github.NGoedix.watchvideo.util.math.Facing;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

public class TVBlockRenderer implements BlockEntityRenderer<TVBlockEntity> {

    public TVBlockRenderer(BlockEntityRendererProvider.Context dispatcher) {}

    @Override
    public boolean shouldRenderOffScreen(TVBlockEntity frame) {
        return frame.getSizeX() > 16 || frame.getSizeY() > 16;
    }

//    @Override
//    public boolean shouldRenderOffScreen(TVBlockEntity pBlockEntity) {
//        return true;
//    }

//    @Override
//    public int getViewDistance() {
//        return 256;
//    }

    @Override
    public boolean shouldRender(TVBlockEntity frame, Vec3 vec) {
        return Vec3.atCenterOf(frame.getBlockPos()).closerThan(vec, 128);
    }

    @Override
    public void render(TVBlockEntity frame, float pPartialTick, PoseStack pose, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        if (frame.isURLEmpty()) {
            if (frame.display != null) frame.display.release();
            return;
        }

        DisplayerApi display = frame.requestDisplay();
        if (display == null) return;

        display.prepare(frame.getUrl(), frame.volume * Minecraft.getInstance().options
                .getSoundSourceVolume(SoundSource.MASTER), frame.minDistance, frame.maxDistance, frame.isPlaying(), frame.loop, frame.getTick());

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        int texture = display.texture();

        if (texture == -1) return;
        RenderSystem.bindTexture(texture);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        Direction d = frame.getBlockState().getValue(TVBlock.FACING);
        if (d == Direction.NORTH) {
            d = Direction.SOUTH;
        } else if (d == Direction.SOUTH) {
            d = Direction.NORTH;
        }

        Facing facing = Facing.get(d);
        AlignedBox box = frame.getBox();

        if (d == Direction.WEST || d == Direction.EAST) {
            box.grow(facing.axis, 0.99F);
        } else {
            box.grow(facing.axis, -0.95F);
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

        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        Matrix4f mat = pose.last().pose();
        Matrix3f mat3f = pose.last().normal();
        Vec3i normal = face.facing.normal;
        for (BoxCorner corner : face.corners)
            builder.vertex(mat, box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacing(face.getTexU()) ? 1 : 0, corner.isFacing(face.getTexV()) ? 1 : 0).color(-1)
                    .normal(mat3f, normal.getX(), normal.getY(), normal.getZ()).endVertex();
        tesselator.end();

        pose.popPose();

        // Reset OpenGL state
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }
}