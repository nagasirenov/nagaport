package com.hbm.items.armor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.render.model.ModelM65;
import com.hbm.util.ArmorUtil;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.item.IGasMask;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderPlayerEvent;

public class ItemModGasmask extends ItemArmorMod implements IGasMask {

	private ModelM65 modelM65;
	private ResourceLocation tex = new ResourceLocation("hbm:textures/models/modelm65.png");
	private ResourceLocation tex_mono = new ResourceLocation("hbm:textures/models/modelm65mono.png");

	public ItemModGasmask() {
		super(ArmorModHandler.helmet_only, true, false, false, false);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {

		list.add(TextFormatting.GREEN + "Gas protection");

		list.add("");
		super.addInformation(stack, player, list, bool);

		ArmorUtil.addGasMaskTooltip(stack, player, list, bool);

		List<HazardClass> haz = getBlacklist(stack, player);

		if(!haz.isEmpty()) {
			list.add(TextFormatting.RED + "Will never protect against:");

			for(HazardClass clazz : haz) {
				list.add(TextFormatting.DARK_RED + " -" + I18nUtil.resolveKey(clazz.lang));
			}
		}
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {

		list.add(TextFormatting.GREEN + "  " + stack.getDisplayName() + " (gas protection)");
		ArmorUtil.addGasMaskTooltip(stack, MainRegistry.proxy.me(), list, false);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void modRender(RenderPlayerEvent.SetArmorModel event, ItemStack armor) {

		if(this.modelM65 == null) {
			this.modelM65 = new ModelM65();
		}

		RenderPlayer renderer = event.getRenderer();
		ModelBiped model = ((net.minecraft.client.model.ModelBiped) renderer.getMainModel());
		EntityPlayer player = event.getEntityPlayer();

		modelM65.isSneak = model.isSneak;
		modelM65.isChild = ((net.minecraft.client.model.ModelBiped) renderer.getMainModel()).isChild;

		float interp = event.getPartialRenderTick();
		float yawHead = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * interp;
		float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * interp;
		float yaw = yawHead - yawOffset;
		float yawWrapped = MathHelper.wrapDegrees(yawHead - yawOffset);
		float pitch = player.rotationPitch;

		if(this == ModItems.attachment_mask)
			Minecraft.getMinecraft().renderEngine.bindTexture(tex);
		if(this == ModItems.attachment_mask_mono)
			Minecraft.getMinecraft().renderEngine.bindTexture(tex_mono);

		modelM65.render(event.getEntityPlayer(), 0.0F, 0.0F, yawWrapped, yaw, pitch, 0.0625F);
	}

	@Override
	public ArrayList<HazardClass> getBlacklist(ItemStack stack, EntityLivingBase entity) {

		if(this == ModItems.attachment_mask_mono) {
			return new ArrayList<HazardClass>(Arrays.asList(new HazardClass[] {HazardClass.GAS_LUNG, HazardClass.GAS_BLISTERING, HazardClass.BACTERIA}));
		} else {
			return new ArrayList<HazardClass>(Arrays.asList(new HazardClass[] {HazardClass.GAS_BLISTERING}));
		}
	}

	@Override
	public ItemStack getFilter(ItemStack stack, EntityLivingBase entity) {
		return ArmorUtil.getGasMaskFilter(stack);
	}

	@Override
	public void installFilter(ItemStack stack, EntityLivingBase entity, ItemStack filter) {
		ArmorUtil.installGasMaskFilter(stack, filter);
	}

	@Override
	public void damageFilter(ItemStack stack, EntityLivingBase entity, int damage) {
		ArmorUtil.damageGasMaskFilter(stack, damage);
	}

	@Override
	public boolean isFilterApplicable(ItemStack stack, EntityLivingBase entity, ItemStack filter) {
		return true;
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if(player.isSneaking()) {
			ItemStack filter = this.getFilter(stack, player);
			if(filter != null) {
				ArmorUtil.removeFilter(stack);
				if(!player.inventory.addItemStackToInventory(filter)) {
					player.dropItem(filter, true);
				}
				return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.SUCCESS, stack);
			}
		}
		return super.onItemRightClick(world, player, hand);
	}
}
