package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.Random;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityZirnoxDestroyed;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class ZirnoxDestroyed extends BlockDummyable {

	public ZirnoxDestroyed(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {

		if(meta >= 12)
			return new TileEntityZirnoxDestroyed();
		if(meta >= 6)
			return new TileEntityProxyCombo(false, true, true);

		return null;
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
			return false;
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {

		Block block = com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock();

		if(block == Blocks.AIR) {
			if(rand.nextInt(10) == 0)
				world.setBlock(x, y + 1, z, ModBlocks.gas_meltdown);

		} else if(block == ModBlocks.foam_layer || block == ModBlocks.block_foam) {
			if(rand.nextInt(25) == 0) {
				int pos[] = this.findCore(world, x, y, z);

				if(pos != null) {
					TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

					if(te instanceof TileEntityZirnoxDestroyed)
						((TileEntityZirnoxDestroyed)te).onFire = false;
				}
			}
		}

		if(rand.nextInt(10) == 0 && com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == Blocks.AIR)
			world.setBlock(x, y + 1, z, ModBlocks.gas_meltdown);

		super.updateTick(world, x, y, z, rand);
	}

	@Override
	public int tickRate(World world) {

		return 100 + world.rand.nextInt(20);
	}

	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);

		if(!world.isRemote) {
			if(world.rand.nextInt(4) == 0) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "rbmkflame");
				data.setInteger("maxAge", 90);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x + 0.25 + world.rand.nextDouble() * 0.5, y + 1.75, z + 0.25 + world.rand.nextDouble() * 0.5), new TargetPoint(world.provider.getDimension(), x + 0.5, y + 1.75, z + 0.5, 75));
				MainRegistry.proxy.effectNT(data);
				com.hbm.sound.VanillaWorldSoundEffects.play(world, x + 0.5F, y + 0.5, z + 0.5, "fire.fire", 1.0F + world.rand.nextFloat(), world.rand.nextFloat() * 0.7F + 0.3F);
			}
		}

		world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, this.tickRate(world));
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState meta, Random rand, int fortune) {
		return null;
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(new ItemStack(ModBlocks.concrete_smooth, 6));
		drops.add(new ItemStack(ModItems.pipe, 4, Mats.MAT_STEEL.id));
		drops.add(new ItemStack(ModBlocks.steel_grate, 2));
		drops.add(new ItemStack(ModItems.debris_metal, 6));
		drops.add(new ItemStack(ModItems.debris_graphite, 2));
		drops.add(new ItemStack(ModItems.fallout, 4));
		return drops;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 2, 2, 2, 2,};
	}

	@Override
	public int getOffset() {
		return 2;
	}

	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
	}

}
