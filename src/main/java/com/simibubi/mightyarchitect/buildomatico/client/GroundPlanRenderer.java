package com.simibubi.mightyarchitect.buildomatico.client;

import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.TheMightyArchitect;
import com.simibubi.mightyarchitect.buildomatico.helpful.TessellatorHelper;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.GroundPlan;
import com.simibubi.mightyarchitect.buildomatico.model.groundPlan.Room;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class GroundPlanRenderer {

	public static final ResourceLocation blueprintShaderLocation = new ResourceLocation(TheMightyArchitect.ID,
			"shaders/post/blueprint.json");
	public static final ResourceLocation heavyTexture = new ResourceLocation(TheMightyArchitect.ID,
			"textures/blocks/markers/heavy.png");
	public static final ResourceLocation lightTexture = new ResourceLocation(TheMightyArchitect.ID,
			"textures/blocks/markers/light.png");
	public static final ResourceLocation innerTexture = new ResourceLocation(TheMightyArchitect.ID,
			"textures/blocks/markers/inner.png");
	public static final ResourceLocation trimTexture = new ResourceLocation(TheMightyArchitect.ID,
			"textures/blocks/markers/trim.png");
	
	public static void updateShader(boolean active) {
		Minecraft mc = Minecraft.getMinecraft();
		if (active && !pencilShaderActive(mc)) 
			mc.entityRenderer.loadShader(blueprintShaderLocation);
		if (!active && pencilShaderActive(mc)) 
			mc.entityRenderer.stopUseShader();
	}
	
	private static boolean pencilShaderActive(Minecraft mc) {
		return mc.entityRenderer.isShaderActive()
				&& mc.entityRenderer.getShaderGroup().getShaderGroupName().equals(blueprintShaderLocation.toString());
	}
	
	private Minecraft mc;
	
	public GroundPlanRenderer(Minecraft mc) {
		this.mc = mc;
	}
		
	public void renderGroundPlan(GroundPlan groundPlan, BlockPos anchor) {
		if (groundPlan != null && anchor != null) {
			BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			ResourceLocation currentTexture = null;
			mc.getTextureManager().bindTexture(innerTexture);

			for (Room c : groundPlan.getAll()) {

				ResourceLocation newTexture = (c.layer == 0) ? heavyTexture : lightTexture;
				if (newTexture != currentTexture) {
					Tessellator.getInstance().draw();
					mc.getTextureManager().bindTexture(newTexture);
					bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
					currentTexture = newTexture;
				}
			}

			Tessellator.getInstance().draw();
			mc.getTextureManager().bindTexture(trimTexture);
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			for (Room c : groundPlan.getAll()) {
				BlockPos pos = c.getOrigin().add(anchor);
				TessellatorHelper.walls(bufferBuilder, pos, new BlockPos(c.width, 1, c.length), 0.125, false, true);
				if (c.isTop())
					TessellatorHelper.walls(bufferBuilder, pos.add(0, c.height, 0), new BlockPos(c.width, 1, c.length),
							0.125, false, true);
			}

			Tessellator.getInstance().draw();
			mc.getTextureManager().bindTexture(innerTexture);
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			for (Room room : groundPlan.getAll()) {
				BlockPos pos = room.getOrigin().add(anchor);
				TessellatorHelper.walls(bufferBuilder, pos, room.getSize(), -0.250, true, false);
			}

			Tessellator.getInstance().draw();
		}
	}

	

}
