package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.world.gen.nbt.INBTBlockTransformable;

import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDecoModel extends BlockEnumMulti implements INBTBlockTransformable {

	public BlockDecoModel(Material mat, Class<? extends Enum> theEnum, boolean multiName, boolean multiTexture) {
		super(mat, theEnum, multiName, multiTexture);
	}

	public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, META); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(META, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(META); }

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
    @Override public net.minecraft.block.state.BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, net.minecraft.util.EnumFacing side) {
        return net.minecraft.block.state.BlockFaceShape.UNDEFINED;
    }

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	//Did somebody say - pain?
			//Alright fuckers, looks like 2/b010 = North, 3/b011 = South, 4/b100 = West, 5/b101 = East for sides.
			//I'll just opt for something similar (0/b00 North, 1/b01 South, 2/b10 West, 3/b11 East)

	//Assumes meta is using the third and fourth bits.
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		int meta;

		if((i & 1) != 1)
			meta = i >> 1; //For North(b00>b00) and South(b10>b01), shift bits right by one
		else {
			if(i == 3)
				meta = 2; //For West(b11>b10), just set to 2
			else
				meta = 3; //For East(b01>b11), just set to 3
		}

		world.setBlockState(pos, getStateFromMeta((meta << 2) | stack.getItemDamage()), 2);
	}

	@Override
	public int damageDropped(int meta) {
		return meta & 3;
	}

	//These are separate because they have to be constant
	private float mnX = 0.0F; //min
	private float mnY = 0.0F;
	private float mnZ = 0.0F;
	private float mxX = 1.0F; //max
	private float mxY = 1.0F;
	private float mxZ = 1.0F;

	public BlockDecoModel setBlockBoundsTo(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		mnX = minX;
		mnY = minY;
		mnZ = minZ;
		mxX = maxX;
		mxY = maxY;
		mxZ = maxZ;

		return this;
	}

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        switch(getMetaFromState(state) >> 2) {
            case 0: return new AxisAlignedBB(1 - mxX, mnY, 1 - mxZ, 1 - mnX, mxY, 1 - mnZ);
            case 1: return new AxisAlignedBB(mnX, mnY, mnZ, mxX, mxY, mxZ);
            case 2: return new AxisAlignedBB(1 - mxZ, mnY, mnX, 1 - mnZ, mxY, mxX);
            default: return new AxisAlignedBB(mnZ, mnY, 1 - mxX, mxZ, mxY, 1 - mnX);
        }
    }

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		if(coordBaseMode == 0) return meta;
		//N: 0b00, S: 0b01, W: 0b10, E: 0b11
		int rot = meta >> 2;
		int type = meta & 3;

		switch(coordBaseMode) {
		default: //South
			break;
		case 1: //West
			if((rot & 3) < 2) //N & S can just have bits toggled
				rot = rot ^ 3;
			else //W & E can just have first bit set to 0
				rot = rot ^ 2;
			break;
		case 2: //North
			rot = rot ^ 1; //N, W, E & S can just have first bit toggled
			break;
		case 3: //East
			if((rot & 3) < 2)//N & S can just have second bit set to 1
				rot = rot ^ 2;
			else //W & E can just have bits toggled
				rot = rot ^ 3;
			break;
		}
		//genuinely like. why did i do that
		return (rot << 2) | type; //To accommodate for BlockDecoModel's shift in the rotation bits; otherwise, simply bit-shift right and or any non-rotation meta after
	}

}