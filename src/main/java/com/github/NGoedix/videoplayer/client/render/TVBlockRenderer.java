package com.github.NGoedix.videoplayer.client.render;

import com.github.NGoedix.videoplayer.block.custom.TVBlock;
import com.github.NGoedix.videoplayer.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.videoplayer.util.displayers.IDisplay;
import com.github.NGoedix.videoplayer.util.math.AlignedBox;
import com.github.NGoedix.videoplayer.util.math.BoxCorner;
import com.github.NGoedix.videoplayer.util.math.BoxFace;
import com.github.NGoedix.videoplayer.util.math.Facing;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.*;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class TVBlockRenderer implements BlockEntityRenderer<TVBlockEntity> {

    public TVBlockRenderer(BlockEntityRendererFactory.Context dispatcher) {}

    @Override
    public void render(TVBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.isURLEmpty()) {
            if (entity.display != null) entity.display.release();
            return;
        }

        IDisplay display = entity.requestDisplay();
        if (display == null) return;

        display.prepare(entity.getUrl(), entity.volume * MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.MASTER), entity.minDistance, entity.maxDistance, entity.isPlaying(), entity.loop, entity.getTick());

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        int texture = display.texture();

        if (texture == -1) return;
        RenderSystem.bindTexture(texture);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        Direction d = entity.getCachedState().get(TVBlock.FACING);
        if (d == Direction.NORTH) {
            d = Direction.SOUTH;
        } else if (d == Direction.SOUTH) {
            d = Direction.NORTH;
        }

        Facing facing = Facing.get(d);
        AlignedBox box = entity.getBox();

        if (d == Direction.WEST || d == Direction.EAST) {
            box.grow(facing.axis, 0.99F);
        } else {
            box.grow(facing.axis, -0.95F);
        }
        BoxFace face = BoxFace.get(facing);

        matrices.push();

        if (d == Direction.NORTH) {
            matrices.translate(-0.185, 0, 0);
        }

        if (d == Direction.SOUTH) {
            matrices.translate(-0.185, 0, 0);
        }

        if (d == Direction.WEST) {
            matrices.translate(0, 0, -0.185);
        }

        if (d == Direction.EAST) {
            matrices.translate(0, 0, -0.185);
        }

        matrices.translate(0.5, 0.5646, 0.5);
        matrices.multiply(facing.rotation().rotationDegrees((float) Math.toRadians(0)));
        matrices.translate(-0.5, -0.5, -0.5);

        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalProgram);
        Tessellator tesselator = Tessellator.getInstance();
        BufferBuilder builder = tesselator.getBuffer();
        builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_NORMAL);
        Matrix4f mat = matrices.peek().getPositionMatrix();
        Matrix3f mat3f = matrices.peek().getNormalMatrix();
        Vec3i normal = face.facing.normal;
        for (BoxCorner corner : face.corners)
            builder.vertex(mat, box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .texture(corner.isFacing(face.getTexU()) ? 1 : 0, corner.isFacing(face.getTexV()) ? 1 : 0).color(-1)
                    .normal(mat3f, normal.getX(), normal.getY(), normal.getZ()).next();
        tesselator.draw();

        matrices.pop();

        // Reset OpenGL state
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }

    @Override
    public boolean rendersOutsideBoundingBox(TVBlockEntity blockEntity) {
        return blockEntity.getSizeX() > 16 || blockEntity.getSizeY() > 16;
    }

    @Override
    public boolean isInRenderDistance(TVBlockEntity blockEntity, Vec3d pos) {
        return Vec3d.ofCenter(blockEntity.getPos()).isInRange(pos, 128);
    }
}