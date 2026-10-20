package com.hbm.items.armor;

import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorEnvsuit;
import com.hbm.render.tileentity.IItemRendererProvider;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;


import java.util.UUID;

import org.lwjgl.opengl.GL11;

public class ArmorEnvsuit extends ArmorFSBPowered implements IItemRendererProvider {

	public ArmorEnvsuit(ArmorMaterial material, int slot, String texture, long maxPower, long chargeRate, long consumption, long drain) {
		super(material, slot, texture, maxPower, chargeRate, consumption, drain);
	}

	@SideOnly(Side.CLIENT)
	ModelArmorEnvsuit[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot) {

		if(models == null) {
			models = new ModelArmorEnvsuit[4];

			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorEnvsuit(i);
		}

		return models[armorSlot];
	}

	private static final UUID speed = UUID.fromString("6ab858ba-d712-485c-bae9-e5e765fc555a");

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {

		super.onArmorTick(world, player, stack);

		if(this != ModItems.envsuit_plate)
			return;

		/// SPEED ///
		Multimap multimap = super.getAttributeModifiers(stack);
		multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), new AttributeModifier(speed, "SQUIRREL SPEED", 0.1, 0));
		player.getAttributeMap().removeAttributeModifiers(multimap);

		if(this.hasFSBArmor(player)) {

			if(player.isSprinting()) player.getAttributeMap().applyAttributeModifiers(multimap);

			if(player.isInWater()) {

				if(!world.isRemote) {
					player.setAir(300);
					player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.NIGHT_VISION, 15 * 20, 0));
				}

				double mo = 0.1 * player.moveForward;
				Vec3d vec = player.getLookVec().scale(mo);

				player.motionX += vec.x;
				player.motionY += vec.y;
				player.motionZ += vec.z;
			} else {
				boolean canRemoveNightVision = true;
				ItemStack helmet = player.inventory.armorInventory[3];
				ItemStack helmetMod = ArmorModHandler.pryMod(helmet, ArmorModHandler.helmet_only); // Get the modification!
				if (helmetMod != null && helmetMod.getItem() instanceof ItemModNightVision) {
					canRemoveNightVision = false;
				}

				if(!world.isRemote && canRemoveNightVision) {
					player.removePotionEffect(net.minecraft.init.MobEffects.NIGHT_VISION);
				}
			}
		}
	}

	@Override public Item getItemForRenderer() { return this; }

	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public ItemRenderBase getRenderer() {
		return new ItemRenderBase( ) {
			public void renderInventory() { setupRenderInv(); }
			public void renderNonInv() { setupRenderNonInv(); }
			public void renderCommon() {
				if(armorType == 0) {
					GL11.glScaled(0.3125, 0.3125, 0.3125);
					GL11.glTranslated(0, 1, 0);
					Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.envsuit_helmet);
					ResourceManager.armor_envsuit.renderPart("Helmet");
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					GL11.glColor3f(1F, 1F, 0.8F);
					ResourceManager.armor_envsuit.renderPart("Lamps");
					GL11.glColor3f(1F, 1F, 1F);
					GL11.glEnable(GL11.GL_TEXTURE_2D);
					GL11.glEnable(GL11.GL_LIGHTING);
				} else {
					renderStandard(ResourceManager.armor_envsuit, armorType,
							ResourceManager.envsuit_helmet, ResourceManager.envsuit_chest, ResourceManager.envsuit_arm, ResourceManager.envsuit_leg,
							"Helmet,Lamps", "Chest", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftFoot", "RightFoot");
				}
			}};
	}
}
