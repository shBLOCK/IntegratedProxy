package com.shblock.integrated_proxy.client.render.world;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.client.render.valuetype.ValueTypeWorldRenderers;

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

    public DimPos get(DimPos dimPos) {
        return this.map.get(dimPos);
    }

    public DimPos get(BlockPos pos, World dim) {
        return this.map.get(DimPos.of(dim, pos));
    }

    public DimPos get(BlockPos pos, String key) {
        return this.map.get(DimPos.of(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(key)), pos));
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        System.out.println("onLogout");
        if (event.getPlayer().equals(Minecraft.getInstance().player) && event.getPlayer().world.isRemote) {
            this.map.clear();
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA.param,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.param,
                GlStateManager.SourceFactor.ONE.param,
                GlStateManager.DestFactor.ZERO.param
        );

        float partialTicks = event.getPartialTicks();
        Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();

        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder builder = buffer.getBuffer(RenderType.LINES);
        MatrixStack matrixStack = event.getMatrixStack();
        matrixStack.push();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        for (Map.Entry<DimPos, DimPos> entry : this.map.entrySet()) {
            DimPos target = entry.getValue();

            if (target.getWorld().equals(Minecraft.getInstance().world.getDimensionKey().getLocation().toString())) {
                AxisAlignedBB bb = new AxisAlignedBB(
                        new BlockPos(
                            target.getBlockPos().getX(),
                            target.getBlockPos().getY(),
                            target.getBlockPos().getZ()
                        )
                ).expand(0.01, 0.01, 0.01).expand(-0.01, -0.01, -0.01);
                WorldRenderer.drawBoundingBox(event.getMatrixStack(), builder, bb, 0.17F, 0.8F, 0.69F, 0.8F);
            }
        }
        matrixStack.pop();
        RenderSystem.enableTexture();
        buffer.finish(RenderType.LINES);

        matrixStack.push();
        for (Map.Entry<DimPos, DimPos> entry : this.map.entrySet()) {
            DimPos proxy = entry.getKey();
            DimPos target = entry.getValue();
            IValue value = this.variable_map.get(proxy);
            int[] rotation = this.rotation_map.get(proxy);
            if (rotation == null) {
                rotation = new int[]{0, 0, 0, 0, 0, 0};
            }

            if (value != null) {
                for (Direction facing : Direction.values()) {
                    matrixStack.push();

                    float scale = 0.08F;
                    matrixStack.translate(-projectedView.x + target.getBlockPos().getX(), -projectedView.y + target.getBlockPos().getY(), -projectedView.z + target.getBlockPos().getZ());
                    translateToFacing(matrixStack, facing);
                    matrixStack.scale(scale, scale, scale);
                    rotateSide(matrixStack, facing, rotation);
                    matrixStack.rotate(Vector3f.ZP.rotationDegrees(180));
                    rotateToFacing(matrixStack, facing);

                    IValueTypeWorldRenderer renderer = ValueTypeWorldRenderers.REGISTRY.getRenderer(value.getType());
                    if (renderer == null) {
                        renderer = ValueTypeWorldRenderers.DEFAULT;
                    }
                    renderer.renderValue(
                            TileEntityRendererDispatcher.instance,
                            null,
                            facing,
                            null,
                            value,
                            partialTicks,
                            matrixStack,
                            buffer,
                            LightTexture.packLight(15, 15),
                            OverlayTexture.NO_OVERLAY,
                            1.0F
                    );
                    matrixStack.pop();
                }
            }
        }
        matrixStack.pop();
        buffer.finish();
    }

    private void translateToFacing(MatrixStack matrixStack, Direction facing) {
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

    private void rotateToFacing(MatrixStack matrixStack, Direction facing) {
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
        matrixStack.rotate(Vector3f.YP.rotationDegrees(rotationY));
        matrixStack.rotate(Vector3f.XP.rotationDegrees(rotationX));
    }

    private void rotateSide(MatrixStack matrixStack, Direction side, int[] rotation) {
        switch (side) {
            case UP:
                matrixStack.translate(-6.25F, 0, -6.25F);
                matrixStack.rotate(Vector3f.YP.rotationDegrees(rotation[1] * 90));
                matrixStack.translate(6.25F, 0, 6.25F);
                break;
            case DOWN:
                matrixStack.translate(-6.25F, 0, 6.25F);
                matrixStack.rotate(Vector3f.YP.rotationDegrees(rotation[0] * 90));
                matrixStack.translate(6.25F, 0, -6.25F);
                break;
            case NORTH:
                matrixStack.translate(6.25F, -6.25F, 0);
                matrixStack.rotate(Vector3f.ZP.rotationDegrees(rotation[3] * 90));
                matrixStack.translate(-6.25F, 6.25F, 0);
                break;
            case SOUTH:
                matrixStack.translate(-6.25F, -6.25F, 0);
                matrixStack.rotate(Vector3f.ZP.rotationDegrees(rotation[2] * 90));
                matrixStack.translate(6.25F, 6.25F, 0);
                break;
            case WEST:
                matrixStack.translate(0, -6.25F, 6.25F);
                matrixStack.rotate(Vector3f.XP.rotationDegrees(rotation[4] * 90));
                matrixStack.translate(0, 6.25F, -6.25F);
                break;
            case EAST:
                matrixStack.translate(0, -6.25F, -6.25F);
                matrixStack.rotate(Vector3f.XP.rotationDegrees(rotation[5] * 90));
                matrixStack.translate(0, 6.25F, 6.25F);
                break;
        }
    }
}
