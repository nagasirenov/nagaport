package com.hbm.items.armor;

import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.render.model.ModelBackTesla;
import com.hbm.tileentity.machine.TileEntityTesla;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderPlayerEvent;

public class ItemModTesla extends ItemArmorMod {

	private ModelBackTesla modelTesla;
	public List<double[]> targets = new ArrayList();
	
	public ItemModTesla() {
		super(ArmorModHandler.plate_only, false, true, false, false);
	}
    
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		list.add(TextFormatting.YELLOW + "Zaps nearby entities (requires full electric set)");
		list.add("");
		super.addInformation(itemstack, player, list, bool);
	}

	@SideOnly(Side.CLIENT)
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.YELLOW + stack.getDisplayName() + " (zaps nearby entities)");
	}
	
	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {
		
		if(!entity.world.isRemote && entity instanceof EntityPlayer && armor.getItem() instanceof ArmorFSBPowered && ArmorFSBPowered.hasFSBArmor((EntityPlayer)entity)) {
			targets = TileEntityTesla.zap(entity.world, entity.posX, entity.posY + 1.25, entity.posZ, 5, entity);
			
			if(targets != null && !targets.isEmpty() && entity.getRNG().nextInt(5) == 0) {
				armor.damageItem(1, entity);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void modRender(RenderPlayerEvent.SetArmorModel event, ItemStack armor) {

		if(this.modelTesla == null) {
			this.modelTesla = new ModelBackTesla();
		}
		
		RenderPlayer renderer = event.getRenderer();
		ModelBiped model = ((net.minecraft.client.model.ModelBiped) renderer.getMainModel());
		EntityPlayer player = event.getEntityPlayer();
		//EntityPlayer me = Minecraft.getMinecraft().thePlayer;

		modelTesla.isSneak = model.isSneak;
		
		float interp = event.getPartialRenderTick();
		float yawHead = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * interp;
		float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * interp;
		float yaw = yawHead - yawOffset;
		float yawWrapped = MathHelper.wrapDegrees(yawHead - yawOffset);
		float pitch = player.rotationPitch;
		
		modelTesla.render(event.getEntityPlayer(), 0.0F, 0.0F, yawWrapped, yaw, pitch, 0.0625F);
		
		/*GL11.glPushMatrix();
		
		GL11.glRotated(yawOffset, 0, -1, 0);

		Vec3d offset = new Vec3d(0, 0, -0.5);
		offset = offset.rotateYaw((float)Math.toRadians(yawOffset));
		
		double sx = player.posX + offset.x;
		double sy = player.posY - 0.25;
		double sz = player.posZ + offset.z;

		double x = me.posX - sx;
		double y = me.posY - sy;
		double z = me.posZ - sz;
		GL11.glTranslated(x, y, z);

		for(double[] target : targets) {

			double length = Math.sqrt(Math.pow(target[0] - sx, 2) + Math.pow(target[1] - sy, 2) + Math.pow(target[2] - sz, 2));
			BeamPronter.prontBeam(new Vec3d((target[0] - sx + offset.x * 1.5), target[1] - sy, -(target[2] - sz + offset.z * 1.5)), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x404040, 0x404040, (int) (player.worldObj.getTotalWorldTime() % 1000 + 1), (int) (length * 5), 0.125F, 2, 0.03125F);
		}
		
		GL11.glPopMatrix();*/
	}

}
