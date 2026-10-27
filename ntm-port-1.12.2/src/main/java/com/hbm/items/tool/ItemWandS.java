package com.hbm.items.tool;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockWandStructure.TileEntityWandStructure;
import com.hbm.main.MainRegistry;
import com.hbm.util.PersistentRegistryRef;
import com.hbm.util.Tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemWandS extends Item {

	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		list.add("Creative-only item");
		list.add("\"Replication breeds decadence\"");
		list.add("(Saves an area defined by two right-clicks,");
		list.add("adds a block to the blacklist by crouch right-clicking!)");

		if(stack.getTagCompound() != null) {
			int px = stack.getTagCompound().getInteger("x");
			int py = stack.getTagCompound().getInteger("y");
			int pz = stack.getTagCompound().getInteger("z");

			if(px != 0 || py != 0 || pz != 0) {
				list.add(TextFormatting.AQUA + "From: " + px + ", " + py + ", " + pz);
			} else {
				list.add(TextFormatting.AQUA + "No start position set");
			}

			Set<Pair<Block, Integer>> blocks = getBlocks(stack);

			if(blocks.size() > 0) {
				list.add("Blacklist:");
				for(Pair<Block, Integer> block : blocks) {
					list.add(TextFormatting.RED + "- " + block.key.getTranslationKey());
				}
			}
		}
	}

	// why the fuck ye'd leave this whole thing obfuscated is beyond me

		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fx, float fy, float fz) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}

		if(player.isSneaking()) {
			Pair<Block, Integer> target = new Pair<Block, Integer>(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock(), com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ));
			Set<Pair<Block, Integer>> blocks = getBlocks(stack);

			if(blocks.contains(target)) {
				blocks.remove(target);
				if(world.isRemote) player.sendMessage(new TextComponentString("Removed from blacklist " + target.key.getTranslationKey()));
			} else {
				blocks.add(target);
				if(world.isRemote) player.sendMessage(new TextComponentString("Added to blacklist " + target.key.getTranslationKey()));
			}

			setBlocks(stack, blocks);

		} else {
			int px = stack.getTagCompound().getInteger("x");
			int py = stack.getTagCompound().getInteger("y");
			int pz = stack.getTagCompound().getInteger("z");

			if(px == 0 && py == 0 && pz == 0) {
				setPosition(stack, x, y, z);

				if(world.isRemote) player.sendMessage(new TextComponentString("First position set!"));
			} else {
				setPosition(stack, 0, 0, 0);

				int minX = Math.min(x, px);
				int minY = Math.min(y, py) - 1;
				int minZ = Math.min(z, pz);

				int sizeX = Math.abs(x - px) + 1;
				int sizeY = Math.abs(y - py) + 1;
				int sizeZ = Math.abs(z - pz) + 1;

				com.hbm.world.WorldBlockState.writeLegacyBlock(world, minX, minY, minZ, ModBlocks.wand_structure);

				TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(minX, minY, minZ));
				if (te instanceof TileEntityWandStructure) {
					TileEntityWandStructure structure = (TileEntityWandStructure) te;

					structure.sizeX = sizeX;
					structure.sizeY = sizeY;
					structure.sizeZ = sizeZ;

					structure.blacklist = getBlocks(stack);
				} else {
					if (world.isRemote)
						player.sendMessage(new TextComponentString("Could not add a structure block!"));
					return true;
				}

				if (world.isRemote)
					player.sendMessage(new TextComponentString("Structure block configured and added at: " + minX + ", " + minY + ", " + minZ));
			}
		}

		return true;
	}

	private void setPosition(ItemStack stack, int x, int y, int z) {
		stack.getTagCompound().setInteger("x", x);
		stack.getTagCompound().setInteger("y", y);
		stack.getTagCompound().setInteger("z", z);
	}

	private Set<Pair<Block, Integer>> getBlocks(ItemStack stack) {
		if(stack.getTagCompound() == null) {
			return new HashSet<>();
		}

		NBTTagList names = stack.getTagCompound().getTagList("blockNames", 8);
		int[] metas = stack.getTagCompound().getIntArray("metas");
		Set<Pair<Block, Integer>> blocks = new HashSet<>(names.tagCount());

		for(int i = 0; i < names.tagCount(); i++) {
			Block block = Block.getBlockFromName(names.getStringTagAt(i));
			if(block == null) {
				MainRegistry.logger.warn("Structure wand blacklist entry '" + names.getStringTagAt(i) + "' is not registered; entry skipped");
				continue;
			}
			blocks.add(new Pair<Block, Integer>(block, i < metas.length ? metas[i] : 0));
		}

		if(names.tagCount() == 0 && stack.getTagCompound().hasKey("blocks")) {
			MainRegistry.logger.warn("Structure wand blacklist uses legacy numeric block ids; they are unresolved and the raw NBT is preserved");
		}

		return blocks;
	}

	private void setBlocks(ItemStack stack, Set<Pair<Block, Integer>> blocks) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}

		NBTTagList names = new NBTTagList();
		int[] metas = new int[blocks.size()];
		int index = 0;

		for(Pair<Block, Integer> entry : blocks) {
			Block block = entry.getKey();
			if(block == null || block.getRegistryName() == null) continue;
			names.appendTag(new NBTTagString(block.getRegistryName().toString()));
			metas[index++] = entry.getValue();
		}

		stack.getTagCompound().setTag("blockNames", names);
		stack.getTagCompound().setIntArray("metas", java.util.Arrays.copyOf(metas, index));
		stack.getTagCompound().removeTag("blocks");
		stack.getTagCompound().setByte(PersistentRegistryRef.SCHEMA_KEY, (byte) PersistentRegistryRef.SCHEMA_VERSION);
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}

		if(player.isSneaking()) {
			stack.getTagCompound().removeTag("blocks");
			stack.getTagCompound().removeTag("blockNames");
			stack.getTagCompound().setIntArray("metas", new int[0]);

			if(world.isRemote) {
				player.sendMessage(new TextComponentString("Cleared blacklist"));
			}
		}

		return stack;
	}

}
