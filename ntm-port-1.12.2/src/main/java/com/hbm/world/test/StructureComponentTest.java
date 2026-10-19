package com.hbm.world.test;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.template.TemplateManager;

public class StructureComponentTest extends StructureComponent {
	
	public StructureComponentTest(int x, int z) {
		this.boundingBox = new StructureBoundingBox(x, 64, z, x + 4, 64 + 9, z + 4);
	}


	//write
	@Override
	protected void writeStructureToNBT(NBTTagCompound nbt) {
		
	}

	//read
	@Override
	protected void readStructureFromNBT(NBTTagCompound nbt, TemplateManager templateManager) {
		
	}

	@Override
	public boolean addComponentParts(World world, Random rand, StructureBoundingBox boundingbox) {
		System.out.println("aaaaaaa");
		this.fillWithBlocks(world, boundingbox, 0, 0, 0, 4, 9, 4, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
		return true;
	}

}
