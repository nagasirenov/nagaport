package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.render.model.ModelJetPack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;

public abstract class JetpackBase extends ItemArmorMod {

	protected ModelBiped cachedModel;

	public JetpackBase() {
		super(ArmorModHandler.plate_only, false, true, false, false);
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		super.addInformation(itemstack, player, list, bool);
		list.add(TextFormatting.GOLD + "Can be worn on its own!");
	}
	
	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		
		ItemStack jetpack = ArmorModHandler.pryMods(armor)[ArmorModHandler.plate_only];
		
		if(jetpack == null)
			return;
		
		list.add(TextFormatting.RED + "  " + stack.getDisplayName());
	}
	
	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {
		
		if(!(entity instanceof EntityPlayer))
			return;
		
		ItemStack jetpack = ArmorModHandler.pryMods(armor)[ArmorModHandler.plate_only];
		
		if(jetpack == null)
			return;
				
		onArmorTick(entity.world, (EntityPlayer)entity, jetpack);
		ArmorModHandler.applyMod(armor, jetpack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void modRender(RenderPlayerEvent.SetArmorModel event, ItemStack armor) {

		ModelBiped modelJetpack = getArmorModel(event.getEntityLiving(), null, net.minecraft.inventory.EntityEquipmentSlot.CHEST, null);
		
		RenderPlayer renderer = event.getRenderer();
		ModelBiped model = ((net.minecraft.client.model.ModelBiped) renderer.getMainModel());
		EntityPlayer player = event.getEntityPlayer();

		modelJetpack.isSneak = model.isSneak;
		
		float interp = event.getPartialRenderTick();
		float yawHead = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * interp;
		float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * interp;
		float yaw = yawHead - yawOffset;
		float yawWrapped = MathHelper.wrapDegrees(yawHead - yawOffset);
		float pitch = player.rotationPitch;
		
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(this.getArmorTexture(armor, event.getEntity(), net.minecraft.inventory.EntityEquipmentSlot.CHEST, null)));
		modelJetpack.render(event.getEntityPlayer(), 0.0F, 0.0F, yawWrapped, yaw, pitch, 0.0625F);
	}

	@Override
	public boolean isValidArmor(ItemStack stack, net.minecraft.inventory.EntityEquipmentSlot armorType, Entity entity) {
		return armorType == net.minecraft.inventory.EntityEquipmentSlot.CHEST;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, net.minecraft.inventory.EntityEquipmentSlot armorSlot, ModelBiped defaultModel) {
		if (armorSlot == net.minecraft.inventory.EntityEquipmentSlot.CHEST) {
			if (cachedModel == null) {
				this.cachedModel = new ModelJetPack();
			}
			return this.cachedModel;
		}
		
		return null;
	}
}
