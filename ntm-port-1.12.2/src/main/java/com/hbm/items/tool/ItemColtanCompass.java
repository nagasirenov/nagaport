package com.hbm.items.tool;

import java.util.List;
import java.util.Random;

import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.render.icon.LegacyIconRegister;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemColtanCompass extends Item {

	@SideOnly(Side.CLIENT)
	private IIcon itemIcon;

	public int lastX = 0;
	public int lastZ = 0;
	public long lease = 0;

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add("Points towards the coltan deposit.");
		list.add("The deposit is a large area where coltan ore spawns like standard ore,");
		list.add("it's not one large blob of ore on that exact location.");
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inhand) {
		
		if(world.isRemote) {
			if(stack.hasTagCompound()) {
				lastX = stack.stackTagCompound.getInteger("colX");
				lastZ = stack.stackTagCompound.getInteger("colZ");
				lease = System.currentTimeMillis() + 1000;
				
				Vec3d vec = new Vec3d(entity.posX - lastX, 0, entity.posZ - lastZ);
				MainRegistry.proxy.displayTooltip(((int) vec.length()) + "m", MainRegistry.proxy.ID_COMPASS);
			}
			
			if(ItemColtanCompass.this.lease < System.currentTimeMillis()) {
				lastX = 0;
				lastZ = 0;
			}
			
		} else {
			if(!stack.hasTagCompound()) {
				stack.stackTagCompound = new NBTTagCompound();

				Random colRand = new Random(world.getSeed() + 5);
				int colX = (int) (colRand.nextGaussian() * 1500);
				int colZ = (int) (colRand.nextGaussian() * 1500);

				stack.stackTagCompound.setInteger("colX", colX);
				stack.stackTagCompound.setInteger("colZ", colZ);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		if(!(register instanceof LegacyIconRegister)) throw new IllegalArgumentException("ItemColtanCompass requires the NTM texture-atlas adapter");
		TextureColtass compass = new TextureColtass(RefStrings.MODID + ":coltass");
		this.itemIcon = ((LegacyIconRegister) register).registerGeneratedSprite(compass); //the animated compass sprite is stitched into the 1.12 atlas
	}

	@SideOnly(Side.CLIENT)
	public class TextureColtass extends TextureAtlasSprite {

		private double currentAngle;
		private double angleDelta;

		public TextureColtass(String texture) {
			super(texture);
		}

		public void updateAnimation() {
			Minecraft minecraft = Minecraft.getMinecraft();

			if(minecraft.world != null && minecraft.player != null && !(ItemColtanCompass.this.lastX == 0 && ItemColtanCompass.this.lastZ == 0)) {
				this.updateCompass(minecraft.world, minecraft.player.posX, minecraft.player.posZ, (double) minecraft.player.rotationYaw, false, false);
			} else {
				this.updateCompass((World) null, 0.0D, 0.0D, 0.0D, true, false);
			}
		}

		public void updateCompass(World world, double x, double z, double yaw, boolean ignoreDestination, boolean instantSnap) {

			if(!this.framesTextureData.isEmpty()) {
				double angle = 0.0D;

				if(world != null && !ignoreDestination && world.provider.isSurfaceWorld() && ItemColtanCompass.this.lease > System.currentTimeMillis()) {

					double d4 = (double) ItemColtanCompass.this.lastX - x;
					double d5 = (double) ItemColtanCompass.this.lastZ - z;
					yaw %= 360.0D;
					angle = -((yaw - 90.0D) * Math.PI / 180.0D - Math.atan2(d5, d4));
					
				} else {
					angle = Math.random() * Math.PI * 2.0D;
				}

				if(instantSnap) {
					this.currentAngle = angle;

				} else {
					double d6;

					for(d6 = angle - this.currentAngle; d6 < -Math.PI; d6 += (Math.PI * 2D)) {
						;
					}

					while(d6 >= Math.PI) {
						d6 -= (Math.PI * 2D);
					}

					if(d6 < -1.0D) {
						d6 = -1.0D;
					}

					if(d6 > 1.0D) {
						d6 = 1.0D;
					}

					this.angleDelta += d6 * 0.1D;
					this.angleDelta *= 0.8D;
					this.currentAngle += this.angleDelta;
				}

				int frameCount = this.framesTextureData.size();
				int i = (int) ((this.currentAngle / (Math.PI * 2D) + 1.0D) * frameCount) % frameCount;
				if(i < 0) i = (i + frameCount) % frameCount;

				if(i != this.frameCounter) {
					this.frameCounter = i;
					TextureUtil.uploadTextureMipmap((int[][]) this.framesTextureData.get(this.frameCounter), this.width, this.height, this.originX, this.originY, false, false);
				}
			}
		}
	}
}
