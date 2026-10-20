package com.hbm.items.special;

import com.hbm.main.MainRegistry;
import com.hbm.render.icon.LegacyIconRegister;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.EnumSkyBlock;

public class ItemAlexandrite extends Item {

	@SideOnly(Side.CLIENT)
	private IIcon itemIcon;

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		if(!(register instanceof LegacyIconRegister)) throw new IllegalArgumentException("ItemAlexandrite requires the NTM texture-atlas adapter");
		if(getRegistryName() == null) throw new IllegalStateException("ItemAlexandrite must be registered before its icon callback");
		TextureAlexandrite texture = new TextureAlexandrite(getRegistryName().toString());
		this.itemIcon = ((LegacyIconRegister) register).registerGeneratedSprite(texture);
	}

	@SideOnly(Side.CLIENT)
	public class TextureAlexandrite extends TextureAtlasSprite {

		protected TextureAlexandrite(String texture) {
			super(texture);
		}
		
		private int lastLight = 0;

		@Override
		public void updateAnimation() {
			
			if(Minecraft.getMinecraft().world == null)
				return;
			
			EntityPlayer player = MainRegistry.proxy.me();
			
			if(player == null)
				return;
			
			int x = (int) Math.floor(player.posX);
			int y = (int) Math.floor(player.posY);
			int z = (int) Math.floor(player.posZ);
			
			int light = player.world.getSavedLightValue(EnumSkyBlock.Block, x, y, z);

			if(light != lastLight) {
				lastLight = light;

				this.frameCounter = light;
				TextureUtil.uploadTextureMipmap((int[][]) this.framesTextureData.get(this.frameCounter), this.width, this.height, this.originX, this.originY, false, false);
			}
		}
	}
}
