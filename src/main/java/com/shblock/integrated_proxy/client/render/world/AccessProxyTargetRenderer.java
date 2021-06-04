package com.shblock.integrated_proxy.client.render.world;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
    private static final AccessProxyTargetRenderer _instance = new AccessProxyTargetRenderer();

    private final HashMap<DimPos, DimPos> map = new HashMap<>();
    private final HashMap<DimPos, IValue> variable_map = new HashMap<>();
    private final HashMap<DimPos, int[]> rotation_map = new HashMap<>();

    public static AccessProxyTargetRenderer getInstance() {
        return _instance;
    }

    public void put(DimPos proxy, DimPos target) {
        this.map.put(proxy, target);
    }

    public void putVariable(DimPos proxy, IValue value) {
        this.variable_map.put(proxy, value);
    }

    public void putRotation(DimPos proxy, int[] value) {
        this.rotation_map.put(proxy, value);
    }

    public void remove(DimPos proxy) {
        this.map.remove(proxy);
        this.variable_map.remove(proxy);
        this.rotation_map.remove(proxy);
    }

    public DimPos get(BlockPos pos, int dim) {
        return this.map.get(DimPos.of(dim, pos));
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer().equals(Minecraft.getInstance().player) && event.getPlayer().world.isRemote) {
            this.map.clear();
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        GlStateManager.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA.param,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.param,
                GlStateManager.SourceFactor.ONE.param,
                GlStateManager.DestFactor.ZERO.param
        );
        GlStateManager.disableTexture();

        PlayerEntity player = Minecraft.getInstance().player;
        float partialTicks = event.getPartialTicks();
        double offsetX = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * (double) partialTicks;
        double offsetY = player.lastTickPosY + (player.getPosY() - player.lastTickPosY) * (double) partialTicks;
        double offsetZ = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * (double) partialTicks;

        for (Map.Entry<DimPos, DimPos> entry : this.map.entrySet()) {
            DimPos target = entry.getValue();

            Vector3d target_vec = new Vector3d(
                    target.getBlockPos().getX(),
                    target.getBlockPos().getY(),
                    target.getBlockPos().getZ()
            );
            GlStateManager.lineWidth((float) (8.0d / player.getPositionVec().distanceTo(target_vec)));

            if (target.getWorld().equals(Minecraft.getInstance().world.getProviderName())) {
                GlStateManager.pushMatrix();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                GlStateManager.translated(-offsetX + target.getBlockPos().getX(), -offsetY + target.getBlockPos().getY(), -offsetZ + target.getBlockPos().getZ());
                WorldRenderer.drawBoundingBox(event.getMatrixStack(), bufferbuilder, -0.01D, -0.01D, -0.01D, 1.01D, 1.01D, 1.01D, 0.17F, 0.8F, 0.69F, 0.8F);
                tessellator.draw();
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.enableTexture();

        GlStateManager.enableRescaleNormal();
        for (Map.Entry<DimPos, DimPos> entry : this.map.entrySet()) {
            DimPos proxy = entry.getKey();
            DimPos target = entry.getValue();
            IValue value = this.variable_map.get(proxy);
            int[] rotation = this.rotation_map.get(proxy);
            if (rotation == null) {
                rotation = new int[]{0, 0, 0, 0, 0, 0};
            }

            GlStateManager.pushMatrix();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            if (value != null) {
                for (Direction facing : Direction.values()) {
                    GlStateManager.pushMatrix();

                    float scale = 0.08F;
                    GlStateManager.translated(-offsetX + target.getBlockPos().getX(), -offsetY + target.getBlockPos().getY(), -offsetZ + target.getBlockPos().getZ());
                    translateToFacing(facing);
                    GlStateManager.scaled(scale, scale, scale);
//                    GlStateManager.translate(-6.25F, 0, -6.25F);
//                    GlStateManager.rotate(rotation[facing.ordinal()] * 90, 0, 1, 0);
//                    GlStateManager.translate(6.25F, 0, 6.25F);
                    rotateSide(facing, rotation);
                    GlStateManager.rotatef(180, 0, 0, 1);
//                    GlStateManager.rotate(180, 0, 0, 1);
                    rotateToFacing(facing);
//                    rotateSide(facing, rotation);

                    IValueTypeWorldRenderer renderer = ValueTypeWorldRenderers.REGISTRY.getRenderer(value.getType());
                    if (renderer == null) {
                        renderer = ValueTypeWorldRenderers.DEFAULT;
                    }
                    renderer.renderValue(
                            null,
                            null,
                            facing,
                            null,
                            value,
                            partialTicks,
                            event.getMatrixStack(), 
                            null,
                            1,
                            1,
                            1.0F
                    );

                    GlStateManager.popMatrix();
                }
            }
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private void translateToFacing(Direction facing) {
        switch (facing) {
            case DOWN:
                GlStateManager.translatef(1, -0.01F, 0);
                break;
            case UP:
                GlStateManager.translatef(1, 1.01F, 1);
                break;
            case NORTH:
                GlStateManager.translatef(0, 1, 1.01F);
                break;
            case SOUTH:
                GlStateManager.translatef(1, 1, -0.01F);
                break;
            case EAST:
                GlStateManager.translatef(1.01F, 1, 1);
                break;
            case WEST:
                GlStateManager.translatef(-0.01F, 1, 0);
                break;
        }
    }

    private void rotateToFacing(Direction facing) {
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
        GlStateManager.rotatef(rotationY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(rotationX, 1.0F, 0.0F, 0.0F);
    }

    private void rotateSide(Direction side, int[] rotation) {
        switch (side) {
            case UP:
                GlStateManager.translatef(-6.25F, 0, -6.25F);
                GlStateManager.rotatef(rotation[1] * 90, 0, 1, 0);
                GlStateManager.translatef(6.25F, 0, 6.25F);
                break;
            case DOWN:
                GlStateManager.translatef(-6.25F, 0, 6.25F);
                GlStateManager.rotatef(rotation[0] * 90, 0, 1, 0);
                GlStateManager.translatef(6.25F, 0, -6.25F);
                break;
            case NORTH:
                GlStateManager.translatef(6.25F, -6.25F, 0);
                GlStateManager.rotatef(rotation[3] * 90, 0, 0, 1);
                GlStateManager.translatef(-6.25F, 6.25F, 0);
                break;
            case SOUTH:
                GlStateManager.translatef(-6.25F, -6.25F, 0);
                GlStateManager.rotatef(rotation[2] * 90, 0, 0, 1);
                GlStateManager.translatef(6.25F, 6.25F, 0);
                break;
            case WEST:
                GlStateManager.translatef(0, -6.25F, 6.25F);
                GlStateManager.rotatef(rotation[4] * 90, 1, 0, 0);
                GlStateManager.translatef(0, 6.25F, -6.25F);
                break;
            case EAST:
                GlStateManager.translatef(0, -6.25F, -6.25F);
                GlStateManager.rotatef(rotation[5] * 90, 1, 0, 0);
                GlStateManager.translatef(0, 6.25F, 6.25F);
                break;
        }
    }
}
