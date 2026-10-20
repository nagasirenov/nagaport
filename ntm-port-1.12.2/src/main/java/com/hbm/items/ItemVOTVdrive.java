package com.hbm.items;

import net.minecraft.client.util.ITooltipFlag;
import java.util.List;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.orbit.OrbitalStation;
import com.hbm.entity.missile.EntityRideableRocket;
import com.hbm.entity.missile.EntityRideableRocket.RocketState;
import com.hbm.lib.RefStrings;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class ItemVOTVdrive extends ItemEnumMulti {

	private IIcon[] overlays;

	public ItemVOTVdrive() {
		super(SolarSystem.Body.class, false, false);
		this.setMaxStackSize(1);
		this.canRepair = false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		super.addInformation(stack, world, list, flag);

		Destination destination = getDestination(stack);

		if(destination.body == SolarSystem.Body.ORBIT) {
			String identifier = stack.getTagCompound().getString("stationName");

			if(identifier.equals("")) identifier = "0x" + Integer.toHexString(new ChunkPos(destination.x, destination.z).hashCode()).toUpperCase();

			list.add("Destination: ORBITAL STATION");
			list.add("Station: " + identifier);

			if(world != null && world.provider.getDimension() != destination.body.getDimensionId()) {
				for(String s : I18nUtil.resolveKey("item.hard_drive_full.orbit.desc").split("\\$")) {
					list.add(TextFormatting.GOLD + s);
				}
			}

			return;
		}

		int processingLevel = world != null ? destination.body.getProcessingLevel(CelestialBody.getBody(world)) : -1;

		list.add("Destination: " + TextFormatting.AQUA + I18nUtil.resolveKey("body." + destination.body.name));

		if(destination.x == 0 && destination.z == 0) {
			list.add(TextFormatting.GOLD + "Needs destination coordinates!");
		} else if(!getProcessed(stack)) {
			// Display processing level info if not processed
			if(world != null) list.add("Process requirement: Level " + processingLevel);
			list.add(TextFormatting.GOLD + "Needs processing!");
			list.add("Target coordinates: " + destination.x + ", " + destination.z);
		} else {
			// Display destination info if processed
			list.add(TextFormatting.GREEN + "Processed!");
			list.add("Target coordinates: " + destination.x + ", " + destination.z);
		}

		if(world != null && world.provider.getDimension() == destination.body.getDimensionId()) {
			for(String s : I18nUtil.resolveKey("item.hard_drive_full.surface.desc").split("\\$")) {
				list.add(TextFormatting.GOLD + s);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		overlays = new IIcon[SolarSystem.Body.values().length];

		for(int i = 0; i < overlays.length; i++) {
			SolarSystem.Body body = SolarSystem.Body.values()[i];
			String name = body != SolarSystem.Body.ORBIT ? body.name : "orbit";
			overlays[i] = iconRegister.registerIcon(RefStrings.MODID + ":votv." + name);
		}

		itemIcon = iconRegister.registerIcon(RefStrings.MODID + ":votv_f"); // Base icon for unprocessed drives
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	public int getRenderPasses(int metadata) {
		return 2;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int meta, int pass) {
		if(pass == 0) return this.getIconFromDamage(meta);
		return this.overlays[meta];
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
		if(!this.isInCreativeTab(tab)) return;
		NBTTagCompound stackTag = new NBTTagCompound();
		stackTag.setInteger("x", 1);
		stackTag.setInteger("ax", 1);
		stackTag.setBoolean("Processed", true);
		for(int i = 0; i < theEnum.getEnumConstants().length; i++) {
			ItemStack stack = new ItemStack(this, 1, i);
			stack.setTagCompound(stackTag);
			list.add(stack);
		}
	}

	public static SolarSystem.Body getBody(ItemStack stack) {
		return SolarSystem.Body.values()[stack.getItemDamage() % SolarSystem.Body.values().length];
	}

	public static Destination getDestination(ItemStack stack) {
		if(stack == null) return null;

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		SolarSystem.Body body = getBody(stack);
		int x = stack.getTagCompound().getInteger("x");
		int z = stack.getTagCompound().getInteger("z");
		return new Destination(body, x, z);
	}

	public static Target getTarget(ItemStack stack, World world) {
		if(stack == null) {
			return new Target(null, false, false);
		}

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		Destination destination = getDestination(stack);

		if(destination.body == SolarSystem.Body.ORBIT) {
			if(world.isRemote) {
				CelestialBody body = CelestialBody.getBody(stack.getTagCompound().getInteger("sDim"));
				boolean hasStation = stack.getTagCompound().getBoolean("sHas");

				return new Target(body, true, hasStation);
			}

			OrbitalStation station = OrbitalStation.getStation(destination.x, destination.z);
			if(!station.hasStation) station.orbiting = CelestialBody.getBody(world);

			// The client can't get this information, so any time the server grabs it, serialize it to the itemstack
			stack.getTagCompound().setString("stationName", station.name);
			stack.getTagCompound().setInteger("sDim", station.orbiting.dimensionId);
			stack.getTagCompound().setBoolean("sHas", station.hasStation);

			return new Target(station.orbiting, true, station.hasStation);
		} else {
			return new Target(destination.body.getBody(), false, true);
		}
	}

	public static void setCoordinates(ItemStack stack, int x, int z) {
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("x", x);
		stack.getTagCompound().setInteger("z", z);
	}

	public static int getProcessingTier(ItemStack stack, CelestialBody from) {
		SolarSystem.Body body = getBody(stack);
		return body.getProcessingLevel(from);
	}

	public static boolean getProcessed(ItemStack stack) {
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		return stack.getTagCompound().getBoolean("Processed");
	}

	public static void setProcessed(ItemStack stack, boolean processed) {
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setBoolean("Processed", processed);
	}

	// Returns an area for the Stardar to draw, so the player can pick a safe spot to land
	public static Destination getApproximateDestination(ItemStack stack) {
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		SolarSystem.Body body = getBody(stack);
		if(!stack.getTagCompound().hasKey("ax") || !stack.getTagCompound().hasKey("az")) {
			stack.getTagCompound().setInteger("ax", itemRand.nextInt(SpaceConfig.maxProbeDistance * 2) - SpaceConfig.maxProbeDistance);
			stack.getTagCompound().setInteger("az", itemRand.nextInt(SpaceConfig.maxProbeDistance * 2) - SpaceConfig.maxProbeDistance);
		}
		int ax = stack.getTagCompound().getInteger("ax");
		int az = stack.getTagCompound().getInteger("az");
		return new Destination(body, ax, az);
	}

	public static void markCopied(ItemStack stack) {
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setBoolean("copied", true);
	}

	public static boolean wasCopied(ItemStack stack) {
		if(!stack.hasTagCompound()) return false;
		return stack.getTagCompound().getBoolean("copied");
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		boolean isProcessed = getProcessed(stack);
		boolean onDestination = world.provider.getDimension() == getDestination(stack).body.getDimensionId();

		// If we're on the body (or in creative), immediately process
		if(!isProcessed && (player.capabilities.isCreativeMode || onDestination)) {
			isProcessed = true;
			setProcessed(stack, true);
		}

		ItemStack newStack = stack;

		if(isProcessed && player.getRidingEntity() != null && player.getRidingEntity() instanceof EntityRideableRocket) {
			EntityRideableRocket rocket = (EntityRideableRocket) player.getRidingEntity();

			if(rocket.getRocket().stages.size() > 0 || world.provider.getDimension() == SpaceConfig.orbitDimension || rocket.isReusable()) {
				if(rocket.getState() == RocketState.LANDED || rocket.getState() == RocketState.AWAITING) {
					// Replace our held stack with the rocket drive and place our held drive into the rocket
					if(rocket.navDrive != null) {
						newStack = rocket.navDrive;
					} else {
						newStack.stackSize = 0;
					}

					rocket.navDrive = stack.copy();
					rocket.navDrive.setCount(1);

					if(!world.isRemote) {
						rocket.setState(RocketState.AWAITING);
					}

					world.playSoundEffect(player.posX, player.posY, player.posZ, "hbm:item.upgradePlug", 1.0F, 1.0F);
				}
			}
		}

		return newStack;
	}


	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fx, float fy, float fz) {
		Destination destination = getDestination(stack);
		if(destination.body == SolarSystem.Body.ORBIT) {
			if(world.provider.getDimension() == SpaceConfig.orbitDimension) return false;

			if(!world.isRemote) {
				OrbitalStation station = OrbitalStation.getStation(destination.x, destination.z);

				Destination target = new Destination(CelestialBody.getEnum(world), x, z);

				if(station.recallPod(target)) {
					player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "Recalling drop pod to coordinates: " + x + ", " + z));
				} else {
					player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "Could not recall drop pod from station!"));
				}
			}

			return true;
		}

		boolean onDestination = world.provider.getDimension() == destination.body.getDimensionId();
		if(!onDestination)
			return false;

		setCoordinates(stack, x, z);
		setProcessed(stack, true);

		if(!world.isRemote)
			player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "Set landing coordinates to: " + x + ", " + z));

		return true;
	}

	public static class Destination {

		public int x;
		public int z;
		public SolarSystem.Body body;

		public Destination(SolarSystem.Body body, int x, int z) {
			this.body = body;
			this.x = x;
			this.z = z;
		}

		public ChunkPos getChunk() {
			return new ChunkPos(x >> 4, z >> 4);
		}

	}

	public static class Target {

		public CelestialBody body;
		public boolean inOrbit;
		public boolean isValid;

		public Target(CelestialBody body, boolean inOrbit, boolean isValid) {
			this.body = body;
			this.inOrbit = inOrbit;
			this.isValid = isValid;
		}

	}

}
