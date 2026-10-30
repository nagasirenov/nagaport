package com.hbm.blocks.generic;

import java.util.List;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.ParticleUtil;

import api.hbm.block.IToolable;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class PartEmitter extends BlockContainer implements IToolable, ITooltipProvider {
	// 1.7.10 type 0 cube; BlockContainer would make it INVISIBLE in 1.12 (render-type parity audit).
	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}


	public PartEmitter() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityPartEmitter();
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		if(tool == ToolType.HAND_DRILL) {
			TileEntityPartEmitter te = (TileEntityPartEmitter) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			te.effect = (te.effect + 1) % te.effectCount;
			te.markDirty();
			return true;
		}

		return false;
	}

	public static class TileEntityPartEmitter extends TileEntity implements net.minecraft.util.ITickable {

		public static final int range = 150;
		public int effect = 0;
		public static final int effectCount = 4;

		@Override
		public void update() {

			if(!this.world.isRemote) {

				double x = this.pos.getX() + 0.5;
				double y = this.pos.getY() + 0.5;
				double z = this.pos.getZ() + 0.5;
				NBTTagCompound data = new NBTTagCompound();

				if(effect == 1) {
					ParticleUtil.spawnGasFlame(this.world, this.pos.getX() + this.world.rand.nextDouble(), this.pos.getY() + 4.5 + this.world.rand.nextDouble(), this.pos.getZ() + this.world.rand.nextDouble(), this.world.rand.nextGaussian() * 0.2, 0.1, this.world.rand.nextGaussian() * 0.2);
				}

				if(effect == 2) {
					data.setString("type", "tower");
					data.setFloat("lift", 5F);
					data.setFloat("base", 0.25F);
					data.setFloat("max", 5F);
					data.setInteger("life", 560 + this.world.rand.nextInt(20));
					data.setInteger("color",0x404040);
				}
				if(effect == 3) {
					data.setString("type", "tower");
					data.setFloat("lift", 0.5F);
					data.setFloat("base", 1F);
					data.setFloat("max", 10F);
					data.setInteger("life", 750 + this.world.rand.nextInt(250));

					x = this.pos.getX() + 0.5 + this.world.rand.nextDouble() * 3 - 1.5;
					y =  this.pos.getY() + 1;
					z = this.pos.getZ() + 0.5 + this.world.rand.nextDouble() * 3 - 1.5;

				}

				if(data.hasKey("type")) {
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), range));
				}
			}
		}

		@Override
		public SPacketUpdateTileEntity getUpdatePacket() {
			NBTTagCompound nbt = new NBTTagCompound();
			this.writeToNBT(nbt);
			return new SPacketUpdateTileEntity(this.getPos(), 0, nbt);
		}

		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.getNbtCompound());
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.effect = nbt.getInteger("effect");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setInteger("effect", this.effect);
			return nbt;
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(TextFormatting.GOLD + "Use hand drill to cycle special effects");
	}
}
