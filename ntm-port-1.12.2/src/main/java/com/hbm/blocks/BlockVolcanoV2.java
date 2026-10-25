package com.hbm.blocks;

import java.util.List;
import java.util.Random;

import com.hbm.lib.ModDamageSource;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.packet.toclient.BufPacket;
import com.hbm.tileentity.IBufPacketReceiver;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockVolcanoV2 extends BlockContainer {

	public BlockVolcanoV2 withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

	public BlockVolcanoV2(Material rock) {
		super(rock);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityLightningVolcano();
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	public static class TileEntityLightningVolcano extends TileEntity implements IBufPacketReceiver, net.minecraft.util.ITickable {

		public int chargetime;
		public float flashd;

		public TileEntityLightningVolcano() {
			// Quick pre-set to prevent it immediately firing on load
			chargetime = (new Random()).nextInt(400) + 100;
		}

		@Override
		public void update() {
			// flashd is used locally to render the spike, and on the server for timing
			if(chargetime > 0) {
				flashd = 0;
			} else {
				flashd += 0.3f;
				flashd = Math.min(100.0f, flashd + 0.3f * (100.0f - flashd) * 0.15f);
			}

			if(!this.world.isRemote) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());

				if(chargetime == 1) {
					double x = (int) (this.pos.getX() + dir.offsetX * (this.world.getTotalWorldTime() / 5L) % 1) + 0.5;
					double y = (int) (this.pos.getY() + dir.offsetY * (this.world.getTotalWorldTime() / 5L) % 1) + 20.5;
					double z = (int) (this.pos.getZ() + dir.offsetZ * (this.world.getTotalWorldTime() / 5L) % 1) + 0.5;

					NBTTagCompound data = new NBTTagCompound();
					data.setString("type", "plasmablast");
					data.setFloat("r", 1F);
					data.setFloat("g", 1F);
					data.setFloat("b", 1F);
					data.setFloat("scale", 10 * 4);
					data.setFloat("yaw", 90);

					PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, x, y, z), new TargetPoint(this.world.provider.getDimension(), x, y, z, 256));
					com.hbm.sound.VanillaWorldSoundEffects.play(this.world, x, y, z, "ambient.weather.thunder", 200, 0.8F + this.world.rand.nextFloat() * 0.2F);
					vapor();
				}

				// we only want to modify chargetime on the server, since it is synced to clients
				if(chargetime > 0) chargetime--;

				if(flashd >= 100) {
					chargetime = this.world.rand.nextInt(400) + 100;
				}

				PacketDispatcher.wrapper.sendToAllAround(new BufPacket(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 256));
			}
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			chargetime = nbt.getInteger("charge");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setInteger("charge", this.chargetime);
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
			buf.writeInt(chargetime);
		}

		@Override
		public void deserialize(ByteBuf buf) {
			chargetime = buf.readInt();
		}

		private void vapor() {
			List<Entity> entities = this.world.getEntitiesWithinAABB(Entity.class,
					new AxisAlignedBB(this.pos.getX() - 0.5, this.pos.getY() + 0.5, this.pos.getZ() - 0.5, this.pos.getX() + 1.5,
							this.pos.getY() + 60, this.pos.getZ() + 1.5));

			if(!entities.isEmpty()) {
				for(Entity e : entities) {

					if(e instanceof EntityLivingBase)
						if(e.attackEntityFrom(ModDamageSource.electricity, MathHelper.clamp(((EntityLivingBase) e).getMaxHealth() * 1F, (float) (3), (float) (20))))
							this.world.playSound(null, e.posX, e.posY, e.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:weapon.tesla"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
				}
			}
		}

	}

}
