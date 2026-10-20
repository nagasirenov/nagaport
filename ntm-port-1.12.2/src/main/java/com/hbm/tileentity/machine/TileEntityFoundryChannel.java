package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.FoundryChannel.FoundryNode;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.lib.Library;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.FoundryNetworkProvider;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityFoundryChannel extends TileEntityFoundryBase {

	public int nextUpdate;
	public int lastFlow = 0;

	protected FoundryNode node;

    /** The paired native block/TESR also draws housing during the damage stage. */
    @Override public boolean canRenderBreaking() { return true; }

	@Override
	public void update() {

		if(!this.world.isRemote) {
			initNode();

			if(this.node.type != null && this.amount == 0) {
				this.node.type = null;
			}

			if(this.type == null && this.amount != 0) {
				this.amount = 0;
			}

			nextUpdate--;

			if(nextUpdate <= 0 && this.amount > 0 && this.type != null) {

				boolean hasOp = false;
				nextUpdate = 5;

				List<Integer> ints = new ArrayList<Integer>() {{ add(2); add(3); add(4); add(5); }};
				Collections.shuffle(ints);
				if(lastFlow > 0) {
					ints.remove((Integer) this.lastFlow);
					ints.add(this.lastFlow);
				}

				for(Integer i : ints) {
					ForgeDirection dir = ForgeDirection.getOrientation(i);
					Block b = this.world.getBlockState(this.pos.add(dir.offsetX, 0, dir.offsetZ)).getBlock();

					if(b instanceof ICrucibleAcceptor && b != ModBlocks.foundry_channel) {
						ICrucibleAcceptor acc = (ICrucibleAcceptor) b;

						if(acc.canAcceptPartialFlow(this.world, this.pos.getX() + dir.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ, dir.getOpposite(), new MaterialStack(this.type, this.amount))) {
							MaterialStack left = acc.flow(this.world, this.pos.getX() + dir.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ, dir.getOpposite(), new MaterialStack(this.type, this.amount));
							if(left == null) {
								this.type = null;
								this.amount = 0;

								this.node.type = null;
							} else {
								this.amount = left.amount;
							}
							hasOp = true;
							break;
						}
					}
				}

				if(!hasOp) {
					for(Integer i : ints) {
						ForgeDirection dir = ForgeDirection.getOrientation(i);
						TileEntity b = this.world.getTileEntity(this.pos.add(dir.offsetX, 0, dir.offsetZ));

						if(b instanceof TileEntityFoundryChannel) {
							TileEntityFoundryChannel acc = (TileEntityFoundryChannel) b;

							if(acc.type == null || acc.type == this.type || acc.amount == 0) {
								acc.type = this.type;
								acc.initNode();
								acc.node.type = this.type;

								acc.lastFlow = dir.getOpposite().ordinal();

								if(this.world.rand.nextInt(5) == 0 || this.amount == 1) { //force swap operations with single quanta to keep them moving
									//1:4 chance that the fill states are simply swapped
									//this promotes faster spreading and prevents spread limits
									int buf = this.amount;
									this.amount = acc.amount;
									acc.amount = buf;

								} else {
									//otherwise, equalize the neighbors
									int diff = this.amount - acc.amount;

									if(diff > 0) {
										diff /= 2;
										this.amount -= diff;
										acc.amount += diff;
									}
								}
							}
						}
					}
				}
			}

			if(this.amount == 0) {
				this.lastFlow = 0;
				this.nextUpdate = 5;
			}
		}

		super.update();
	}

	protected void initNode() {
		if(this.node == null || this.node.expired) {

			this.node = (FoundryNode) UniNodespace.getNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), FoundryNetworkProvider.THE_PROVIDER);

			if(this.node == null || this.node.expired) {
				this.node = this.createNode();
				this.node.type = this.type;
				UniNodespace.createNode(this.world, this.node);
			}
		}
	}

	@Override
	public int getCapacity() {
		return MaterialShapes.INGOT.q(2);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.lastFlow = nbt.getByte("flow");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setByte("flow", (byte) this.lastFlow);
		return nbt;
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(!this.world.isRemote) {
			if(this.node != null) {
				UniNodespace.destroyNode(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), FoundryNetworkProvider.THE_PROVIDER);
			}
		}
	}

	public FoundryNode createNode() {
		TileEntity tile = (TileEntity) this;
		return new FoundryNode(FoundryNetworkProvider.THE_PROVIDER, new BlockPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ())).setConnections(
			new DirPos(tile.getPos().getX() + 1, tile.getPos().getY(), tile.getPos().getZ(), Library.POS_X),
			new DirPos(tile.getPos().getX() - 1, tile.getPos().getY(), tile.getPos().getZ(), Library.NEG_X),
			new DirPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ() + 1, Library.POS_Z),
			new DirPos(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ() - 1, Library.NEG_Z)
		);
	}

	@Override
	public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		if(this.node == null || !this.node.hasValidNet()) return false;
		for(Object o : this.node.net.links) {
			FoundryNode node = (FoundryNode) o;
			if(node.type != null && node.type != stack.material) {
				return false;
			}
		}

		return super.canAcceptPartialPour(world, x, y, z, dX, dY, dZ, side, stack);
	}

	@Override
	public MaterialStack flow(World world, int x, int y, int z, ForgeDirection side, MaterialStack stack) {
		if(this.node != null) this.node.type = stack.material;
		return super.flow(world, x, y, z, side, stack);
	}

	@Override
	public MaterialStack pour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		if(this.node != null) this.node.type = stack.material;
		return super.pour(world, x, y, z, dX, dY, dZ, side, stack);
	}

}
