package com.hbm.tileentity.machine.rbmk;

import api.hbm.fluidmk2.FluidNetMK2;

import api.hbm.fluidmk2.FluidNode;
import api.hbm.fluidmk2.IFluidReceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.entity.effect.EntitySpear;
import com.hbm.entity.projectile.EntityRBMKDebris;
import com.hbm.entity.projectile.EntityRBMKDebris.DebrisType;
import com.hbm.handler.neutron.NeutronNodeWorld;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKType;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.IOverpressurable;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.ColumnType;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Compat;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import com.hbm.lib.ForgeDirection;
import org.lwjgl.opengl.GL11;

import java.util.*;

/**
 * Base class for all RBMK components, active or passive. Handles heat and the explosion sequence
 * @author hbm
 *
 */
public abstract class TileEntityRBMKBase extends TileEntityLoadedBase implements net.minecraft.util.ITickable {

	public double heat;

	public int reasimWater;
	public static final int maxWater = 16000;
	public int reasimSteam;
	public static final int maxSteam = 16000;
	public int craneIndicator;

	public static boolean explodeOnBroken = true;

	public boolean hasLid() {

		if(!isLidRemovable())
			return true;

		return this.getBlockMetadata() != RBMKBase.DIR_NO_LID.ordinal() + RBMKBase.offset;
	}

	public boolean isLidRemovable() {
		return true;
	}

	/**
	 * Approx melting point of steel
	 * Fuels often burn much hotter than this but it won't affect the column too much due to low diffusion
	 * @return
	 */
	public double maxHeat() {
		return 1500D;
	}

	/**
	 * Around the same for every component except boilers which do not have passive cooling.
	 * Requires the amount of connected neighbors to scale cooling
	 * @return
	 */
	public double passiveCooling(int neighbors) {
		double min = RBMKDials.getPassiveCoolingInner(this.world); //default: 0.1D
		double max = RBMKDials.getPassiveCooling(this.world); //default: 1.0D
		return min + (max - min) * ((4 - MathHelper.clamp(neighbors, 0, 4)) / 4D);
	}

	//necessary checks to figure out whether players are close enough to ensure that the reactor can be safely used
	public boolean shouldUpdate() {
		return true;
	}

	//unused
	public int trackingRange() {
		return 15;
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(this.craneIndicator > 0) this.craneIndicator--;

			this.world.profiler.startSection("rbmkBase_heat_movement");
			moveHeat();
			if(RBMKDials.getReasimBoilers(this.world)) {
				this.world.profiler.endStartSection("rbmkBase_reasim_boilers");
				boilWater();
			}

			this.networkPackNT(trackingRange());
		}
	}

	/**
	 * The ReaSim boiler dial causes all RBMK parts to behave like boilers
	 */
	private void boilWater() {

		if(heat < 100D)
			return;

		double heatConsumption = RBMKDials.getBoilerHeatConsumption(this.world);
		double availableHeat = (this.heat - 100) / heatConsumption;
		double availableWater = this.reasimWater;
		double availableSpace = maxSteam - this.reasimSteam;

		int processedWater = (int) Math.floor(BobMathUtil.min(availableHeat, availableWater, availableSpace) * MathHelper.clamp(RBMKDials.getReaSimBoilerSpeed(this.world), 0D, 1D));

		if(processedWater <= 0) return;

		this.reasimWater -= processedWater;
		this.reasimSteam += processedWater;
		this.heat -= processedWater * heatConsumption;
	}

	public static final ForgeDirection[] neighborDirs = new ForgeDirection[] {
			ForgeDirection.NORTH,
			ForgeDirection.EAST,
			ForgeDirection.SOUTH,
			ForgeDirection.WEST
	};

	protected TileEntityRBMKBase[] neighborCache = new TileEntityRBMKBase[4];

	/**
	 * Moves heat to neighboring parts, if possible, in a relatively fair manner
	 */
	private void moveHeat() {

		boolean reasim = RBMKDials.getReasimBoilers(this.world);

		List<TileEntityRBMKBase> rec = new ArrayList<>();
		rec.add(this);
		double heatTot = this.heat;
		int waterTot = this.reasimWater;
		int steamTot = this.reasimSteam;

		int index = 0;
		for(ForgeDirection dir : neighborDirs) {

			if(neighborCache[index] != null && neighborCache[index].isInvalid())
				neighborCache[index] = null;

			if(neighborCache[index] == null) {
				TileEntity te = Compat.getTileStandard(this.world, this.pos.getX() + dir.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ);

				if(te instanceof TileEntityRBMKBase) {
					TileEntityRBMKBase base = (TileEntityRBMKBase) te;
					neighborCache[index] = base;
				}
			}

			index++;
		}

		for(TileEntityRBMKBase base : neighborCache) {

			if(base != null) {
				rec.add(base);
				heatTot += base.heat;
				if(reasim) {
					waterTot += base.reasimWater;
					steamTot += base.reasimSteam;
				}
			}
		}

		int members = rec.size();
		double stepSize = RBMKDials.getColumnHeatFlow(this.world);

		if(members > 1) {

			double targetHeat = heatTot / (double)members;

			int tWater = waterTot / members;
			int rWater = waterTot % members;
			int tSteam = steamTot / members;
			int rSteam = steamTot % members;

			for(TileEntityRBMKBase rbmk : rec) {
				double delta = targetHeat - rbmk.heat;
				rbmk.heat += delta * stepSize;

				//set to the averages, rounded down
				if(reasim) {
					rbmk.reasimWater = tWater;
					rbmk.reasimSteam = tSteam;
				}
			}

			//add the modulo to make up for the losses coming from rounding
			if(reasim) {
				this.reasimWater += rWater;
				this.reasimSteam += rSteam;
			}

			this.markDirty();
		}

		this.world.profiler.endStartSection("rbmkBase_rpassive_cooling");
		coolPassively(members - 1);
		this.world.profiler.endSection();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		NeutronNodeWorld.removeNode(this.world, new BlockPos(this)); // woo-fucking-hoo!!!
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		NeutronNodeWorld.removeNode(this.world, new BlockPos(this)); // woo-fucking-hoo!!!
	}

	@Override
	public void markDirty() {

		if(this.world != null) {
			this.world.markChunkDirty(this.pos, this);
		}
	}

	protected void coolPassively(int neighbors) {
		this.heat -= this.passiveCooling(neighbors);
		if(heat < 20) heat = 20D;
	}

	public RBMKType getRBMKType() {
		return RBMKType.OTHER;
	}

	protected static boolean diag = false;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		if(!diag) {
			super.readFromNBT(nbt);
		}

		this.heat = nbt.getDouble("heat");
		this.reasimWater = nbt.getInteger("reasimWater");
		this.reasimSteam = nbt.getInteger("reasimSteam");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		if(!diag) {
			super.writeToNBT(nbt);
		}

		nbt.setDouble("heat", this.heat);
		nbt.setInteger("reasimWater", this.reasimWater);
		nbt.setInteger("reasimSteam", this.reasimSteam);
		return nbt;
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeDouble(this.heat);
		buf.writeInt(this.reasimWater);
		buf.writeInt(this.reasimSteam);
		buf.writeByte((byte) this.craneIndicator);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.heat = buf.readDouble();
		this.reasimWater = buf.readInt();
		this.reasimSteam = buf.readInt();
		this.craneIndicator = buf.readByte();
	}

	public void getDiagData(NBTTagCompound nbt) {
		diag = true;
		this.writeToNBT(nbt);
		diag = false;
	}

	@SuppressWarnings("unchecked")
	@SideOnly(Side.CLIENT)
	public static void diagnosticPrintHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {

		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution resolution = event.getResolution();

		RBMKBase rbmk = (RBMKBase) com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();
		int[] pos = rbmk.findCore(world, x, y, z);

		if(pos == null)
			return;

		TileEntityRBMKBase te = (TileEntityRBMKBase) world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
		NBTTagCompound flush = new NBTTagCompound();
		te.getDiagData(flush);
		Set<String> keys = flush.getKeySet();

		GL11.glPushMatrix();

		int pX = resolution.getScaledWidth() / 2 + 8;
		int pZ = resolution.getScaledHeight() / 2;

		List<String> exceptions = new ArrayList<>();
		exceptions.add("x");
		exceptions.add("y");
		exceptions.add("z");
		exceptions.add("items");
		exceptions.add("id");
		exceptions.add("muffled");

		//Keep the title unlocalized is cool.
		String title = "Dump of Ordered Data Diagnostic (DODD)";
		mc.fontRenderer.drawString(title, pX + 1, pZ - 19, 0x006000);
		mc.fontRenderer.drawString(title, pX, pZ - 20, 0x00FF00);

		mc.fontRenderer.drawString(I18nUtil.resolveKey(rbmk.getTranslationKey() + ".name"), pX + 1, pZ - 9, 0x606000);
		mc.fontRenderer.drawString(I18nUtil.resolveKey(rbmk.getTranslationKey() + ".name"), pX, pZ - 10, 0xffff00);

		String[] ents = new String[keys.size()];
		keys.toArray(ents);
		Arrays.sort(ents);

		for(String key : ents) {

			if(exceptions.contains(key))
				continue;
			String value = flush.getTag(key).toString();
			//No...’d‘ doesn't refer to "day" and ‘s’ doesn't refer to "second". Meaningless.
			if (!value.isEmpty()) {
				char lastChar = value.charAt(value.length() - 1);
				if (lastChar == 'd' || lastChar == 's' || lastChar == 'b') {
					value = value.substring(0, value.length() - 1);
				}
			}
			mc.fontRenderer.drawString(I18nUtil.resolveKey("tile.rbmk.dodd." + key) + ": " + value, pX, pZ, 0xFFFFFF);
			pZ += 10;
		}

		GL11.glDisable(GL11.GL_BLEND);

		GL11.glPopMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
	}

	public void onOverheat() {

		for(int i = 0; i < 4; i++) {
			com.hbm.world.WorldBlockState.write(this.world, this.pos.getX(), this.pos.getY() + i, this.pos.getZ(), Blocks.LAVA.getStateFromMeta(0), 3);
		}
	}

	public void onMelt(int reduce) {

		standardMelt(reduce);

		if(this.getBlockMetadata() == RBMKBase.DIR_NORMAL_LID.ordinal() + RBMKBase.offset)
			spawnDebris(DebrisType.LID);
	}

	protected void standardMelt(int reduce) {

		int h = RBMKDials.getColumnHeight(this.world);
		reduce = MathHelper.clamp(reduce, 1, h);

		if(this.world.rand.nextInt(3) == 0)
			reduce++;

		for(int i = h; i >= 0; i--) {

			if(i <= h + 1 - reduce) {

				if(reduce > 1 && i == h + 1 - reduce) {
					com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, this.pos.getX(), this.pos.getY() + i, this.pos.getZ(), ModBlocks.pribris_burning);
				} else {
					com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, this.pos.getX(), this.pos.getY() + i, this.pos.getZ(), ModBlocks.pribris);
				}

			} else {
				com.hbm.world.WorldBlockState.write(this.world, this.pos.getX(), this.pos.getY() + i, this.pos.getZ(), Blocks.AIR.getStateFromMeta(0), 3);
			}
			com.hbm.world.WorldBlockState.markBlockForUpdate(this.world, this.pos.getX(), this.pos.getY() + i, this.pos.getZ());
		}
	}

	protected void spawnDebris(DebrisType type) {

		EntityRBMKDebris debris = new EntityRBMKDebris(this.world, this.pos.getX() + 0.5D, this.pos.getY() + 4D, this.pos.getZ() + 0.5D, type);
		debris.motionX = this.world.rand.nextGaussian() * 0.25D;
		debris.motionZ = this.world.rand.nextGaussian() * 0.25D;
		debris.motionY = 0.25D + this.world.rand.nextDouble() * 1.25D;

		if(type == DebrisType.LID) {
			debris.motionX *= 0.5D;
			debris.motionY += 0.5D;
			debris.motionZ *= 0.5D;
		}

		this.world.spawnEntity(debris);
	}

	public static HashSet<TileEntityRBMKBase> columns = new HashSet<>();
	public static HashSet<FluidNetMK2> pipes = new HashSet<>();

	//assumes that !worldObj.isRemote
	@SuppressWarnings("unchecked")
	public void meltdown() {

		RBMKBase.dropLids = false;

		columns.clear();
		pipes.clear();
		getFF(this.pos.getX(), this.pos.getY(), this.pos.getZ());

		int minX = this.pos.getX();
		int maxX = this.pos.getX();
		int minZ = this.pos.getZ();
		int maxZ = this.pos.getZ();

		//set meltdown bounds
		for(TileEntityRBMKBase rbmk : columns) {

			if(rbmk.getPos().getX() < minX)
				minX = rbmk.getPos().getX();
			if(rbmk.getPos().getX() > maxX)
				maxX = rbmk.getPos().getX();
			if(rbmk.getPos().getZ() < minZ)
				minZ = rbmk.getPos().getZ();
			if(rbmk.getPos().getZ() > maxZ)
				maxZ = rbmk.getPos().getZ();
		}

		for(TileEntityRBMKBase rbmk : columns) {

			int distFromMinX = rbmk.getPos().getX() - minX;
			int distFromMaxX = maxX - rbmk.getPos().getX();
			int distFromMinZ = rbmk.getPos().getZ() - minZ;
			int distFromMaxZ = maxZ - rbmk.getPos().getZ();

			int minDist = Math.min(distFromMinX, Math.min(distFromMaxX, Math.min(distFromMinZ, distFromMaxZ)));

			rbmk.onMelt(minDist + 1);
		}

		for(TileEntityRBMKBase rbmk : columns) {

			if(rbmk instanceof TileEntityRBMKRod && com.hbm.world.WorldBlockState.read(this.world, rbmk.getPos().getX(), rbmk.getPos().getY(), rbmk.getPos().getZ()).getBlock() == ModBlocks.corium_block) {

				for(int x = rbmk.getPos().getX() - 1; x <= rbmk.getPos().getX() + 1; x ++) {
					for(int y = rbmk.getPos().getY() - 1; y <= rbmk.getPos().getY() + 1; y ++) {
						for(int z = rbmk.getPos().getZ() - 1; z <= rbmk.getPos().getZ() + 1; z ++) {

							Block b = com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock();

							if(this.world.rand.nextInt(3) == 0 && (b == ModBlocks.pribris || b == ModBlocks.pribris_burning)) {

								if(RBMKBase.digamma)
									com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, x, y, z, ModBlocks.pribris_digamma);
								else
									com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, x, y, z, ModBlocks.pribris_radiating);
							}
						}
					}
				}
			}
		}

		/* Hanlde overpressure event */
		if(RBMKDials.getOverpressure(this.world) && !pipes.isEmpty()) {
			HashSet<FluidNode> pipeBlocks = new HashSet<>();
			HashSet<Map.Entry<IFluidReceiverMK2, Long>> pipeReceivers = new HashSet<>();

			//unify all parts into single sets to prevent redundancy
			pipes.forEach(x -> {
				pipeBlocks.addAll(x.links);
				pipeReceivers.addAll(x.receiverEntries.entrySet());
			});

			int count = 0;
			int max = Math.min(pipeBlocks.size() / 5, 100);
			Iterator<FluidNode> itPipes = pipeBlocks.iterator();
			Iterator<Map.Entry<IFluidReceiverMK2, Long>> itReceivers = pipeReceivers.iterator();

			while(itPipes.hasNext() && count < max) {
				FluidNode node = itPipes.next();
				for (BlockPos pos : node.positions) {
					int x = pos.getX(), y = pos.getY(), z = pos.getZ();
					if (this.world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y ,z)) != null) {
						com.hbm.world.WorldBlockState.write(this.world, x, y, z, Blocks.AIR.getStateFromMeta(0), 3);
					}
				}
				count++;
			}

			while(itReceivers.hasNext()) {
				Map.Entry<IFluidReceiverMK2, Long> e = itReceivers.next();
				IFluidReceiverMK2 con = e.getKey();
				if(con instanceof TileEntity) {
					TileEntity tile = (TileEntity) con;
					if(con instanceof IOverpressurable) {
						((IOverpressurable) con).explode(this.world, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
					} else {
						com.hbm.world.WorldBlockState.write(this.world, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), Blocks.AIR.getStateFromMeta(0), 3);
						this.world.newExplosion(null, tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5, 5F, false, false);
					}
				}
			}
		}

		int smallDim = Math.min(maxX - minX, maxZ - minZ);
		int avgX = minX + (maxX - minX) / 2;
		int avgZ = minZ + (maxZ - minZ) / 2;

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "rbmkmush");
		data.setFloat("scale", smallDim);
		PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, avgX + 0.5, this.pos.getY() + 1, avgZ + 0.5), new TargetPoint(this.world.provider.getDimension(),avgX + 0.5, this.pos.getY() + 1, avgZ + 0.5, 250));
		MainRegistry.proxy.effectNT(data);

		com.hbm.sound.WorldSoundEffects.play(this.world, avgX + 0.5, this.pos.getY() + 1, avgZ + 0.5, "hbm:block.rbmk_explosion", 50.0F, 1.0F);

		List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class,
				new AxisAlignedBB(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5).expand(50, 50, 50));

		for(EntityPlayer player : players) {
			com.hbm.advancement.NtmAdvancements.award(player, MainRegistry.achRBMKBoom);
		}

		if(RBMKBase.digamma) {
			EntitySpear spear = new EntitySpear(this.world);
			spear.posX = avgX + 0.5;
			spear.posZ = avgZ + 0.5;
			spear.posY = this.pos.getY() + 100;
			this.world.spawnEntity(spear);
		}

		RBMKBase.dropLids = true;
		RBMKBase.digamma = false;

		columns.clear();
		pipes.clear();
	}

	private void getFF(int x, int y, int z) {

		TileEntity te = Compat.getTileStandard(this.world, x, y, z);

		if(te instanceof TileEntityRBMKBase) {

			TileEntityRBMKBase rbmk = (TileEntityRBMKBase) te;

			if(!columns.contains(rbmk)) {
				columns.add(rbmk);
				getFF(x + 1, y, z);
				getFF(x - 1, y, z);
				getFF(x, y, z + 1);
				getFF(x, y, z - 1);
			}
		}
	}

	public boolean isModerated() {
		return false;
	}

	public abstract ColumnType getConsoleType();

	public NBTTagCompound getNBTForConsole() {
		return null;
	}

	public static List<String> getFancyStats(NBTTagCompound nbt) {
		return null;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.pos.getX() + 1, this.pos.getY() + 17, this.pos.getZ() + 1);
	}
}
