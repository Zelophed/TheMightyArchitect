package com.simibubi.mightyarchitect.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.mightyarchitect.gui.widgets.AbstractSimiWidget;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;

public abstract class AbstractSimiScreen extends Screen {

	protected int sWidth, sHeight;
	protected int topLeftX, topLeftY;
	protected List<Widget> widgets;

	protected AbstractSimiScreen() {
		super(new StringTextComponent(""));
		widgets = new ArrayList<>();
	}

	protected void setWindowSize(int width, int height) {
		sWidth = width;
		sHeight = height;
		topLeftX = (this.width - sWidth) / 2;
		topLeftY = (this.height - sHeight) / 2;
	}

	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		renderBackground(ms);
		renderWindow(ms, mouseX, mouseY, partialTicks);
		for (Widget widget : widgets)
			widget.render(ms, mouseX, mouseY, partialTicks);
		renderWindowForeground(ms, mouseX, mouseY, partialTicks);
		for (Widget widget : widgets)
			widget.renderToolTip(ms, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean result = false;
		for (Widget widget : widgets) {
			if (widget.mouseClicked(x, y, button))
				result = true;
		}
		return result;
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		for (Widget widget : widgets) {
			if (widget.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_))
				return true;
		}
		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	public boolean charTyped(char character, int code) {
		for (Widget widget : widgets) {
			if (widget.charTyped(character, code))
				return true;
		}
		if (character == 'e')
			onClose();
		return super.charTyped(character, code);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		for (Widget widget : widgets) {
			if (widget.mouseScrolled(mouseX, mouseY, delta))
				return true;
		}
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected abstract void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks);

	protected void renderWindowForeground(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		for (Widget widget : widgets) {
			if (!widget.isHovered())
				continue;
			if (widget instanceof AbstractSimiWidget && !((AbstractSimiWidget) widget).getToolTip()
				.isEmpty())
				renderComponentTooltip(ms, ((AbstractSimiWidget) widget).getToolTip(), mouseX, mouseY);
		}
	}

}
