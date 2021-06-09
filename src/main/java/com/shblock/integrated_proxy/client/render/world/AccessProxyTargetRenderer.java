package com.shblock.integrated_proxy.client.render.world;

import com.shblock.integrated_proxy.client.data.AccessProxyClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.client.render.valuetype.ValueTypeWorldRenderers;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class AccessProxyTargetRenderer {
    @SubscribeEvent
    public static void onRender(RenderWorldLastEvent event) {
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();

        EntityPlayer player = Minecraft.getMinecraft().player;
        float partialTicks = event.getPartialTicks();
        double offsetX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
        double offsetY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
        double offsetZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;

        for (Map.Entry<DimPos, DimPos> entry : AccessProxyClientData.getInstance().getTargetMap().entrySet()) {
            DimPos target = entry.getValue();

            Vec3d target_vec = new Vec3d(
                    target.getBlockPos().getX(),
                    target.getBlockPos().getY(),
                    target.getBlockPos().getZ()
            );
            GlStateManager.glLineWidth((float) (8.0d / player.getPositionVector().distanceTo(target_vec)));

            if (target.getDimensionId() == Minecraft.getMinecraft().world.provider.getDimension()) {
                GlStateManager.pushMatrix();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                GlStateManager.translate(-offsetX + target.getBlockPos().getX(), -offsetY + target.getBlockPos().getY(), -offsetZ + target.getBlockPos().getZ());
                RenderGlobal.drawBoundingBox(bufferbuilder, -0.01D, -0.01D, -0.01D, 1.01D, 1.01D, 1.01D, 0.17F, 0.8F, 0.69F, 0.8F);
                tessellator.draw();
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.enableTexture2D();

        GlStateManager.enableRescaleNormal();
        for (Map.Entry<DimPos, DimPos> entry : AccessProxyClientData.getInstance().getTargetMap().entrySet()) {
            DimPos proxy = entry.getKey();
            DimPos target = entry.getValue();
            IValue value = AccessProxyClientData.getInstance().getVariable(proxy);
            int[] rotation = AccessProxyClientData.getInstance().getRotation(proxy);
            if (rotation == null) {
                rotation = new int[]{0, 0, 0, 0, 0, 0};
            }

            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (value != null) {
                for (EnumFacing facing : EnumFacing.values()) {
                    GlStateManager.pushMatrix();

                    float scale = 0.08F;
                    GlStateManager.translate(-offsetX + target.getBlockPos().getX(), -offsetY + target.getBlockPos().getY(), -offsetZ + target.getBlockPos().getZ());
                    translateToFacing(facing);
                    GlStateManager.scale(scale, scale, scale);
                    rotateSide(facing, rotation);
                    GlStateManager.rotate(180, 0, 0, 1);
                    rotateToFacing(facing);

                    IValueTypeWorldRenderer renderer = ValueTypeWorldRenderers.REGISTRY.getRenderer(value.getType());
                    if (renderer == null) {
                        renderer = ValueTypeWorldRenderers.DEFAULT;
                    }
                    renderer.renderValue(
                            null,
                            -offsetX + target.getBlockPos().getX(),
                            -offsetY + target.getBlockPos().getY(),
                            -offsetZ + target.getBlockPos().getZ(),
                            partialTicks,
                            0,
                            facing,
                            null,
                            value,
                            TileEntityRendererDispatcher.instance,
                            1
                    );

                    GlStateManager.popMatrix();
                }
            }
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static void translateToFacing(EnumFacing facing) {
        switch (facing) {
            case DOWN:
                GlStateManager.translate(1, -0.01, 0);
                break;
            case UP:
                GlStateManager.translate(1, 1.01, 1);
                break;
            case NORTH:
                GlStateManager.translate(0, 1, 1.01);
                break;
            case SOUTH:
                GlStateManager.translate(1, 1, -0.01);
                break;
            case EAST:
                GlStateManager.translate(1.01, 1, 1);
                break;
            case WEST:
                GlStateManager.translate(-0.01, 1, 0);
                break;
        }
    }

    private static void rotateToFacing(EnumFacing facing) {
        short rotationY = 0;
        short rotationX = 0;
        if (facing == EnumFacing.SOUTH) {
            rotationY = 0;
        } else if (facing == EnumFacing.NORTH) {
            rotationY = 180;
        } else if (facing == EnumFacing.EAST) {
            rotationY = 90;
        } else if (facing == EnumFacing.WEST) {
            rotationY = -90;
        } else if (facing == EnumFacing.UP) {
            rotationX = -90;
        } else if (facing == EnumFacing.DOWN) {
            rotationX = 90;
        }
        GlStateManager.rotate(rotationY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(rotationX, 1.0F, 0.0F, 0.0F);
    }

    private static void rotateSide(EnumFacing side, int[] rotation) {
        switch (side) {
            case UP:
                GlStateManager.translate(-6.25, 0, -6.25);
                GlStateManager.rotate(rotation[1] * 90, 0, 1, 0);
                GlStateManager.translate(6.25, 0, 6.25);
                break;
            case DOWN:
                GlStateManager.translate(-6.25, 0, 6.25);
                GlStateManager.rotate(rotation[0] * 90, 0, 1, 0);
                GlStateManager.translate(6.25, 0, -6.25);
                break;
            case NORTH:
                GlStateManager.translate(6.25, -6.25, 0);
                GlStateManager.rotate(rotation[3] * 90, 0, 0, 1);
                GlStateManager.translate(-6.25, 6.25, 0);
                break;
            case SOUTH:
                GlStateManager.translate(-6.25, -6.25, 0);
                GlStateManager.rotate(rotation[2] * 90, 0, 0, 1);
                GlStateManager.translate(6.25, 6.25, 0);
                break;
            case WEST:
                GlStateManager.translate(0, -6.25, 6.25);
                GlStateManager.rotate(rotation[4] * 90, 1, 0, 0);
                GlStateManager.translate(0, 6.25, -6.25);
                break;
            case EAST:
                GlStateManager.translate(0, -6.25, -6.25);
                GlStateManager.rotate(rotation[5] * 90, 1, 0, 0);
                GlStateManager.translate(0, 6.25, 6.25);
                break;
        }
    }
}
