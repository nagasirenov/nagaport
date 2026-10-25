package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockUberConcrete extends BlockBase {

	public BlockUberConcrete() {
		super();
		this.setTickRandomly(true);
	}

	@SideOnly(Side.CLIENT)
	protected IIcon[] brokenIcons;

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		super.registerBlockIcons(iconRegister);
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":concrete_super");
		brokenIcons = new IIcon[4];

		for(int i = 0; i < 4; i++) {
			this.brokenIcons[i] = iconRegister.registerIcon(RefStrings.MODID + ":concrete_super_m" + i);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {

		if(meta == 15) return brokenIcons[3];
		if(meta == 14) return brokenIcons[2];
		if(meta > 11) return brokenIcons[1];
		if(meta > 9) return brokenIcons[0];

		return this.blockIcon;
	}

	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

		if(rand.nextInt(meta + 1) > 0)
			return;

		if(meta < 15) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, meta + 1, 3);
		} else {
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);

			if(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == Blocks.AIR) {
				{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.concrete_super_broken; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
				return;
			}

			List<Integer> sides = new ArrayList(); //i wish the fucking homunculus that made the standard list implementation the nastiest fucking diarrhea of his life
			Collections.addAll(sides, 2, 3, 4, 5);
			Collections.shuffle(sides);

			for(Integer i : sides) {
				ForgeDirection dir = ForgeDirection.getOrientation(i);
				if(com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y, z + dir.offsetZ).getBlock() == Blocks.AIR && com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y - 1, z + dir.offsetZ).getBlock() == Blocks.AIR) {
					EntityFallingBlock debris = new EntityFallingBlock(world, x + 0.5 + dir.offsetX, y + 0.5, z + 0.5 + dir.offsetZ, ModBlocks.concrete_super_broken.getDefaultState());
					debris.fallTime = 2;
					debris.shouldDropItem = false;
					debris.setHurtEntities(true);
					world.spawnEntity(debris);
					return;
				}
			}

			{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.concrete_super_broken; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
		}
	}
}
