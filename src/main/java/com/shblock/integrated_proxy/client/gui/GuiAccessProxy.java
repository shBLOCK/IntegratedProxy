package com.shblock.integrated_proxy.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shblock.integrated_proxy.client.render.world.AccessProxyTargetRenderer;
import com.shblock.integrated_proxy.helper.DimPosHelper;
import com.shblock.integrated_proxy.inventory.container.ContainerAccessProxy;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
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

    public GuiAccessProxy(ContainerAccessProxy container, PlayerInventory inventory, ITextComponent title) {
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
        if (RenderHelpers.isPointInRegion(offsetX + this.guiLeft + 44, offsetY + this.guiTop + 20, 90, 16, mouseX - this.offsetX, mouseY - this.offsetY) && button == 0) {
            ValueNotifierHelpers.setValue(getContainer(), getContainer().lastPosModeValueId, getContainer().getLastPosModeValue() == 0 ? 1 : 0);
            return true;
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }

    public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
        fontRendererIn.drawString(new MatrixStack(), text, (float)(x - fontRendererIn.getStringWidth(text) / 2), (float)y, color);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float f, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.getMinecraft().getTextureManager().bindTexture(this.texture);
        this.blit(matrixStack, this.offsetX + this.guiLeft, this.offsetY + this.guiTop, 0, 0, 177, 170);

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.errorX.drawBackground(matrixStack, getContainer().getErrors(getContainer().lastXErrorId), ERRORS_X + 36 * 0 + 9, ERRORS_Y, ERRORS_X + 36 * 0 + 9, ERRORS_Y, this, this.guiLeft, this.guiTop, getContainer().variableOk(getContainer().lastXOkId));
        this.errorY.drawBackground(matrixStack, getContainer().getErrors(getContainer().lastYErrorId), ERRORS_X + 36 * 1 + 9, ERRORS_Y, ERRORS_X + 36 * 1 + 9, ERRORS_Y, this, this.guiLeft, this.guiTop, getContainer().variableOk(getContainer().lastYOkId));
        this.errorZ.drawBackground(matrixStack, getContainer().getErrors(getContainer().lastZErrorId), ERRORS_X + 36 * 2 + 9, ERRORS_Y, ERRORS_X + 36 * 2 + 9, ERRORS_Y, this, this.guiLeft, this.guiTop, getContainer().variableOk(getContainer().lastZOkId));
        this.errorDisplay.drawBackground(matrixStack, getContainer().getErrors(getContainer().lastDisplayErrorId), ERRORS_X + 36 * 3 + 9, ERRORS_Y, ERRORS_X + 36 * 3 + 9, ERRORS_Y, this, this.guiLeft, this.guiTop, getContainer().variableOk(getContainer().lastDisplayOkId));

        if (this.getContainer().getLastPosModeValue() == 0) {
            drawCenteredString(this.font, I18n.format("integrated_proxy.gui.access_proxy.relative_mode"), offsetX + this.guiLeft + 88, offsetY + this.guiTop + 24, 4210752);
        } else {
            drawCenteredString(this.font, I18n.format("integrated_proxy.gui.access_proxy.absolute_mode"), offsetX + this.guiLeft + 88, offsetY + this.guiTop + 24, 4210752);
        }
        drawCenteredString(this.font, I18n.format("integrated_proxy.gui.access_proxy.x"), offsetX + this.guiLeft + 27 + 36 * 0 + 9, offsetY + this.guiTop + 42, 4210752);
        drawCenteredString(this.font, I18n.format("integrated_proxy.gui.access_proxy.y"), offsetX + this.guiLeft + 27 + 36 * 1 + 9, offsetY + this.guiTop + 42, 4210752);
        drawCenteredString(this.font, I18n.format("integrated_proxy.gui.access_proxy.z"), offsetX + this.guiLeft + 27 + 36 * 2 + 9, offsetY + this.guiTop + 42, 4210752);
        drawCenteredString(this.font, I18n.format("integrated_proxy.gui.access_proxy.display_value"), offsetX + this.guiLeft + 27 + 36 * 3 + 9, offsetY + this.guiTop + 42, 4210752);

        if (getContainer().getTilePos() != null) {
            DimPos tilePos = getContainer().getTilePos();
            DimPos target = AccessProxyTargetRenderer.getInstance().get(tilePos);
            String pos_str = I18n.format(
                    "integrated_proxy.gui.access_proxy.display_pos",
                    target.getBlockPos().getX(),
                    target.getBlockPos().getY(),
                    target.getBlockPos().getZ()
            );
            RenderHelpers.drawScaledCenteredString(matrixStack, this.font, pos_str, this.getGuiLeftTotal() + 94, this.getGuiTopTotal() + 11, 76, ValueTypes.INTEGER.getDisplayColor());
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.translatef(1, 1, 1);
        this.errorX.drawForeground(matrixStack, getContainer().getErrors(getContainer().lastXErrorId), ERRORS_X + 36 * 0 + 9, ERRORS_Y, mouseX, mouseY, this, this.guiLeft, this.guiTop);
        this.errorY.drawForeground(matrixStack, getContainer().getErrors(getContainer().lastYErrorId), ERRORS_X + 36 * 1 + 9, ERRORS_Y, mouseX, mouseY, this, this.guiLeft, this.guiTop);
        this.errorZ.drawForeground(matrixStack, getContainer().getErrors(getContainer().lastZErrorId), ERRORS_X + 36 * 2 + 9, ERRORS_Y, mouseX, mouseY, this, this.guiLeft, this.guiTop);
        this.errorDisplay.drawForeground(matrixStack, getContainer().getErrors(getContainer().lastDisplayErrorId), ERRORS_X + 36 * 3 + 9, ERRORS_Y, mouseX, mouseY, this, this.guiLeft, this.guiTop);
    }
}
