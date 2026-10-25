package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.SoyuzLauncher;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntitySoyuzStruct extends TileEntity implements net.minecraft.util.ITickable {

	int age;

	@Override
	public void update() {

		if(this.world.isRemote)
			return;

		age++;

		if(age < 20)
			return;

		age = 0;

		/// CHECK PAD ///
		for(int i = -6; i <= 6; i++)
			for(int j = 3; j <= 4; j++)
				for(int k = -6; k <= 6; k++)
					if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.struct_launcher)
						return;

		for(int i = -1; i <= 1; i++)
			for(int j = 3; j <= 4; j++)
				for(int k = -8; k <= -7; k++)
					if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.struct_launcher)
						return;

		for(int i = -2; i <= 2; i++)
			for(int j = 3; j <= 4; j++)
				for(int k = 7; k <= 9; k++)
					if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.struct_launcher)
						return;

		for(int i = -2; i <= 2; i++)
			for(int k = 5; k <= 9; k++)
				if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + 51, this.pos.getZ() + k).getBlock() != ModBlocks.struct_launcher)
					return;

		for(int i = -1; i <= 1; i++)
			for(int k = -8; k <= -6; k++)
				if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + 38, this.pos.getZ() + k).getBlock() != ModBlocks.struct_launcher)
					return;

		/// CHECK LEGS ///
		for(int i = 3; i <= 6; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = 3; k <= 6; k++)
					if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.concrete &&
							com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.concrete_smooth)
						return;

		for(int i = -6; i <= -3; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = 3; k <= 6; k++)
					if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.concrete &&
					com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.concrete_smooth)
				return;

		for(int i = -6; i <= -3; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = -6; k <= -3; k++)
					if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.concrete &&
					com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.concrete_smooth)
				return;

		for(int i = 3; i <= 6; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = -6; k <= -3; k++)
					if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.concrete &&
					com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.concrete_smooth)
				return;

		for(int i = -1; i <= 1; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = -8; k <= -6; k++)
					if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.concrete &&
					com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.concrete_smooth)
				return;

		for(int i = -2; i <= 2; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = 5; k <= 9; k++)
					if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.concrete &&
					com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.concrete_smooth)
				return;

		/// CHECK SCAFFOLDING ///
		for(int i = -1; i <= 1; i++)
			for(int j = 5; j <= 50; j++)
				for(int k = 6; k <= 8; k++)
					if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k).getBlock() != ModBlocks.struct_scaffold)
						return;

		for(int j = 5; j <= 37; j++)
			if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY() + j, this.pos.getZ() - 7).getBlock() != ModBlocks.struct_scaffold)
				return;
		/// CHECKS COMPLETE ///

		/// DELETE SCAFFOLDING ///

		for(int i = -2; i <= 2; i++)
			for(int k = 5; k <= 9; k++)
				{ net.minecraft.world.World nativePlacementWorld = this.world; int nativePlacementX = this.pos.getX() + i; int nativePlacementY = this.pos.getY() + 51; int nativePlacementZ = this.pos.getZ() + k; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }

		for(int i = -1; i <= 1; i++)
			for(int k = -8; k <= -6; k++)
				{ net.minecraft.world.World nativePlacementWorld = this.world; int nativePlacementX = this.pos.getX() + i; int nativePlacementY = this.pos.getY() + 38; int nativePlacementZ = this.pos.getZ() + k; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }

		for(int i = -2; i <= 2; i++)
			for(int j = 0; j <= 2; j++)
				for(int k = 5; k <= 9; k++)
					{ net.minecraft.world.World nativePlacementWorld = this.world; int nativePlacementX = this.pos.getX() + i; int nativePlacementY = this.pos.getY() + j; int nativePlacementZ = this.pos.getZ() + k; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }

		for(int i = -1; i <= 1; i++)
			for(int j = 5; j <= 50; j++)
				for(int k = 6; k <= 8; k++)
					{ net.minecraft.world.World nativePlacementWorld = this.world; int nativePlacementX = this.pos.getX() + i; int nativePlacementY = this.pos.getY() + j; int nativePlacementZ = this.pos.getZ() + k; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }

		for(int j = 5; j <= 37; j++)
			{ net.minecraft.world.World nativePlacementWorld = this.world; int nativePlacementX = this.pos.getX(); int nativePlacementY = this.pos.getY() + j; int nativePlacementZ = this.pos.getZ() - 7; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }

		/// GENERATE LAUNCHER ///

		ForgeDirection dir = ForgeDirection.EAST;

		{ net.minecraft.world.World nativePlacementWorld = this.world; int nativePlacementX = this.pos.getX(); int nativePlacementY = this.pos.getY(); int nativePlacementZ = this.pos.getZ(); net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, this.pos.getX() , this.pos.getY() + SoyuzLauncher.height, this.pos.getZ(), ModBlocks.soyuz_launcher, dir.ordinal() + SoyuzLauncher.offset, 3);
		((SoyuzLauncher)ModBlocks.soyuz_launcher).fillSpace(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), dir, 0);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
