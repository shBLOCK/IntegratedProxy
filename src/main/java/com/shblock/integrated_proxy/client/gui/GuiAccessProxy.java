package com.shblock.integrated_proxy.client.gui;

import com.shblock.integrated_proxy.client.render.world.AccessProxyTargetRenderer;
import com.shblock.integrated_proxy.inventory.container.ContainerAccessProxy;
import com.shblock.integrated_proxy.tileentity.TileAccessProxy;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.DimensionManager;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerConfigurable;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.helper.WorldHelpers;
import org.cyclops.integrateddynamics.core.client.gui.ContainerScreenActiveVariableBase;
import org.cyclops.integrateddynamics.core.client.gui.container.DisplayErrorsComponent;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.inventory.container.ContainerProxy;

import java.util.Set;

public class GuiAccessProxy extends ContainerScreenActiveVariableBase<ContainerAccessProxy> {
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
    protected ResourceLocation constructResourceLocation() {
        return new ResourceLocation(getGuiTexture());
    }

    @Override
    public String getGuiTexture() {
        return "integrated_proxy:textures/gui/access_proxy_gui.png";
    }

    @Override
    protected int getBaseYSize() {
        return 170;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (RenderHelpers.isPointInRegion(offsetX + this.guiLeft + 44, offsetY + this.guiTop + 20, 90, 16, mouseX - this.offsetX, mouseY - this.offsetY) && state == 0) {
            ValueNotifierHelpers.setValue(this, lastPosModeValueId, this.getLastPosModeValue() == 0 ? 1 : 0);
        }
    }

    @Override
    public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
        fontRendererIn.drawString(text, (float)(x - fontRendererIn.getStringWidth(text) / 2), (float)y, color, false);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.mc.getTextureManager().bindTexture(this.texture);
        this.drawTexturedModalRect(this.offsetX + this.guiLeft, this.offsetY + this.guiTop, 0, 0, 177, 170);

        GlStateManager.color(1.0F, 1.0F, 1.0F);
        TileAccessProxy tile = getContainer().getTile();
        this.errorX.drawBackground(tile.evaluator_x.getErrors(), ERRORS_X + 36 * 0 + 9, ERRORS_Y, ERRORS_X + 36 * 0 + 9, ERRORS_Y, this, this.guiLeft, this.guiTop, getContainer().variableOk(getContainer().lastXOkId));
        this.errorY.drawBackground(tile.evaluator_y.getErrors(), ERRORS_X + 36 * 1 + 9, ERRORS_Y, ERRORS_X + 36 * 1 + 9, ERRORS_Y, this, this.guiLeft, this.guiTop, getContainer().variableOk(getContainer().lastYOkId));
        this.errorZ.drawBackground(tile.evaluator_z.getErrors(), ERRORS_X + 36 * 2 + 9, ERRORS_Y, ERRORS_X + 36 * 2 + 9, ERRORS_Y, this, this.guiLeft, this.guiTop, getContainer().variableOk(getContainer().lastZOkId));
        this.errorDisplay.drawBackground(tile.evaluator_display.getErrors(), ERRORS_X + 36 * 3 + 9, ERRORS_Y, ERRORS_X + 36 * 3 + 9, ERRORS_Y, this, this.guiLeft, this.guiTop, getContainer().variableOk(getContainer().lastDisplayOkId));

        if (this.getContainer().getLastPosModeValue() == 0) {
            drawCenteredString(this.fontRenderer, I18n.format("integrated_proxy.gui.access_proxy.relative_mode"), offsetX + this.guiLeft + 88, offsetY + this.guiTop + 24, 4210752);
        } else {
            drawCenteredString(this.fontRenderer, I18n.format("integrated_proxy.gui.access_proxy.absolute_mode"), offsetX + this.guiLeft + 88, offsetY + this.guiTop + 24, 4210752);
        }
        drawCenteredString(this.fontRenderer, I18n.format("integrated_proxy.gui.access_proxy.x"), offsetX + this.guiLeft + 27 + 36 * 0 + 9, offsetY + this.guiTop + 42, 4210752);
        drawCenteredString(this.fontRenderer, I18n.format("integrated_proxy.gui.access_proxy.y"), offsetX + this.guiLeft + 27 + 36 * 1 + 9, offsetY + this.guiTop + 42, 4210752);
        drawCenteredString(this.fontRenderer, I18n.format("integrated_proxy.gui.access_proxy.z"), offsetX + this.guiLeft + 27 + 36 * 2 + 9, offsetY + this.guiTop + 42, 4210752);
        drawCenteredString(this.fontRenderer, I18n.format("integrated_proxy.gui.access_proxy.display_value"), offsetX + this.guiLeft + 27 + 36 * 3 + 9, offsetY + this.guiTop + 42, 4210752);

        DimPos target = AccessProxyTargetRenderer.getInstance().get(getContainer().getTile().getPos(), getContainer().getTile().getWorld().provider.getDimension());
        String pos_str = I18n.format(
                "integrated_proxy.gui.access_proxy.display_pos",
                target.getBlockPos().getX(),
                target.getBlockPos().getY(),
                target.getBlockPos().getZ()
        );
        RenderHelpers.drawScaledCenteredString(this.fontRenderer, pos_str, this.getGuiLeftTotal() + 94, this.getGuiTopTotal() + 11, 76, ValueTypes.INTEGER.getDisplayColor());
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        GlStateManager.translate(1, 1, 1);
        TileAccessProxy tile = getContainer().getTile();
        this.errorX.drawForeground(tile.evaluator_x.getErrors(), ERRORS_X + 36 * 0 + 9, ERRORS_Y, mouseX, mouseY, this, this.guiLeft, this.guiTop);
        this.errorY.drawForeground(tile.evaluator_y.getErrors(), ERRORS_X + 36 * 1 + 9, ERRORS_Y, mouseX, mouseY, this, this.guiLeft, this.guiTop);
        this.errorZ.drawForeground(tile.evaluator_z.getErrors(), ERRORS_X + 36 * 2 + 9, ERRORS_Y, mouseX, mouseY, this, this.guiLeft, this.guiTop);
        this.errorDisplay.drawForeground(tile.evaluator_display.getErrors(), ERRORS_X + 36 * 3 + 9, ERRORS_Y, mouseX, mouseY, this, this.guiLeft, this.guiTop);
    }
}
