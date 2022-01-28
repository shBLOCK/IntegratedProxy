package com.shblock.integratedproxy.client.render.world;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.shblock.integratedproxy.client.data.AccessProxyClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.client.render.valuetype.ValueTypeWorldRenderers;

import java.util.Map;

public class AccessProxyTargetRenderer {
    @SubscribeEvent
    public static void onRender(RenderLevelLastEvent event) {
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        float partialTicks = event.getPartialTick();
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer builder = buffer.getBuffer(RenderType.LINES);
        PoseStack matrixStack = event.getPoseStack();
        matrixStack.pushPose();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        for (Map.Entry<DimPos, DimPos> entry : AccessProxyClientData.getInstance().getTargetMap().entrySet()) {
            DimPos proxy = entry.getKey();
            DimPos target = entry.getValue();

            if (target.getLevelKey().equals(Minecraft.getInstance().level.dimension()) && !AccessProxyClientData.getInstance().getDisable(proxy)) {
                AABB bb = new AABB(
                        new BlockPos(
                            target.getBlockPos().getX(),
                            target.getBlockPos().getY(),
                            target.getBlockPos().getZ()
                        )
                ).expandTowards(0.01, 0.01, 0.01).expandTowards(-0.01, -0.01, -0.01);
                LevelRenderer.renderLineBox(event.getPoseStack(), builder, bb, 0.17F, 0.8F, 0.69F, 0.8F);
            }
        }
        matrixStack.popPose();
        RenderSystem.enableTexture();
        buffer.endBatch(RenderType.LINES);

        matrixStack.pushPose();
        for (Map.Entry<DimPos, DimPos> entry : AccessProxyClientData.getInstance().getTargetMap().entrySet()) {
            DimPos proxy = entry.getKey();
            DimPos target = entry.getValue();
            IValue value = AccessProxyClientData.getInstance().getVariable(proxy);
            int[] rotation = AccessProxyClientData.getInstance().getRotation(proxy);
            if (rotation == null || rotation.length == 0) {
                rotation = new int[]{0, 0, 0, 0, 0, 0};
            }

            if (value != null) {
                for (Direction facing : Direction.values()) {
                    matrixStack.pushPose();

                    float scale = 0.08F;
                    matrixStack.translate(-projectedView.x + target.getBlockPos().getX(), -projectedView.y + target.getBlockPos().getY(), -projectedView.z + target.getBlockPos().getZ());
                    translateToFacing(matrixStack, facing);
                    matrixStack.scale(scale, scale, scale);
                    rotateSide(matrixStack, facing, rotation);
                    matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
                    rotateToFacing(matrixStack, facing);

                    IValueTypeWorldRenderer renderer = ValueTypeWorldRenderers.REGISTRY.getRenderer(value.getType());
                    if (renderer == null) {
                        renderer = ValueTypeWorldRenderers.DEFAULT;
                    }

                    Minecraft mc = Minecraft.getInstance();
                    renderer.renderValue(
                        new BlockEntityRendererProvider.Context(mc.getBlockEntityRenderDispatcher(), mc.getBlockRenderer(), mc.getEntityModels(), mc.font),
                            null,
                            facing,
                            null,
                            value,
                            partialTicks,
                            matrixStack,
                            buffer,
                            LightTexture.pack(15, 15),
                            OverlayTexture.NO_OVERLAY,
                            1.0F
                    );
                    matrixStack.popPose();
                }
            }
        }
        matrixStack.popPose();
        buffer.endBatch();
    }

    private static void translateToFacing(PoseStack matrixStack, Direction facing) {
        switch (facing) {
            case DOWN:
                matrixStack.translate(1, -0.015F, 0);
                break;
            case UP:
                matrixStack.translate(1, 1.015F, 1);
                break;
            case NORTH:
                matrixStack.translate(0, 1, 1.015F);
                break;
            case SOUTH:
                matrixStack.translate(1, 1, -0.015F);
                break;
            case EAST:
                matrixStack.translate(1.015F, 1, 1);
                break;
            case WEST:
                matrixStack.translate(-0.015F, 1, 0);
                break;
        }
    }

    private static void rotateToFacing(PoseStack matrixStack, Direction facing) {
        short rotationY = 0;
        short rotationX = 0;
        if (facing == Direction.SOUTH) {
            rotationY = 0;
        } else if (facing == Direction.NORTH) {
            rotationY = 180;
        } else if (facing == Direction.EAST) {
            rotationY = 90;
        } else if (facing == Direction.WEST) {
            rotationY = -90;
        } else if (facing == Direction.UP) {
            rotationX = -90;
        } else if (facing == Direction.DOWN) {
            rotationX = 90;
        }
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(rotationY));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(rotationX));
    }

    private static void rotateSide(PoseStack matrixStack, Direction side, int[] rotation) {
        switch (side) {
            case UP:
                matrixStack.translate(-6.25F, 0, -6.25F);
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(rotation[1] * 90));
                matrixStack.translate(6.25F, 0, 6.25F);
                break;
            case DOWN:
                matrixStack.translate(-6.25F, 0, 6.25F);
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(rotation[0] * 90));
                matrixStack.translate(6.25F, 0, -6.25F);
                break;
            case NORTH:
                matrixStack.translate(6.25F, -6.25F, 0);
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(rotation[3] * 90));
                matrixStack.translate(-6.25F, 6.25F, 0);
                break;
            case SOUTH:
                matrixStack.translate(-6.25F, -6.25F, 0);
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(rotation[2] * 90));
                matrixStack.translate(6.25F, 6.25F, 0);
                break;
            case WEST:
                matrixStack.translate(0, -6.25F, 6.25F);
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(rotation[4] * 90));
                matrixStack.translate(0, 6.25F, -6.25F);
                break;
            case EAST:
                matrixStack.translate(0, -6.25F, -6.25F);
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(rotation[5] * 90));
                matrixStack.translate(0, 6.25F, 6.25F);
                break;
        }
    }
}
