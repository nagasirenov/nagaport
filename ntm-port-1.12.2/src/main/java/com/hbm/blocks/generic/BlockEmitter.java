package com.hbm.blocks.generic;

import java.awt.Color;
import java.util.List;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toclient.AuxParticlePacketNT;

import api.hbm.block.IToolable;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockEmitter extends BlockContainer implements IToolable, ITooltipProvider {

	public BlockEmitter() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityEmitter();
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {

		int l = BlockPistonBase.determineOrientation(world, x, y, z, player);
		world.setBlockMetadataWithNotify(x, y, z, l, 2);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int i, float fx, float fy, float fz) {

		if(world.isRemote)
			return true;

		TileEntityEmitter te = (TileEntityEmitter)world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null) {

			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof ItemDye) {
				te.color = ItemDye.field_150922_c[com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItemDamage()];
				te.markDirty();
				world.markBlockForUpdate(x, y, z);
				com.hbm.util.NativeHeldItem.getMainHandNullable(player).stackSize--;
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		TileEntityEmitter te = (TileEntityEmitter)world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(tool == ToolType.SCREWDRIVER) {
			te.girth += 0.125F;
			te.markDirty();
			return true;
		}

		if(tool == ToolType.DEFUSER) {
			te.girth -= 0.125F;
			if(te.girth < 0.125F) te.girth = 0.125F;
			te.markDirty();
			return true;
		}

		if(tool == ToolType.HAND_DRILL) {
			te.effect = (te.effect + 1) % te.effectCount;
			te.markDirty();
			return true;
		}

		return false;
	}

	public static class TileEntityEmitter extends TileEntityLoadedBase implements net.minecraft.util.ITickable {

		public static final int range = 100;
		public int color;
		public int beam;
		public float girth = 0.5F;
		public int effect = 0;
		public static final int effectCount = 5;

		@Override
		public void update() {

			if(!this.world.isRemote) {

				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());

				if(this.world.getTotalWorldTime() % 20 == 0) {
					for(int i = 1; i <= range; i++) {

						beam = i;

						int x = this.pos.getX() + dir.offsetX * i;
						int y = this.pos.getY() + dir.offsetY * i;
						int z = this.pos.getZ() + dir.offsetZ * i;

						Block b = com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock();
						if(b.isBlockSolid(this.world, x, y, z, dir.ordinal())) {
							break;
						}
					}
				}

				if(effect == 4 && beam > 0) {

					if(this.world.getTotalWorldTime() % 5 == 0) {
						double x = (int) (this.pos.getX() + dir.offsetX * (this.world.getTotalWorldTime() / 5L) % beam) + 0.5;
						double y = (int) (this.pos.getY() + dir.offsetY * (this.world.getTotalWorldTime() / 5L) % beam) + 0.5;
						double z = (int) (this.pos.getZ() + dir.offsetZ * (this.world.getTotalWorldTime() / 5L) % beam) + 0.5;

						int prevColor = color;
						if(color == 0) {
							color = Color.HSBtoRGB(this.world.getTotalWorldTime() / 50.0F, 0.5F, 0.25F) & 16777215;
						}

						NBTTagCompound data = new NBTTagCompound();
						data.setString("type", "plasmablast");
						data.setFloat("r", ((float)((color & 0xff0000) >> 16)) / 256F);
						data.setFloat("g", ((float)((color & 0x00ff00) >> 8)) / 256F);
						data.setFloat("b", ((float)((color & 0x0000ff))) / 256F);
						data.setFloat("scale", girth * 5);

						if(this.getBlockMetadata() == 2) {
							data.setFloat("pitch", 90);
						}
						if(this.getBlockMetadata() == 3) {
							data.setFloat("pitch", -90);
						}
						if(this.getBlockMetadata() == 4) {
							data.setFloat("pitch", 90);
							data.setFloat("yaw", 90);
						}
						if(this.getBlockMetadata() == 5) {
							data.setFloat("pitch", -90);
							data.setFloat("yaw", 90);
						}

						PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z),
								new TargetPoint(this.world.provider.getDimension(), x, y, z, 100));

						color = prevColor;
					}
				}

				networkPackNT(150);

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
			this.color = nbt.getInteger("color");
			this.girth = nbt.getFloat("girth");
			this.effect = nbt.getInteger("effect");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setInteger("color", this.color);
			nbt.setFloat("girth", this.girth);
			nbt.setInteger("effect", this.effect);
			return nbt;
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

		@Override
		public void serialize(ByteBuf buf) {
			buf.writeInt(this.beam);
			buf.writeInt(this.color);
			buf.writeFloat(this.girth);
			buf.writeInt(this.effect);
		}

		@Override
		public void deserialize(ByteBuf buf) {
			this.beam = buf.readInt();
			this.color = buf.readInt();
			this.girth = buf.readFloat();
			this.effect = buf.readInt();
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(TextFormatting.GOLD + "Use screwdriver to widen beam");
		list.add(TextFormatting.GOLD + "Use defuser to narrow beam");
		list.add(TextFormatting.GOLD + "Use hand drill to cycle special effects");
		list.add(TextFormatting.GOLD + "Use dye to change color");
	}
}
