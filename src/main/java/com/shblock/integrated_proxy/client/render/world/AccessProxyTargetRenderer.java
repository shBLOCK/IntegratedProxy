package com.shblock.integrated_proxy.client.render.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
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
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class AccessProxyTargetRenderer {
    private static final AccessProxyTargetRenderer _instance = new AccessProxyTargetRenderer();

    private final HashMap<DimPos, DimPos> map = new HashMap<>();

    public static AccessProxyTargetRenderer getInstance() {
        return _instance;
    }

    public void put(DimPos proxy, DimPos target) {
        this.map.put(proxy, target);
    }

    public void remove(DimPos proxy) {
        this.map.remove(proxy);
    }

    public DimPos get(BlockPos pos, int dim) {
        return this.map.get(DimPos.of(dim, pos));
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player.equals(Minecraft.getMinecraft().player) && event.player.world.isRemote) {
            this.map.clear();
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();

        EntityPlayer player = Minecraft.getMinecraft().player;
        float partialTicks = event.getPartialTicks();
        double offsetX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
        double offsetY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
        double offsetZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;

        for (Map.Entry<DimPos, DimPos> entry : this.map.entrySet()) {
            DimPos proxy = entry.getKey();
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
    }
}
