package com.hbm.inventory.gui;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.generic.BlockSnowglobe.TileEntitySnowglobe;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class GUIScreenSnowglobe extends GuiScreen {
	
	TileEntitySnowglobe snowglobe;

	public GUIScreenSnowglobe(TileEntitySnowglobe bobble) {
		this.snowglobe = bobble;
	}

	@Override
	public void initGui() {
		mc.getSoundHandler().playSound(com.hbm.sound.GuiSound.create(new ResourceLocation("hbm:block.bobble"), 1.0F));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		
		this.drawDefaultBackground();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		double sizeX = 300;
		double sizeY = 150;
		double left = (this.width - sizeX) / 2;
		double top = (this.height - sizeY) / 2;
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder tessBuffer = tess.getBuffer();
		tessBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		int tessColor0Red = Math.max(0, Math.min(255, (int) ((0F) * 255.0F)));
		int tessColor0Green = Math.max(0, Math.min(255, (int) ((0.2F) * 255.0F)));
		int tessColor0Blue = Math.max(0, Math.min(255, (int) ((0F) * 255.0F)));
		int tessColor0Alpha = Math.max(0, Math.min(255, (int) ((0.8F) * 255.0F)));
		tessBuffer.pos(left + sizeX, top, this.zLevel).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
		tessBuffer.pos(left, top, this.zLevel).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
		tessBuffer.pos(left, top + sizeY, this.zLevel).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
		tessBuffer.pos(left + sizeX, top + sizeY, this.zLevel).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
		tess.draw();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		
		int nextLevel = (int)top + 10;

		String bobbleTitle = "Nuclear Tech Commemorative Snowglobe";
		this.fontRenderer.drawStringWithShadow(bobbleTitle, (int)(left + sizeX / 2 - this.fontRenderer.getStringWidth(bobbleTitle) / 2), nextLevel, 0x00ff00);
		
		nextLevel += 10;
		
		String bobbleName = this.snowglobe.type.label;
		this.fontRenderer.drawStringWithShadow(bobbleName, (int)(left + sizeX / 2 - this.fontRenderer.getStringWidth(bobbleName) / 2), nextLevel, 0x009900);
		
		nextLevel += 20;
		
		/*if(this.snowglobe.type.contribution != null) {

			String title = "Has contributed";
			this.fontRendererObj.drawStringWithShadow(title, (int)(left + sizeX / 2 - this.fontRendererObj.getStringWidth(title) / 2), nextLevel, 0x00ff00);
			
			nextLevel += 10;


			String[] list = this.snowglobe.type.contribution.split("\\$");
			for(String text : list) {
				this.fontRendererObj.drawStringWithShadow(text, (int)(left + sizeX / 2 - this.fontRendererObj.getStringWidth(text) / 2), nextLevel, 0x009900);
				nextLevel += 10;
			}

			nextLevel += 10;
		}*/
		
		if(this.snowglobe.type.inscription != null) {

			String title = "On the bottom is the following inscription:";
			this.fontRenderer.drawStringWithShadow(title, (int)(left + sizeX / 2 - this.fontRenderer.getStringWidth(title) / 2), nextLevel, 0x00ff00);
			
			nextLevel += 10;

			List<String> list = I18nUtil.autoBreakWithParagraphs(this.fontRenderer, this.snowglobe.type.inscription, 280);
			for(String text : list) {
				this.fontRenderer.drawStringWithShadow(text, (int)(left + sizeX / 2 - this.fontRenderer.getStringWidth(text) / 2), nextLevel, 0x009900);
				nextLevel += 10;
			}

			nextLevel += 10;
		}
		
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	@Override
	protected void keyTyped(char c, int key) {
		if(key == 1 || key == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
			this.mc.player.closeScreen();
		}
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
