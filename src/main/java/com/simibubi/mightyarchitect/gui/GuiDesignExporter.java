package com.simibubi.mightyarchitect.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.ImmutableList;
import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignTheme;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.gui.widgets.DynamicLabel;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea;
import com.simibubi.mightyarchitect.gui.widgets.ScrollArea.IScrollAction;
import com.simibubi.mightyarchitect.networking.PacketNbt;
import com.simibubi.mightyarchitect.networking.PacketSender;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class GuiDesignExporter extends GuiScreen {

	private int xSize, ySize;
	private int xTopLeft, yTopLeft;

	private List<ScrollArea> scrollAreas;

	private ScrollArea scrollAreaTheme;
	private ScrollArea scrollAreaLayer;
	private ScrollArea scrollAreaType;
	private ScrollArea scrollAreaAdditionalData;

	private DynamicLabel labelTheme;
	private DynamicLabel labelLayer;
	private DynamicLabel labelType;
	private DynamicLabel labelAdditionalData;

	private String additionalDataKey;
	private int additionalDataValue;

	@Override
	public void initGui() {
		super.initGui();
		xSize = GuiResources.EXPORTER.width;
		ySize = GuiResources.EXPORTER.height;
		xTopLeft = (this.width - this.xSize) / 2;
		yTopLeft = (this.height - this.ySize) / 2;

		DesignTheme theme = DesignTheme.Medieval;
		DesignLayer layer = DesignLayer.Independent;
		DesignType type = DesignType.WALL;
		additionalDataValue = -1;

		ItemStack heldItem = mc.player.getHeldItemMainhand();
		if (heldItem.hasTagCompound()) {
			NBTTagCompound tag = heldItem.getTagCompound();

			if (tag.hasKey("Theme"))
				theme = DesignTheme.valueOf(tag.getString("Theme"));
			if (tag.hasKey("Layer"))
				layer = DesignLayer.valueOf(tag.getString("Layer"));
			if (tag.hasKey("Type"))
				type = DesignType.valueOf(tag.getString("Type"));
			if (tag.hasKey("Additional"))
				additionalDataValue = tag.getInteger("Additional");
		}

		labelTheme = new DynamicLabel(xTopLeft + 96, yTopLeft + 28);
		labelLayer = new DynamicLabel(xTopLeft + 96, yTopLeft + 48);
		labelType = new DynamicLabel(xTopLeft + 96, yTopLeft + 68);
		labelAdditionalData = new DynamicLabel(xTopLeft + 96, yTopLeft + 88);

		scrollAreas = new LinkedList<>();
		additionalDataKey = "";
		initScrollAreas(theme, layer, type);
	}

	private void initScrollAreas(DesignTheme theme, DesignLayer layer, DesignType type) {
		scrollAreas.clear();

		List<DesignTheme> themes = ImmutableList.copyOf(DesignTheme.values());
		List<DesignLayer> layers = theme.getLayers();

		if (!layers.contains(layer))
			layer = DesignLayer.Independent;

		List<DesignType> types = theme.getTypes();
		if (!types.contains(type))
			type = DesignType.WALL;

		List<String> layerOptions = new ArrayList<>();
		layers.forEach(l -> layerOptions.add(l.getDisplayName()));

		scrollAreaLayer = new ScrollArea(layerOptions, new IScrollAction() {
			@Override
			public void onScroll(int position) {
				labelLayer.text = layerOptions.get(position);
			}
		});
		scrollAreaLayer.setBounds(xTopLeft + 93, yTopLeft + 45, 70, 14);
		scrollAreaLayer.setTitle("Style Layer");
		scrollAreaLayer.setState(layers.indexOf(layer));
		labelLayer.text = layer.getDisplayName();
		scrollAreas.add(scrollAreaLayer);

		List<String> typeOptions = new ArrayList<>();
		types.forEach(t -> typeOptions.add(t.getDisplayName()));

		scrollAreaType = new ScrollArea(typeOptions, new IScrollAction() {
			@Override
			public void onScroll(int position) {
				labelType.text = typeOptions.get(position);
				initAdditionalDataScrollArea(types.get(position));
			}
		});
		scrollAreaType.setBounds(xTopLeft + 93, yTopLeft + 65, 70, 14);
		scrollAreaType.setTitle("Design Type");
		scrollAreaType.setState(types.indexOf(type));
		labelType.text = type.getDisplayName();
		scrollAreas.add(scrollAreaType);

		List<String> themeOptions = new ArrayList<>();
		themes.forEach(t -> themeOptions.add(t.getDisplayName()));

		scrollAreaTheme = new ScrollArea(themeOptions, new IScrollAction() {
			@Override
			public void onScroll(int position) {
				labelTheme.text = themeOptions.get(position);
				initScrollAreas(DesignTheme.values()[position], layers.get(scrollAreaLayer.getState()),
						types.get(scrollAreaType.getState()));
			}
		});
		scrollAreaTheme.setBounds(xTopLeft + 93, yTopLeft + 25, 70, 14);
		scrollAreaTheme.setTitle("Theme");
		scrollAreaTheme.setState(themes.indexOf(theme));
		labelTheme.text = theme.getDisplayName();
		scrollAreas.add(scrollAreaTheme);

		initAdditionalDataScrollArea(type);
	}

	private void initAdditionalDataScrollArea(DesignType type) {
		if (type.hasAdditionalData()) {

			additionalDataKey = type.getAdditionalDataName();

			if (type.hasSizeData()) {
				if (additionalDataValue == -1)
					additionalDataValue = 1;
				
				labelAdditionalData.text = additionalDataValue + "m";
				scrollAreaAdditionalData = new ScrollArea(1, 32, new IScrollAction() {
					@Override
					public void onScroll(int position) {
						additionalDataValue = position;
						labelAdditionalData.text = position + "m";
					}
				});
				scrollAreaAdditionalData.setNumeric(true);

			} else if (type.hasSubtypes()) {				
				if (additionalDataValue == -1)
					additionalDataValue = 0;
				
				List<String> subtypeOptions = type.getSubtypeOptions();
				if (additionalDataValue >= subtypeOptions.size())
					additionalDataValue = 0;
					
				labelAdditionalData.text = subtypeOptions.get(additionalDataValue);
				scrollAreaAdditionalData = new ScrollArea(subtypeOptions, new IScrollAction() {
					@Override
					public void onScroll(int position) {
						additionalDataValue = position;
						labelAdditionalData.text = subtypeOptions.get(position);
					}
				});
				scrollAreaAdditionalData.setNumeric(false);
			}

			scrollAreaAdditionalData.setTitle(additionalDataKey);
			scrollAreaAdditionalData.setBounds(xTopLeft + 93, yTopLeft + 85, 70, 14);
			scrollAreaAdditionalData.setState(additionalDataValue);
			
		} else {

			additionalDataValue = -1;
			additionalDataKey = "";
			labelAdditionalData.text = "";
			scrollAreaAdditionalData = null;

		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		GuiResources.EXPORTER.draw(this, xTopLeft, yTopLeft);
		super.drawScreen(mouseX, mouseY, partialTicks);

		fontRenderer.drawString("Export custom Designs", xTopLeft + 10, yTopLeft + 10, 0x4F7023, false);

		fontRenderer.drawString("Theme / Style", xTopLeft + 10, yTopLeft + 28, 0x4F7023, false);
		fontRenderer.drawString("Building Layer", xTopLeft + 10, yTopLeft + 48, 0x4F7023, false);
		fontRenderer.drawString("Design Type", xTopLeft + 10, yTopLeft + 68, 0x4F7023, false);
		fontRenderer.drawString(additionalDataKey, xTopLeft + 10, yTopLeft + 88, 0x4F7023, false);

		labelTheme.draw(this);
		labelLayer.draw(this);
		labelType.draw(this);
		labelAdditionalData.draw(this);

		scrollAreas.forEach(area -> area.draw(this, mouseX, mouseY));
		if (scrollAreaAdditionalData != null)
			scrollAreaAdditionalData.draw(this, mouseX, mouseY);
	}

	@Override
	public void onGuiClosed() {
		ItemStack heldItemMainhand = mc.player.getHeldItemMainhand();
		NBTTagCompound nbt = new NBTTagCompound();

		DesignTheme chosenTheme = DesignTheme.values()[scrollAreaTheme.getState()];
		nbt.setString("Theme", chosenTheme.name());
		nbt.setString("Layer", chosenTheme.getLayers().get(scrollAreaLayer.getState()).name());
		nbt.setString("Type", chosenTheme.getTypes().get(scrollAreaType.getState()).name());

		if (additionalDataValue != -1)
			nbt.setInteger("Additional", additionalDataValue);

		heldItemMainhand.setTagCompound(nbt);
		PacketSender.INSTANCE.sendToServer(new PacketNbt(heldItemMainhand));
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		int scrollAmount = ((mouseButton == 0) ? -1 : 1) * ((Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) ? 5 : 1);
		scrollAreas.forEach(area -> area.tryScroll(mouseX, mouseY, scrollAmount));
		if (scrollAreaAdditionalData != null)
			scrollAreaAdditionalData.tryScroll(mouseX, mouseY, scrollAmount);
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();

		int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

		int scroll = Mouse.getEventDWheel();
		if (scroll != 0) {
			int amount = (int) (scroll / -120f);
			scrollAreas.forEach(area -> area.tryScroll(i, j, amount));
			if (scrollAreaAdditionalData != null)
				scrollAreaAdditionalData.tryScroll(i, j, amount);
		}
	}

}
