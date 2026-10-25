package com.hbm.items.tool;

import java.util.List;
import java.util.Locale;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;

import api.hbm.block.IToolable;
import api.hbm.block.IToolable.ToolType;
import api.hbm.fluidmk2.IFillableItem;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemBlowtorch extends Item implements IFillableItem {

	public ItemBlowtorch() {
		this.setMaxStackSize(1);
		this.setFull3D();
		this.setCreativeTab(MainRegistry.controlTab);

		ToolType.TORCH.register(new ItemStack(this));
	}

	@Override
	public Item setTranslationKey(String unlocalizedName) {
		super.setTranslationKey(unlocalizedName);
		com.hbm.render.item.NativeItemTextureRegistry.texture(this, RefStrings.MODID + ":"+ unlocalizedName);
		return this;
	}

	@Override
	public boolean acceptsFluid(FluidType type, ItemStack stack) {

		if(this == ModItems.blowtorch) return type == Fluids.GAS;
		if(this == ModItems.acetylene_torch) return type == Fluids.UNSATURATEDS || type == Fluids.OXYGEN;

		return false;
	}

	@Override
	public int tryFill(FluidType type, int amount, ItemStack stack) {

		if(!acceptsFluid(type, stack))
			return amount;

		int toFill = Math.min(amount, 50);
		toFill = Math.min(toFill, getMaxFill(type) - this.getFill(stack, type));
		this.setFill(stack, type, this.getFill(stack, type) + toFill);

		return amount - toFill;
	}

	public int getFill(ItemStack stack, FluidType type) {
		if(!stack.hasTagCompound()) {
			initNBT(stack);
		}

		//just in case
		String name = Fluids.toNameCompat(type);
		if(stack.getTagCompound().hasKey(name)) {
			int fill = stack.getTagCompound().getInteger(name);
			stack.getTagCompound().removeTag(name);
			stack.getTagCompound().setInteger(Integer.toString(type.getID()), fill);

			return fill;
		}

		return stack.getTagCompound().getInteger(Integer.toString(type.getID()));
	}

	public int getMaxFill(FluidType type) {
		if(type == Fluids.GAS) return 4_000;
		if(type == Fluids.UNSATURATEDS) return 8_000;
		if(type == Fluids.OXYGEN) return 16_000;

		return 0;
	}

	public void setFill(ItemStack stack, FluidType type, int fill) {
		if(!stack.hasTagCompound()) {
			initNBT(stack);
		}

		stack.getTagCompound().setInteger(Integer.toString(type.getID()), fill);
	}

	public void initNBT(ItemStack stack) {

		stack.setTagCompound(new NBTTagCompound());

		if(this == ModItems.blowtorch) {
			this.setFill(stack, Fluids.GAS, this.getMaxFill(Fluids.GAS));
		}
		if(this == ModItems.acetylene_torch) {
			this.setFill(stack, Fluids.UNSATURATEDS, this.getMaxFill(Fluids.UNSATURATEDS));
			this.setFill(stack, Fluids.OXYGEN, this.getMaxFill(Fluids.OXYGEN));
		}
	}

	public static ItemStack getEmptyTool(Item item) {
		ItemBlowtorch tool = (ItemBlowtorch) item;
		ItemStack stack = new ItemStack(item);

		if(item == ModItems.blowtorch) {
			tool.setFill(stack, Fluids.GAS, 0);
		}
		if(item == ModItems.acetylene_torch) {
			tool.setFill(stack, Fluids.UNSATURATEDS, 0);
			tool.setFill(stack, Fluids.OXYGEN, 0);
		}

		return stack;
	}


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fX, float fY, float fZ) {

		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();

		if(b instanceof IToolable) {

			if(this == ModItems.blowtorch) {
				if(this.getFill(stack, Fluids.GAS) < 250) return false;
			}

			if(this == ModItems.acetylene_torch) {
				if(this.getFill(stack, Fluids.UNSATURATEDS) < 20) return false;
				if(this.getFill(stack, Fluids.OXYGEN) < 10) return false;
			}

			if(((IToolable)b).onScrew(world, player, x, y, z, side, fX, fY, fZ, ToolType.TORCH)) {

				if(!world.isRemote) {

					if(this == ModItems.blowtorch) {
						this.setFill(stack, Fluids.GAS, this.getFill(stack, Fluids.GAS) - 250);
					}

					if(this == ModItems.acetylene_torch) {
						this.setFill(stack, Fluids.UNSATURATEDS, this.getFill(stack, Fluids.UNSATURATEDS) - 20);
						this.setFill(stack, Fluids.OXYGEN, this.getFill(stack, Fluids.OXYGEN) - 10);
					}

					player.inventoryContainer.detectAndSendChanges();

					NBTTagCompound dPart = new NBTTagCompound();
					dPart.setString("type", "tau");
					dPart.setByte("count", (byte) 10);
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(dPart, x + fX, y + fY, z + fZ), new TargetPoint(world.provider.getDimension(), x, y, z, 50));
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) > 0;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {

		double frac = 0D;

		if(this == ModItems.blowtorch) {
			frac = (double) this.getFill(stack, Fluids.GAS) / (double) this.getMaxFill(Fluids.GAS);
		}

		if(this == ModItems.acetylene_torch) {
			frac = Math.min(
					(double) this.getFill(stack, Fluids.UNSATURATEDS) / (double) this.getMaxFill(Fluids.UNSATURATEDS),
					(double) this.getFill(stack, Fluids.OXYGEN) / (double) this.getMaxFill(Fluids.OXYGEN)
					);
		}

		return 1 - frac;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {

		if(this == ModItems.blowtorch) {
			list.add(TextFormatting.YELLOW + getFillGauge(stack, Fluids.GAS));
		}
		if(this == ModItems.acetylene_torch) {
			list.add(TextFormatting.YELLOW + getFillGauge(stack, Fluids.UNSATURATEDS));
			list.add(TextFormatting.AQUA + getFillGauge(stack, Fluids.OXYGEN));
		}
	}

	@SideOnly(Side.CLIENT)
	private String getFillGauge(ItemStack stack, FluidType type) {
		return type.getLocalizedName() + ": " + String.format(Locale.US, "%,d", this.getFill(stack, type)) + " / " + String.format(Locale.US, "%,d", this.getMaxFill(type));
	}

	@Override public boolean providesFluid(FluidType type, ItemStack stack) { return false; }
	@Override public int tryEmpty(FluidType type, int amount, ItemStack stack) { return amount; }

	@Override
	public FluidType getFirstFluidType(ItemStack stack) {
		return null;
	}

	@Override
	public int getFill(ItemStack stack) {
		return 0;
	}
}
