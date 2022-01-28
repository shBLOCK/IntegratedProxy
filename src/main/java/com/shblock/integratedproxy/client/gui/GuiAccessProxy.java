package com.shblock.integratedproxy.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.shblock.integratedproxy.client.data.AccessProxyClientData;
import com.shblock.integratedproxy.inventory.container.ContainerAccessProxy;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.*;
import org.cyclops.integrateddynamics.core.client.gui.container.DisplayErrorsComponent;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

public class GuiAccessProxy extends ContainerScreenExtended<ContainerAccessProxy> {
    public DisplayErrorsComponent errorX = new DisplayErrorsComponent();
    public DisplayErrorsComponent errorY = new DisplayErrorsComponent();
    public DisplayErrorsComponent errorZ = new DisplayErrorsComponent();
    public DisplayErrorsComponent errorDisplay = new DisplayErrorsComponent();

    private static final int ERRORS_X = 20;
    private static final int ERRORS_Y = 71;

    public GuiAccessProxy(ContainerAccessProxy container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return getGuiTexture();
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return new ResourceLocation("integrated_proxy:textures/gui/access_proxy_gui.png");
    }

    @Override
    protected int getBaseYSize() {
        return 170;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (RenderHelpers.isPointInRegion(offsetX + this.getGuiLeft() + 44, offsetY + this.getGuiTop() + 20, 90, 16, mouseX - this.offsetX, mouseY - this.offsetY) && button == 0) {
            ValueNotifierHelpers.setValue(getMenu(), getMenu().lastPosModeValueId, getMenu().getLastPosModeValue() == 0 ? 1 : 0);
            return true;
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }

    public void drawCenteredString(Font fontRendererIn, String text, int x, int y, int color) {
        fontRendererIn.draw(new PoseStack(), text, x - fontRendererIn.getSplitter().stringWidth(text) / 2, (float)y, color);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float f, int x, int y) {
//        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);//?

        RenderHelpers.bindTexture(this.texture);
        this.blit(matrixStack, this.offsetX + this.getGuiLeft(), this.offsetY + this.getGuiTop(), 0, 0, 177, 170);

//        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.errorX.drawBackground(matrixStack, getMenu().getErrors(getMenu().lastXErrorId), ERRORS_X + 36 * 0 + 9, ERRORS_Y, ERRORS_X + 36 * 0 + 9, ERRORS_Y, this, this.getGuiLeft(), this.getGuiTop(), getMenu().variableOk(getMenu().lastXOkId));
        this.errorY.drawBackground(matrixStack, getMenu().getErrors(getMenu().lastYErrorId), ERRORS_X + 36 * 1 + 9, ERRORS_Y, ERRORS_X + 36 * 1 + 9, ERRORS_Y, this, this.getGuiLeft(), this.getGuiTop(), getMenu().variableOk(getMenu().lastYOkId));
        this.errorZ.drawBackground(matrixStack, getMenu().getErrors(getMenu().lastZErrorId), ERRORS_X + 36 * 2 + 9, ERRORS_Y, ERRORS_X + 36 * 2 + 9, ERRORS_Y, this, this.getGuiLeft(), this.getGuiTop(), getMenu().variableOk(getMenu().lastZOkId));
        this.errorDisplay.drawBackground(matrixStack, getMenu().getErrors(getMenu().lastDisplayErrorId), ERRORS_X + 36 * 3 + 9, ERRORS_Y, ERRORS_X + 36 * 3 + 9, ERRORS_Y, this, this.getGuiLeft(), this.getGuiTop(), getMenu().variableOk(getMenu().lastDisplayOkId));

        if (this.getMenu().getLastPosModeValue() == 0) {
            drawCenteredString(this.font, I18n.get("integrated_proxy.gui.access_proxy.relative_mode"), offsetX + this.getGuiLeft() + 88, offsetY + this.getGuiTop() + 24, 4210752);
        } else {
            drawCenteredString(this.font, I18n.get("integrated_proxy.gui.access_proxy.absolute_mode"), offsetX + this.getGuiLeft() + 88, offsetY + this.getGuiTop() + 24, 4210752);
        }
        drawCenteredString(this.font, I18n.get("integrated_proxy.gui.access_proxy.x"), offsetX + this.getGuiLeft() + 27 + 36 * 0 + 9, offsetY + this.getGuiTop() + 42, 4210752);
        drawCenteredString(this.font, I18n.get("integrated_proxy.gui.access_proxy.y"), offsetX + this.getGuiLeft() + 27 + 36 * 1 + 9, offsetY + this.getGuiTop() + 42, 4210752);
        drawCenteredString(this.font, I18n.get("integrated_proxy.gui.access_proxy.z"), offsetX + this.getGuiLeft() + 27 + 36 * 2 + 9, offsetY + this.getGuiTop() + 42, 4210752);
        drawCenteredString(this.font, I18n.get("integrated_proxy.gui.access_proxy.display_value"), offsetX + this.getGuiLeft() + 27 + 36 * 3 + 9, offsetY + this.getGuiTop() + 42, 4210752);

        if (getMenu().getTilePos() != null) {
            DimPos tilePos = getMenu().getTilePos();
            DimPos target = AccessProxyClientData.getInstance().getTarget(tilePos);
            if (target != null) {
                String pos_str = I18n.get(
                        "integrated_proxy.gui.access_proxy.display_pos",
                        target.getBlockPos().getX(),
                        target.getBlockPos().getY(),
                        target.getBlockPos().getZ()
                );
                RenderHelpers.drawScaledCenteredString(matrixStack, this.font, pos_str, this.getGuiLeftTotal() + 94, this.getGuiTopTotal() + 11, 76, ValueTypes.INTEGER.getDisplayColor());
            } else {
                onClose();
            }
        }
    }

    @Override
    protected void drawCurrentScreen(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.drawCurrentScreen(matrixStack, mouseX, mouseY, partialTicks);
//        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//        RenderSystem.translatef(1, 1, 1);
        matrixStack.translate(1, 1, 1);//?
        this.errorX.drawForeground(matrixStack, getMenu().getErrors(getMenu().lastXErrorId), ERRORS_X + 36 * 0 + 9, ERRORS_Y, mouseX, mouseY, this, this.getGuiLeft(), this.getGuiTop());
        this.errorY.drawForeground(matrixStack, getMenu().getErrors(getMenu().lastYErrorId), ERRORS_X + 36 * 1 + 9, ERRORS_Y, mouseX, mouseY, this, this.getGuiLeft(), this.getGuiTop());
        this.errorZ.drawForeground(matrixStack, getMenu().getErrors(getMenu().lastZErrorId), ERRORS_X + 36 * 2 + 9, ERRORS_Y, mouseX, mouseY, this, this.getGuiLeft(), this.getGuiTop());
        this.errorDisplay.drawForeground(matrixStack, getMenu().getErrors(getMenu().lastDisplayErrorId), ERRORS_X + 36 * 3 + 9, ERRORS_Y, mouseX, mouseY, this, this.getGuiLeft(), this.getGuiTop());
    }
}
