package com.hbm.blocks.bomb;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Level;

import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.projectile.EntityShrapnel;
import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.ExplosionNT.ExAttrib;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.handler.threading.PacketThreading;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class BlockVolcano extends BlockContainer implements ITooltipProvider, IBlockMulti {

	public BlockVolcano() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityVolcanoCore();
	}

	@Override
	public int getSubCount() {
		return 5;
	}

	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {

		for(int i = 0; i < 5; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {

		int meta = stack.getItemDamage();

		if(meta == META_SMOLDERING) {
			list.add(TextFormatting.GOLD + "SHIELD VOLCANO");
			return;
		}

		list.add(BlockVolcano.isGrowing(meta) ? (TextFormatting.RED + "DOES GROW") : (TextFormatting.DARK_GRAY + "DOES NOT GROW"));
		list.add(BlockVolcano.isExtinguishing(meta) ? (TextFormatting.RED + "DOES EXTINGUISH") : (TextFormatting.DARK_GRAY + "DOES NOT EXTINGUISH"));
	}

	public static final int META_STATIC_ACTIVE = 0;
	public static final int META_STATIC_EXTINGUISHING = 1;
	public static final int META_GROWING_ACTIVE = 2;
	public static final int META_GROWING_EXTINGUISHING = 3;
	public static final int META_SMOLDERING = 4;

	public static boolean isGrowing(int meta) {
		return meta == META_GROWING_ACTIVE || meta == META_GROWING_EXTINGUISHING;
	}

	public static boolean isExtinguishing(int meta) {
		return meta == META_STATIC_EXTINGUISHING || meta == META_GROWING_EXTINGUISHING;
	}

	public static class TileEntityVolcanoCore extends TileEntity implements net.minecraft.util.ITickable {

		private static List<ExAttrib> volcanoExplosion = Arrays.asList(new ExAttrib[] {ExAttrib.NODROP, ExAttrib.LAVA_V, ExAttrib.NOSOUND, ExAttrib.ALLMOD, ExAttrib.NOHURT});
		private static List<ExAttrib> volcanoRadExplosion = Arrays.asList(new ExAttrib[] {ExAttrib.NODROP, ExAttrib.LAVA_R, ExAttrib.NOSOUND, ExAttrib.ALLMOD, ExAttrib.NOHURT});

		public int volcanoTimer;

		@Override
		public void update() {

			if(!this.world.isRemote) {
				this.volcanoTimer++;

				if(this.volcanoTimer % 10 == 0) {
					//if that type has a vertical channel, blast it open and raise the magma
					if(this.hasVerticalChannel()) {
						this.blastMagmaChannel();
						this.raiseMagma();
					}

					double magmaChamber = this.magmaChamberSize();
					if(magmaChamber > 0) this.blastMagmaChamber(magmaChamber);

					Object[] melting = this.surfaceMeltingParams();
					if(melting != null) this.meltSurface((int)melting[0], (double)melting[1], (double)melting[2]);

					//self-explanatory
					if(this.isSpewing()) this.spawnBlobs();
					if(this.isSmoking()) this.spawnSmoke();

					//generates a 3x3x3 cube of lava
					this.surroundLava();
				}

				if(this.volcanoTimer >= this.getUpdateRate()) {
					this.volcanoTimer = 0;

					if(this.shouldGrow()) {
						this.world.setBlock(this.pos.getX(), this.pos.getY() + 1, this.pos.getZ(), this.getBlockType(), this.getBlockMetadata(), 3);
						this.world.setBlock(this.pos.getX(), this.pos.getY(), this.pos.getZ(), getLava());
						return;
					} else if(this.isExtinguishing()) {
						this.world.setBlock(this.pos.getX(), this.pos.getY(), this.pos.getZ(), getLava());
						return;
					}
				}
			}
		}

		public boolean isRadioacitve() {
			return this.getBlockType() == ModBlocks.volcano_rad_core;
		}

		protected Block getLava() {
			if(isRadioacitve()) return ModBlocks.rad_lava_block;
			return ModBlocks.volcanic_lava_block;
		}

		protected List<ExAttrib> getExpAttrb() {
			return this.isRadioacitve() ? this.volcanoRadExplosion : this.volcanoExplosion;
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.volcanoTimer = nbt.getInteger("timer");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setInteger("timer", this.volcanoTimer);
			return nbt;
		}

		private boolean shouldGrow() {
			return isGrowing() && this.pos.getY() < 200;
		}

		private boolean isGrowing() {
			int meta = this.getBlockMetadata();
			return meta == META_GROWING_ACTIVE || meta == META_GROWING_EXTINGUISHING;
		}

		private boolean isExtinguishing() {
			int meta = this.getBlockMetadata();
			return meta == META_STATIC_EXTINGUISHING || meta == META_GROWING_EXTINGUISHING;
		}

		private boolean isSmoking() {
			return this.getBlockMetadata() != META_SMOLDERING;
		}

		private boolean isSpewing() {
			return this.getBlockMetadata() != META_SMOLDERING;
		}

		private boolean hasVerticalChannel() {
			return this.getBlockMetadata() != META_SMOLDERING;
		}

		private double magmaChamberSize() {
			return this.getBlockMetadata() == META_SMOLDERING ? 15 : 0;
		}

		/* count per tick, radius, depth */
		private Object[] surfaceMeltingParams() {
			return this.getBlockMetadata() == META_SMOLDERING ? new Object[] {50, 50D, 10D} : null;
		}

		private int getUpdateRate() {
			switch(this.getBlockMetadata()) {
			case META_STATIC_EXTINGUISHING: return 60 * 60 * 20; //once per hour
			case META_GROWING_ACTIVE:
			case META_GROWING_EXTINGUISHING: return 60 * 60 * 20 / 250; //250x per hour
			default: return 10;
			}
		}

		/** Causes two magma explosions, one from bedrock to the core and one from the core to 15 blocks above. */
		private void blastMagmaChannel() {
			ExplosionNT explosion = new ExplosionNT(this.world, null, this.pos.getX() + 0.5, this.pos.getY() + this.world.rand.nextInt(15) + 1.5, this.pos.getZ() + 0.5, 7);
			explosion.addAllAttrib(getExpAttrb()).explode();
			ExplosionNT explosion2 = new ExplosionNT(this.world, null, this.pos.getX() + 0.5 + this.world.rand.nextGaussian() * 3, this.world.rand.nextInt(this.pos.getY() + 1), this.pos.getZ() + 0.5 + this.world.rand.nextGaussian() * 3, 10);
			explosion2.addAllAttrib(getExpAttrb()).explode();
		}

		/** Causes two magma explosions at a random position around the core, one at normal and one at half range. */
		private void blastMagmaChamber(double size) {

			for(int i = 0; i < 2; i++) {
				double dist = size / (double) (i + 1);
				ExplosionNT explosion = new ExplosionNT(this.world, null, this.pos.getX() + 0.5 + this.world.rand.nextGaussian() * dist, this.pos.getY() + 0.5 + this.world.rand.nextGaussian() * dist, this.pos.getZ() + 0.5 + this.world.rand.nextGaussian() * dist, 7);
				explosion.addAllAttrib(getExpAttrb()).explode();
			}
		}

		/** Randomly selects surface blocks and converts them into lava if solid or air if not solid. */
		private void meltSurface(int count, double radius, double depth) {

			for(int i = 0; i < count; i++) {
				int x = (int) Math.floor(this.pos.getX() + this.world.rand.nextGaussian() * radius);
				int z = (int) Math.floor(this.pos.getZ() + this.world.rand.nextGaussian() * radius);
				//gaussian distribution makes conversions more likely on the surface and rarer at the bottom
				int y = this.world.getHeightValue(x, z) + 1 - (int) Math.floor(Math.abs(this.world.rand.nextGaussian() * depth));

				net.minecraft.block.state.IBlockState nativeAirState = com.hbm.world.WorldBlockState.read(this.world, x, y, z);
				Block b = nativeAirState.getBlock();

				if(!b.isAir(nativeAirState, this.world, new net.minecraft.util.math.BlockPos(x, y, z)) && b.getExplosionResistance(null) < Blocks.OBSIDIAN.getExplosionResistance(null)) {
					//turn into lava if solid block, otherwise just break
					this.world.setBlock(x, y, z, b.isNormalCube(nativeAirState) ? this.getLava() : Blocks.AIR);
				}
			}
		}

		/** Increases the magma level in a small radius around the core. */
		private void raiseMagma() {

			int rX = this.pos.getX() - 10 + this.world.rand.nextInt(21);
			int rY = this.pos.getY() + this.world.rand.nextInt(11);
			int rZ = this.pos.getZ() - 10 + this.world.rand.nextInt(21);

			if(com.hbm.world.WorldBlockState.read(this.world, rX, rY, rZ).getBlock() == Blocks.AIR && com.hbm.world.WorldBlockState.read(this.world, rX, rY - 1, rZ).getBlock() == this.getLava())
				this.world.setBlock(rX, rY, rZ, this.getLava());
		}

		/** Creates a 3x3x3 lava sphere around the core. */
		private void surroundLava() {

			for(int i = -1; i <= 1; i++) {
				for(int j = -1; j <= 1; j++) {
					for(int k = -1; k <= 1; k++) {

						if(i != 0 || j != 0 || k != 0) {
							this.world.setBlock(this.pos.getX() + i, this.pos.getY() + j, this.pos.getZ() + k, this.getLava());
						}
					}
				}
			}
		}

		/** Spews specially tagged shrapnels which create volcanic lava and monoxide clouds. */
		private void spawnBlobs() {

			for(int i = 0; i < 3; i++) {
				EntityShrapnel frag = new EntityShrapnel(this.world);
				frag.setLocationAndAngles(this.pos.getX() + 0.5, this.pos.getY() + 1.5, this.pos.getZ() + 0.5, 0.0F, 0.0F);
				frag.motionY = 1D + this.world.rand.nextDouble();
				frag.motionX = this.world.rand.nextGaussian() * 0.2D;
				frag.motionZ = this.world.rand.nextGaussian() * 0.2D;
				if(this.isRadioacitve()) {
					frag.setRadVolcano(true);
				} else {
					frag.setVolcano(true);
				}
				this.world.spawnEntityInWorld(frag);
			}
		}

		/** I SEE SMOKE, AND WHERE THERE'S SMOKE THERE'S FIRE! */
		private void spawnSmoke() {
			NBTTagCompound dPart = new NBTTagCompound();
			dPart.setString("type", "vanillaExt");
			dPart.setString("mode", "volcano");
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(dPart, this.pos.getX() + 0.5, this.pos.getY() + 10, this.pos.getZ() + 0.5), new TargetPoint(this.world.provider.getDimension(), this.pos.getX() + 0.5, this.pos.getY() + 10, this.pos.getZ() + 0.5, 250));
		}
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		if(!world.isRemote) {
				if(GeneralConfig.enableExtendedLogging) {
				MainRegistry.logger.log(Level.INFO, "[BOMBPL]" + this.getLocalizedName() + " placed at " + x + " / " + y + " / " + z + "! " + "by "+ player.getCommandSenderName());
			}
		}
	}
}
