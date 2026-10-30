package com.hbm.blocks.gas;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.ArmorUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import com.hbm.lib.ForgeDirection;

public abstract class BlockGasBase extends Block {
	// 1.7.10 getRenderType() -1: gas is invisible, only particles are drawn.
	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
	}


	float red;
	float green;
	float blue;

	public BlockGasBase(float r, float g, float b) {
		super(ModBlocks.materialGas);
		this.setHardness(0.0F);
		this.setResistance(0.0F);
		this.lightOpacity = 0;
		this.red = r;
		this.green = g;
		this.blue = b;
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	public int getRenderType() {
		return -1;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess p_149668_1_, net.minecraft.util.math.BlockPos pos) {
		int p_149668_2_ = pos.getX(), p_149668_3_ = pos.getY(), p_149668_4_ = pos.getZ();
		return null;
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return null;
	}

	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing side) {
		return false;
	}

	@Override
	public boolean canCollideCheck(net.minecraft.block.state.IBlockState state, boolean p_149678_2_) {
		return false;
	}

	@Override
	public boolean isReplaceable(IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
		return true;
	}

	@Override
	public void onBlockAdded(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		if(!world.isRemote) {

			if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) == 0)
				world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, 10);
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ) != 0) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 0, 4);
			world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, 10);
		}
	}


		@Override
	public void updateTick(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, java.util.Random rand) {
		updateTick(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
	}

public void updateTick(World world, int x, int y, int z, Random rand) {

		if(!world.isRemote) {

			net.minecraftforge.fml.relauncher.ReflectionHelper.setPrivateValue(net.minecraft.world.World.class, world, false, "field_72999_e", "scheduledUpdatesAreImmediate"); //prevent recursive loop when some dumbass forgets to clean up immediate updating

			if(!tryMove(world, x, y, z, getFirstDirection(world, x, y, z)))
				if(!tryMove(world, x, y, z, getSecondDirection(world, x, y, z)))
					world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, getDelay(world));
		}
	}

	public abstract ForgeDirection getFirstDirection(World world, int x, int y, int z);

	public ForgeDirection getSecondDirection(World world, int x, int y, int z) {
		return getFirstDirection(world, x, y, z);
	}

	public boolean tryMove(World world, int x, int y, int z, ForgeDirection dir) {

		if(com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getBlock() == Blocks.AIR) {
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, this);
			world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ), this, getDelay(world));
			return true;
		}

		return false;
	}

	public int getDelay(World world) {
		return 2;
	}

	public ForgeDirection randomHorizontal(World world) {
		return ForgeDirection.getOrientation(world.rand.nextInt(4) + 2);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, world, pos, rand);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		EntityPlayer p = MainRegistry.proxy.me();
		if(ArmorUtil.checkArmorPiece(p, ModItems.ashglasses, 3) && this != ModBlocks.vacuum) {
			if(rand.nextInt(9)==1)
			{
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "vanillaExt");
				data.setString("mode", "cloud");
				data.setDouble("posX", x + 0.5);
				data.setDouble("posY", y + 0.5);
				data.setDouble("posZ", z + 0.5);
				data.setFloat("r", red);
				data.setFloat("g", green);
				data.setFloat("b", blue);
				MainRegistry.proxy.effectNT(data);
			}
		}
	}
}
