package com.hbm.items.tool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public abstract class ItemStructureTool extends Item implements ILookOverlay {

	File file = new File(MainRegistry.configHbmDir, "structureOutput.txt");
	FileWriter writer;

	public void writeToFile(String message) {
		if(!GeneralConfig.enableDebugMode)
			return;

		try {
			if(!file.exists()) file.createNewFile();
			if(writer == null) writer = new FileWriter(file, true);

			writer.write(message);
			writer.flush();
		} catch(IOException e) {
			System.out.print("ItemStructureWand encountered an IOException!");
		}
	}


		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void addInformation(net.minecraft.item.ItemStack stack, net.minecraft.world.World worldIn, java.util.List<java.lang.String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
		addInformation(stack, (net.minecraft.entity.player.EntityPlayer) null, tooltip, flagIn.isAdvanced());
	}

public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		BlockPos anchor = this.getAnchor(stack);

		if(anchor == null)
			list.add(TextFormatting.RED + "No anchor set! Right click an anchor to get started.");

		if(GeneralConfig.enableDebugMode)
			list.add(TextFormatting.GREEN + "Will write to \"structureOutput.txt\" in hbmConfig.");
	}

	public static BlockPos getAnchor(ItemStack stack) {

		if(!stack.hasTagCompound()) {
			return null;
		}

		return new BlockPos(stack.getTagCompound().getInteger("anchorX"), stack.getTagCompound().getInteger("anchorY"), stack.getTagCompound().getInteger("anchorZ"));
	}

	public static void setAnchor(ItemStack stack, int x, int y, int z) {

		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}

		stack.getTagCompound().setInteger("anchorX", x);
		stack.getTagCompound().setInteger("anchorY", y);
		stack.getTagCompound().setInteger("anchorZ", z);
	}


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {

		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();

		if(b == ModBlocks.structure_anchor) {
			this.setAnchor(stack, x, y, z);
			return true;
		}

		if(this.getAnchor(stack) == null) {
			return false;
		}

		if(!this.dualUse() && world.isRemote) {
			this.doTheThing(stack, world, x, y, z);
		} else {

			if(!stack.getTagCompound().hasKey("x")) {
				stack.getTagCompound().setInteger("x", x);
				stack.getTagCompound().setInteger("y", y);
				stack.getTagCompound().setInteger("z", z);
			} else {
				if(world.isRemote)
					this.doTheThing(stack, world, x, y, z);
				stack.getTagCompound().removeTag("x");
				stack.getTagCompound().removeTag("y");
				stack.getTagCompound().removeTag("z");
			}
		}

		return true;
	}

	protected boolean dualUse() {
		return false;
	}

	protected abstract void doTheThing(ItemStack stack, World world, int x, int y, int z);

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		ItemStack stack = com.hbm.util.NativeHeldItem.getMainHandNullable(Minecraft.getMinecraft().player);
		List<String> text = new ArrayList();

		BlockPos anchor = getAnchor(stack);

		if(anchor == null) {
			text.add(TextFormatting.RED + "No Anchor");
		} else {

			int dX = x - anchor.getX();
			int dY = y - anchor.getY();
			int dZ = z - anchor.getZ();
			text.add(TextFormatting.YELLOW + "Position: " + dX + " / " + dY + " / " + dZ);

			if(this.dualUse() && stack.getTagCompound().hasKey("x")) {
				int sX = Math.abs(x - stack.getTagCompound().getInteger("x")) + 1;
				int sY = Math.abs(y - stack.getTagCompound().getInteger("y")) + 1;
				int sZ = Math.abs(z - stack.getTagCompound().getInteger("z")) + 1;
				text.add(TextFormatting.GOLD + "Selection: " + sX + " / " + sY + " / " + sZ);
			}
		}

		if(Minecraft.getMinecraft().player.isSneaking())
			text.add("B: " + com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock().getTranslationKey() + ", M: " + com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ));

		ILookOverlay.printGeneric(event, this.getItemStackDisplayName(stack), 0xffff00, 0x404000, text);
	}
}
