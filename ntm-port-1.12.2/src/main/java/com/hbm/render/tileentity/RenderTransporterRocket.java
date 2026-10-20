package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityTransporterRocket;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;

public class RenderTransporterRocket extends TileEntitySpecialRenderer {

	@Override
	public void render(TileEntity te, double x, double y, double z, float interp, int nativeDestroyStage, float nativeAlpha) {
		if(!(te instanceof TileEntityTransporterRocket)) return;
		TileEntityTransporterRocket pad = (TileEntityTransporterRocket) te;

		GL11.glPushMatrix();
		{

			GL11.glTranslated(x + 0.5D, y, z + 0.5D);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glShadeModel(GL11.GL_SMOOTH);

			GL11.glRotatef(90, 0, 1, 0);

			switch(te.getBlockMetadata() - BlockDummyable.offset) {
			case 2: GL11.glRotatef(0, 0F, 1F, 0F); break;
			case 4: GL11.glRotatef(90, 0F, 1F, 0F); break;
			case 3: GL11.glRotatef(180, 0F, 1F, 0F); break;
			case 5: GL11.glRotatef(270, 0F, 1F, 0F); break;
			}

			bindTexture(ResourceManager.transporter_pad_tex);
			ResourceManager.transporter_pad.renderPart("base");

			GL11.glPushMatrix();
			{

				float rot = MathHelper.clamp(getPipeEngage(pad, interp), (float) (0), (float) (1));

				GL11.glTranslatef(0.0F, 0.75F, -0.75F);
				GL11.glRotatef(rot * -30.0F, 1, 0, 0);
				GL11.glTranslatef(0.0F, -0.75F, 0.75F);

				ResourceManager.transporter_pad.renderPart("pipe");

			}
			GL11.glPopMatrix();

			if(pad.launchTicks < 100) {
				GL11.glPushMatrix();
				{

					GL11.glTranslatef(0.0F, 0.75F + MathHelper.clamp(pad.launchTicks + (pad.hasRocket ? -interp : interp), (float) (0), (float) (200)), 0.0F);
					GL11.glDisable(GL11.GL_CULL_FACE);

					bindTexture(ResourceManager.minerRocket_tex);
					ResourceManager.minerRocket.renderAll();

					GL11.glEnable(GL11.GL_CULL_FACE);

				}
				GL11.glPopMatrix();
			}

			GL11.glShadeModel(GL11.GL_FLAT);

		}
		GL11.glPopMatrix();
	}

	private float getPipeEngage(TileEntityTransporterRocket pad, float interp) {
		if(pad.launchTicks >= 0) {
			return !pad.hasRocket ? (pad.launchTicks + interp) * 0.25F : 1.0F;
		} else {
			return 1 - (-pad.launchTicks - 1 + interp) * 0.25F;
		}
	}



}
