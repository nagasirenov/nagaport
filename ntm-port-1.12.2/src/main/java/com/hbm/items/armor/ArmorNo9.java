package com.hbm.items.armor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.lib.Library;
import com.hbm.render.model.ModelNo9;
import com.hbm.util.Tuple.Pair;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class ArmorNo9 extends ArmorModel implements IAttackHandler, IDamageHandler {

	protected ModelNo9 model;

	public ArmorNo9(ArmorMaterial armorMaterial, int armorType) {
		super(armorMaterial, armorType);
		this.setMaxDamage(0);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		list.add(TextFormatting.BLUE + "+0.5 DT");
		list.add(TextFormatting.YELLOW + "Lets you breathe coal, neat!");
	}

	@Override
	public void handleDamage(LivingHurtEvent event, ItemStack stack) {

		if(event.getSource().isUnblockable())
			return;

		event.setAmount((float) (event.getAmount() - (0.5F)));

		if(event.getAmount() < 0)
			event.setAmount(0);
	}

	@Override
	public void handleAttack(LivingAttackEvent event, ItemStack armor) {

		if(event.getSource().isUnblockable())
			return;

		if(event.getAmount() <= 0.5F) {
			event.setCanceled(true);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, net.minecraft.inventory.EntityEquipmentSlot armorSlot, ModelBiped defaultModel) {

		if(model == null) {
			model = new ModelNo9(0);
		}

		return model;
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack armor) {

		if(!world.isRemote) {
			if(!armor.hasTagCompound()) {
				armor.setTagCompound(new NBTTagCompound());
			}

			boolean turnOn = HbmPlayerProps.getData(player).enableHUD;
			boolean wasOn = armor.getTagCompound().getBoolean("isOn");

			if(turnOn && !wasOn) world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("fire.ignite"), net.minecraft.util.SoundCategory.PLAYERS, 1F, 1.5F);
			if(!turnOn && wasOn) world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("random.fizz"), net.minecraft.util.SoundCategory.PLAYERS, 0.5F, 2F);
			armor.getTagCompound().setBoolean("isOn", turnOn); // a crude way of syncing the "enableHUD" prop to other players is just by piggybacking off the NBT sync

			if(HbmLivingProps.getBlackLung(player) > HbmLivingProps.maxBlacklung * 0.9) {
				HbmLivingProps.setBlackLung(player, (int) (HbmLivingProps.maxBlacklung * 0.9));
			}

			if(HbmLivingProps.getBlackLung(player) >= HbmLivingProps.maxBlacklung * 0.25) {
				HbmLivingProps.setBlackLung(player, HbmLivingProps.getBlackLung(player) - 1);
			}
		}

		if(world.isRemote && world.getTotalWorldTime() % 2 == 0 && armor.hasTagCompound() && armor.getTagCompound().getBoolean("isOn")) {

			//originally it would have just been a bright aura like a torch, but that's boring
			/*int x = (int) Math.floor(player.posX);
			int y = (int) Math.floor(player.posY + player.eyeHeight);
			int z = (int) Math.floor(player.posZ);*/

			checkLights(world, false);
			float range = 50F;
			RayTraceResult mop = Library.rayTrace(player, range, 0F, false, true, false);

			if(mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
				Vec3d look = new Vec3d(player.posX - mop.hitVec.x, player.posY + player.getEyeHeight() - mop.hitVec.y, player.posZ - mop.hitVec.z);
				net.minecraft.util.EnumFacing side = mop.sideHit;
				int level = Math.min(15, (int) (25 - (look.length() * 25 / range)));
				lightUpRecursively(world, mop.getBlockPos().getX() + side.getXOffset(), mop.getBlockPos().getY() + side.getYOffset(), mop.getBlockPos().getZ() + side.getZOffset(), level);
				breadcrumb.clear();
			}
		}
	}

	public static Set<BlockPos> breadcrumb = new HashSet();
	public static void lightUpRecursively(World world, int x, int y, int z, int light) {
		if(light <= 0) return;
		BlockPos pos = new BlockPos(x, y, z);
		if(breadcrumb.contains(pos)) return;
		breadcrumb.add(pos);

		int existingLight = world.getLightFor(EnumSkyBlock.BLOCK, new net.minecraft.util.math.BlockPos(x, y, z));
		int occupancy = com.hbm.world.WorldBlockState.read(world, x, y, z).getLightOpacity();

		if(occupancy >= 255) return; // only block if it's fully blocking, light goes through semi-translucent blocks like it's air

		int newLight = Math.min(15, Math.max(existingLight, light));
		world.setLightFor(EnumSkyBlock.BLOCK, new net.minecraft.util.math.BlockPos(x, y, z), newLight);
		lightCheck.put(new Pair(world, pos), world.getTotalWorldTime() + 5);

		lightUpRecursively(world, x + 1, y, z, light - 1);
		lightUpRecursively(world, x - 1, y, z, light - 1);
		lightUpRecursively(world, x, y + 1, z, light - 1);
		lightUpRecursively(world, x, y - 1, z, light - 1);
		lightUpRecursively(world, x, y, z + 1, light - 1);
		lightUpRecursively(world, x, y, z - 1, light - 1);
	}

	public static HashMap<World, Long> lastChecks = new HashMap();
	public static HashMap<Pair<World, BlockPos>, Long> lightCheck = new HashMap();

	public static void checkLights(World world, boolean force) {
		Iterator it = lightCheck.entrySet().iterator();

		while(it.hasNext()) {
			Entry<Pair<World, BlockPos>, Long> entry = (Entry<Pair<World, BlockPos>, Long>) it.next();

			if(entry.getKey().getKey() == world && (world.getTotalWorldTime() > entry.getValue() || force)) {
				BlockPos pos = entry.getKey().getValue();
				world.checkLight(new net.minecraft.util.math.BlockPos(pos.getX(), pos.getY(), pos.getZ()));
				it.remove();
			}
		}

		lastChecks.put(world, world.getTotalWorldTime());
	}

	public static void updateWorldHook(World world) {
		if(world == null || !world.isRemote) return;
		Long last = lastChecks.get(world);

		if(last != null && last < world.getTotalWorldTime() + 15) {
			checkLights(world, false);
		}
	}

	// lighting data is saved by the server, not the client, so why would we need that??
	/*public static void unloadWorldHook(World world) {
		if(!world.isRemote) return;
		checkLights(world, true);
	}*/
}
