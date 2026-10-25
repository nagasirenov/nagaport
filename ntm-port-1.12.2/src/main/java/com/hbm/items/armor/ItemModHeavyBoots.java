package com.hbm.items.armor;

import java.util.List;

import com.hbm.dim.CelestialBody;
import com.hbm.handler.ArmorModHandler;
import com.hbm.util.AstronomyUtil;
import com.hbm.util.i18n.I18nUtil;

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
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderPlayerEvent;

public class ItemModHeavyBoots extends ItemArmorMod {

	public ItemModHeavyBoots() {
		super(ArmorModHandler.boots_only, false, false, false, true);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		list.add(TextFormatting.BLUE + "Increases fall speed in low gravity");
		list.add(TextFormatting.BLUE + "Activated by crouching");
		list.add("");
		super.addInformation(itemstack, player, list, bool);
		list.add(TextFormatting.GOLD + "Can be worn on its own!");
		list.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "We take no responsibility for any deaths that may");
		list.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "occur while using these boots without a space suit.");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (" + I18nUtil.resolveKey("armor.fastFall") + ")");
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		// if crouching in air, apply extra gravity until we match the overworld
		if(player.isSneaking() && !player.onGround && !player.isInWater()) {
			float gravity = CelestialBody.getGravity(player);
			if(gravity > 1.5F) return;
			if(gravity == 0) return;
			if(gravity < 0.2F) gravity = 0.2F;

			player.motionY /= 0.98F;
			player.motionY += (gravity / 20F);
			player.motionY -= (AstronomyUtil.STANDARD_GRAVITY / 20F);
			player.motionY *= 0.98F;
		}
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {
		if(!(entity instanceof EntityPlayer))
			return;

		ItemStack boots = ArmorModHandler.pryMods(armor)[ArmorModHandler.boots_only];

		if(boots == null)
			return;

		onArmorTick(entity.world, (EntityPlayer)entity, boots);
		ArmorModHandler.applyMod(armor, boots);
	}

	@Override
	public boolean isValidArmor(ItemStack stack, net.minecraft.inventory.EntityEquipmentSlot armorType, Entity entity) {
		return armorType == net.minecraft.inventory.EntityEquipmentSlot.FEET;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, net.minecraft.inventory.EntityEquipmentSlot slot, String type) {
		return "hbm:textures/armor/heavy_boots.png";
	}

	@SideOnly(Side.CLIENT)
	public void armorRender(RenderPlayerEvent.SetArmorModel event, ItemStack armor) {
		RenderPlayer renderer = event.getRenderer();
		ModelBiped modelbiped = ((net.minecraft.client.model.ModelBiped) renderer.getMainModel());
		modelbiped.bipedHead.showModel = false;
		modelbiped.bipedHeadwear.showModel = false;
		modelbiped.bipedBody.showModel = false;
		modelbiped.bipedRightArm.showModel = false;
		modelbiped.bipedLeftArm.showModel = false;
		modelbiped.bipedRightLeg.showModel = true;
		modelbiped.bipedLeftLeg.showModel = true;
		modelbiped.swingProgress = ((net.minecraft.client.model.ModelBiped) renderer.getMainModel()).swingProgress;
		modelbiped.isRiding = ((net.minecraft.client.model.ModelBiped) renderer.getMainModel()).isRiding;
		modelbiped.isChild = ((net.minecraft.client.model.ModelBiped) renderer.getMainModel()).isChild;

		EntityPlayer player = event.getEntityPlayer();

		float interp = event.getPartialRenderTick();

		float swingRight = player.prevLimbSwingAmount + (player.limbSwingAmount - player.prevLimbSwingAmount) * interp;
		float swingLeft = player.limbSwing - player.limbSwingAmount * (1.0F - interp);
		if(player.isChild()) swingLeft *= 3.0F;
		if(swingRight > 1.0F) swingRight = 1.0F;

		float yawHead = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * interp;
		float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * interp;
		float yaw = yawHead - yawOffset;
		float yawWrapped = MathHelper.wrapDegrees(yawHead - yawOffset);
		float pitch = player.rotationPitch;

		modelbiped.setLivingAnimations(player, swingLeft, swingRight, interp);
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(this.getArmorTexture(armor, event.getEntity(), net.minecraft.inventory.EntityEquipmentSlot.FEET, null)));
		modelbiped.render(event.getEntityPlayer(), swingLeft, swingRight, yawWrapped, yaw, pitch, 0.0625F);
	}

}
