package com.hbm.tileentity.machine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.hbm.blocks.ModBlocks;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.blocks.generic.BlockTallPlant.EnumTallFlower;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.lib.ModDamageSource;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.fluid.IFluidStandardReceiver;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineAutosaw extends TileEntityLoadedBase implements IBufPacketReceiver, IFluidStandardReceiver, IFluidCopiable, net.minecraft.util.ITickable {

	private static final int MIN_DIST = 2;
	private static final int MAX_DIST = 9;

	private static final int FELL_HORIZONTAL_RANGE = 10;
	private static final int FELL_BFS_RADIUS = MAX_DIST + FELL_HORIZONTAL_RANGE;
	private static final int FELL_VERTICAL_RANGE = 32;
	private static final int FELL_MAX_BASE_DEPTH = FELL_VERTICAL_RANGE / 2;

	// 18-connectivity: 6 face-adjacent + 12 edge-adjacent (exactly one coord diff is 0)
	private static final int[][] EIGHTEEN_DIRS = {
		{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1},
		{1, 1, 0}, {1, -1, 0}, {-1, 1, 0}, {-1, -1, 0},
		{1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1},
		{0, 1, 1}, {0, 1, -1}, {0, -1, 1}, {0, -1, -1}
	};

	public static final HashSet<FluidType> acceptedFuels = new HashSet();

	static {
		acceptedFuels.add(Fluids.WOODOIL);
		acceptedFuels.add(Fluids.ETHANOL);
		acceptedFuels.add(Fluids.FISHOIL);
		acceptedFuels.add(Fluids.HEAVYOIL);
	}

	public FluidTank tank;

	public boolean isOn;
	public boolean isSuspended;
	private int forceSkip;
	public float syncYaw;
	public float rotationYaw;
	public float prevRotationYaw;
	public float syncPitch;
	public float rotationPitch;
	public float prevRotationPitch;

	// 0: searching, 1: extending, 2: retracting
	private int state = 0;

	private int turnProgress;

	public float spin;
	public float lastSpin;

	public TileEntityMachineAutosaw() {
		this.tank = new FluidTank(Fluids.WOODOIL, 100);
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(!isSuspended && this.world.getTotalWorldTime() % 20 == 0) {
				if(tank.getFill() > 0) {
					tank.setFill(tank.getFill() - 1);
					this.isOn = true;
				} else {
					this.isOn = false;
				}

				this.subscribeToAllAround(tank.getTankType(), this);
			}

			if(isOn && !isSuspended) {
				Vec3d pivot = new Vec3d(this.pos.getX() + 0.5, this.pos.getY() + 1.75, this.pos.getZ() + 0.5);
				com.hbm.util.Vec3NT upperArm = new com.hbm.util.Vec3NT(0, 0, -4);
				upperArm.rotateAroundXRad((float) Math.toRadians(80 - rotationPitch));
				upperArm.rotateAroundYRad(-(float) Math.toRadians(rotationYaw));
				com.hbm.util.Vec3NT lowerArm = new com.hbm.util.Vec3NT(0, 0, -4);
				lowerArm.rotateAroundXRad((float) -Math.toRadians(80 - rotationPitch));
				lowerArm.rotateAroundYRad(-(float) Math.toRadians(rotationYaw));
				Vec3d armTip = new Vec3d(0, 0, -2);
				armTip = armTip.rotateYaw(-(float) Math.toRadians(rotationYaw));

				double cX = pivot.x + upperArm.x + lowerArm.x + armTip.x;
				double cY = pivot.y;
				double cZ = pivot.z + upperArm.z + lowerArm.z + armTip.z;

				List<EntityLivingBase> affected = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(cX - 1, cY - 0.25, cZ - 1, cX + 1, cY + 0.25, cZ + 1));

				for(EntityLivingBase e : affected) {
					if(e.isEntityAlive() && e.attackEntityFrom(ModDamageSource.turbofan, 100)) {
						com.hbm.sound.VanillaWorldSoundEffects.play(this.world, e.posX, e.posY, e.posZ, "mob.zombie.woodbreak", 2.0F, 0.95F + this.world.rand.nextFloat() * 0.2F);
						int count = Math.min((int)Math.ceil(e.getMaxHealth() / 4), 250);
						NBTTagCompound data = new NBTTagCompound();
						data.setString("type", "vanillaburst");
						data.setInteger("count", count * 4);
						data.setDouble("motion", 0.1D);
						data.setString("mode", "blockdust");
						data.setInteger("block", Block.getIdFromBlock(Blocks.REDSTONE_BLOCK));
						PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, e.posX, e.posY + e.height * 0.5, e.posZ), new TargetPoint(e.dimension, e.posX, e.posY, e.posZ, 50));
					}
				}

				if(state == 0) {

					this.rotationYaw += 1;

					if(this.rotationYaw >= 360) {
						this.rotationYaw -= 360;
					}

					if(forceSkip > 0) {
						forceSkip--;
					} else {
						final double CUT_ANGLE = Math.toRadians(5);
						double rotationYawRads = Math.toRadians((rotationYaw + 270) % 360);

						outer:
						for(int dx = -MAX_DIST; dx <= MAX_DIST; dx++) {
							for(int dz = -MAX_DIST; dz <= MAX_DIST; dz++) {
								int sqrDst = dx * dx + dz * dz;

								if(sqrDst <= MIN_DIST * MIN_DIST || sqrDst > MAX_DIST * MAX_DIST)
									continue;

								double angle = Math.atan2(dz, dx);
								double relAngle = Math.abs(angle - rotationYawRads);
								relAngle = Math.abs((relAngle + Math.PI) % (2 * Math.PI) - Math.PI);

								if(relAngle > CUT_ANGLE) continue;

								int x = this.pos.getX() + dx;
								int y = this.pos.getY() + 1;
								int z = this.pos.getZ() + dz;

								Block b = com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock();
								if(!(b.getDefaultState().getMaterial() == Material.WOOD || b.getDefaultState().getMaterial() == Material.LEAVES || b.getDefaultState().getMaterial() == Material.PLANTS))
									continue;

								int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, x, y, z) );
								if(shouldIgnore(this.world, x, y, z, b, meta)) continue;

								state = 1;
								break outer;
							}
						}
					}
				}

				int hitY = (int) Math.floor(cY);
				int hitX0 = (int) Math.floor(cX - 0.5);
				int hitZ0 = (int) Math.floor(cZ - 0.5);
				int hitX1 = (int) Math.floor(cX + 0.5);
				int hitZ1 = (int) Math.floor(cZ + 0.5);

				this.tryInteract(hitX0, hitY, hitZ0);
				this.tryInteract(hitX1, hitY, hitZ0);
				this.tryInteract(hitX0, hitY, hitZ1);
				this.tryInteract(hitX1, hitY, hitZ1);

				if(state == 1) {
					this.rotationPitch += 2;

					if(this.rotationPitch > 80) {
						this.rotationPitch = 80;
						state = 2;
					}
				}

				if(state == 2) {
					this.rotationPitch -= 2;

					if(this.rotationPitch <= 0) {
						this.rotationPitch = 0;
						state = 0;
					}
				}
			}

			networkPackNT(100);
		} else {

			this.lastSpin = this.spin;

			if(isOn && !isSuspended) {
				this.spin += 15F;

				Vec3d vec = new Vec3d(0.625, 0, 1.625);
				vec = vec.rotateYaw(-(float) Math.toRadians(rotationYaw));

				this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, this.pos.getX() + 0.5 + vec.x, this.pos.getY() + 2.0625, this.pos.getZ() + 0.5 + vec.z, 0, 0, 0);
			}

			if(this.spin >= 360F) {
				this.spin -= 360F;
				this.lastSpin -= 360F;
			}

			this.prevRotationYaw = this.rotationYaw;
			this.prevRotationPitch = this.rotationPitch;

			if(this.turnProgress > 0) {
				double d0 = MathHelper.wrapDegrees(this.syncYaw - (double) this.rotationYaw);
				double d1 = MathHelper.wrapDegrees(this.syncPitch - (double) this.rotationPitch);
				this.rotationYaw = (float) ((double) this.rotationYaw + d0 / (double) this.turnProgress);
				this.rotationPitch = (float) ((double) this.rotationPitch + d1 / (double) this.turnProgress);
				--this.turnProgress;
			} else {
				this.rotationYaw = this.syncYaw;
				this.rotationPitch = this.syncPitch;
			}
		}
	}

	/** Anything additionally that the detector nor the blades should pick up on, like non-mature willows */
	public static boolean shouldIgnore(World world, int x, int y, int z, Block b, int meta) {
		if(b == ModBlocks.plant_tall) {
			return meta == EnumTallFlower.CD2.ordinal() + 8 || meta == EnumTallFlower.CD3.ordinal() + 8;
		}

		if((b instanceof IGrowable)) {
			return ((IGrowable) b).canGrow(world, new net.minecraft.util.math.BlockPos(x, y, z), com.hbm.world.WorldBlockState.read(world, x, y, z), world.isRemote);
		}

		return false;
	}

	protected void tryInteract(int x, int y, int z) {

		Block b = com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock();
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, x, y, z) );

		if(!shouldIgnore(this.world, x, y, z, b, meta)) {
			if(b.getDefaultState().getMaterial() == Material.LEAVES || b.getDefaultState().getMaterial() == Material.PLANTS) {
				cutCrop(x, y, z);
			} else if(b.getDefaultState().getMaterial() == Material.WOOD) {
				fellTree(x, y, z);
				if(state == 1) {
					state = 2;
				}
			}
		}

		// Return when hitting a wall
		if(state == 1 && com.hbm.world.WorldBlockState.read(this.world, x, y, z).isNormalCube()) {
			state = 2;
			forceSkip = 5;
		}
	}

	protected void cutCrop(int x, int y, int z) {

		Block soil = com.hbm.world.WorldBlockState.read(this.world, x, y - 1, z).getBlock();

		Block b = com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock();
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, x, y, z) );

		this.world.playEvent(2001, new net.minecraft.util.math.BlockPos(x, y, z), Block.getIdFromBlock(b) + (meta << 12));

		Block replacementBlock = Blocks.AIR;
		int replacementMeta = 0;

		if (!this.world.isRemote && !this.world.restoringBlockSnapshots) {
			ArrayList<ItemStack> drops = new ArrayList<ItemStack>(com.hbm.world.WorldBlockState.getDrops(b, this.world, x, y, z, meta, 0));
			boolean replanted = false;

			for (ItemStack drop : drops) {
				if (!replanted && drop.getItem() instanceof IPlantable) {
					IPlantable seed = (IPlantable) drop.getItem();

					if(soil.canSustainPlant(soil.getDefaultState(), this.world, new net.minecraft.util.math.BlockPos(x, y - 1, z), EnumFacing.UP, seed)) {
						replacementBlock = seed.getPlant(this.world, new net.minecraft.util.math.BlockPos(x, y, z)).getBlock();
						replacementMeta = com.hbm.world.WorldBlockState.metadata(seed.getPlant(this.world, new net.minecraft.util.math.BlockPos(x, y, z)));
						replanted = true;
						drop.shrink(1);
					}
				}

				float delta = 0.7F;
				double dx = (double)(this.world.rand.nextFloat() * delta) + (double)(1.0F - delta) * 0.5D;
				double dy = (double)(this.world.rand.nextFloat() * delta) + (double)(1.0F - delta) * 0.5D;
				double dz = (double)(this.world.rand.nextFloat() * delta) + (double)(1.0F - delta) * 0.5D;

				EntityItem entityItem = new EntityItem(this.world, x + dx, y + dy, z + dz, drop);
				entityItem.setPickupDelay(10);
				this.world.spawnEntity(entityItem);
			}

			// Apparently, until 1.14 full-grown wheat could sometimes drop no seeds at all
			// This is a quick and dirty workaround for that.
			if (b == Blocks.WHEAT && !replanted) {
				replacementBlock = b;
				replacementMeta = 0;
				replanted = true;
			}
		}

		com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, x, y, z, replacementBlock, replacementMeta, 3);
	}

	protected void fellTree(int hitX, int hitY, int hitZ) {

		int sawY = hitY;
		BlockPos hitCol = new BlockPos(hitX, -1, hitZ);

		// Step A: Scan working area for trunks (column -> trunk base pos)
		HashMap<BlockPos, BlockPos> trunks = new HashMap<BlockPos, BlockPos>();

		for(int dx = -MAX_DIST; dx <= MAX_DIST; dx++) {
			for(int dz = -MAX_DIST; dz <= MAX_DIST; dz++) {
				if(dx * dx + dz * dz > MAX_DIST * MAX_DIST) {
					continue;
				}

				int colX = this.pos.getX() + dx;
				int colZ = this.pos.getZ() + dz;

				if(com.hbm.world.WorldBlockState.read(this.world, colX, sawY, colZ).getBlock().getDefaultState().getMaterial() != Material.WOOD) {
					continue;
				}

				int baseY = sawY;
				while(sawY - baseY < FELL_MAX_BASE_DEPTH && com.hbm.world.WorldBlockState.read(this.world, colX, baseY - 1, colZ).getBlock().getDefaultState().getMaterial() == Material.WOOD) {
					baseY--;
				}

				if(!canSupportSapling(this.world, colX, baseY - 1, colZ)) {
					continue;
				}

				trunks.put(new BlockPos(colX, -1, colZ), new BlockPos(colX, baseY, colZ));
			}
		}

		// Always include the hit position's trunk
		if(!trunks.containsKey(hitCol)) {
			int baseY = hitY;
			while(sawY - baseY < FELL_MAX_BASE_DEPTH && com.hbm.world.WorldBlockState.read(this.world, hitX, baseY - 1, hitZ).getBlock().getDefaultState().getMaterial() == Material.WOOD) {
				baseY--;
			}
			trunks.put(hitCol, new BlockPos(hitX, baseY, hitZ));
		}

		// Step B: 0-1 BFS from all trunks
		// Vertical neighbors (same column) have distance 0, horizontal neighbors have distance 1
		// blockOwner: block pos -> column of owning trunk
		HashMap<BlockPos, BlockPos> blockOwner = new HashMap<BlockPos, BlockPos>();
		ArrayDeque<BlockPos[]> deque = new ArrayDeque<BlockPos[]>();
		int hitColCount = 1;

		int minY = Math.max(0, sawY - FELL_MAX_BASE_DEPTH);
		int maxY = Math.min(255, sawY + FELL_VERTICAL_RANGE);

		for(Map.Entry<BlockPos, BlockPos> trunk : trunks.entrySet()) {
			deque.addFirst(new BlockPos[] {trunk.getValue(), trunk.getKey()});
		}

		while(!deque.isEmpty()) {
			BlockPos[] pair = deque.pollFirst();
			BlockPos current = pair[0];
			BlockPos currentCol = pair[1];

			if(blockOwner.containsKey(current)) {
				if(currentCol.equals(hitCol)) {
					hitColCount--;
					if(hitColCount == 0) {
						break;
					}
				}
				continue;
			}
			blockOwner.put(current, currentCol);

			for(int[] dir : EIGHTEEN_DIRS) {
				int neighborX = current.getX() + dir[0];
				int neighborY = current.getY() + dir[1];
				int neighborZ = current.getZ() + dir[2];

				// Bounds check: radius FELL_BFS_RADIUS horizontal, minY to maxY vertical
				int neighborDx = neighborX - this.pos.getX();
				int neighborDz = neighborZ - this.pos.getZ();
				if(neighborDx * neighborDx + neighborDz * neighborDz > FELL_BFS_RADIUS * FELL_BFS_RADIUS) {
					continue;
				}
				if(neighborY < minY || neighborY > maxY) {
					continue;
				}

				BlockPos neighborPos = new BlockPos(neighborX, neighborY, neighborZ);
				if(blockOwner.containsKey(neighborPos)) {
					continue;
				}

				Block b = com.hbm.world.WorldBlockState.read(this.world, neighborX, neighborY, neighborZ).getBlock();
				Material mat = b.getDefaultState().getMaterial();
				if(mat != Material.WOOD && mat != Material.LEAVES && !(b instanceof BlockLeaves)) {
					continue;
				}

				boolean hasHorizontal = dir[0] != 0 || dir[2] != 0;
				BlockPos[] entry = new BlockPos[] {neighborPos, currentCol};
				if(!hasHorizontal) {
					deque.addFirst(entry);
				} else {
					deque.addLast(entry);
				}
				if(currentCol.equals(hitCol)) {
					hitColCount++;
				}
			}

			if(currentCol.equals(hitCol)) {
				hitColCount--;
				if(hitColCount == 0) {
					break; // Early exit: all hit-tree blocks processed
				}
			}
		}

		// Step C: Cut blocks assigned to the hit trunk
		for(Map.Entry<BlockPos, BlockPos> entry : blockOwner.entrySet()) {
			if(!entry.getValue().equals(hitCol)) {
				continue;
			}

			BlockPos pos = entry.getKey();
			int bx = pos.getX();
			int by = pos.getY();
			int bz = pos.getZ();

			Block b = com.hbm.world.WorldBlockState.read(this.world, bx, by, bz).getBlock();

			// Replant sapling at positions within working area
			if(b.getDefaultState().getMaterial() == Material.WOOD && isWithinWorkingArea(bx, bz) && canSupportSapling(this.world, bx, by - 1, bz)) {
				int bmeta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, bx, by, bz) );
				int sapMeta = 0;
				if(b == Blocks.LOG) {
					sapMeta = bmeta & 3;
				} else if(b == Blocks.LOG2) {
					sapMeta = (bmeta & 3) + 4;
				}
				com.hbm.world.WorldBlockDestruction.destroy(this.world, bx, by, bz, true);
				com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, bx, by, bz, Blocks.SAPLING, sapMeta, 3);
			} else {
				com.hbm.world.WorldBlockDestruction.destroy(this.world, bx, by, bz, true);
			}
		}
	}

	private boolean isWithinWorkingArea(int x, int z) {
		int dx = x - this.pos.getX();
		int dz = z - this.pos.getZ();
		int distSq = dx * dx + dz * dz;
		return distSq > MIN_DIST * MIN_DIST && distSq <= MAX_DIST * MAX_DIST;
	}

	private static boolean canSupportSapling(World world, int x, int y, int z) {
		Block block = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();
		return block.canSustainPlant(block.getDefaultState(), world, new net.minecraft.util.math.BlockPos(x, y, z), EnumFacing.UP, (IPlantable) Blocks.SAPLING);
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBoolean(this.isOn);
		buf.writeBoolean(this.isSuspended);
		buf.writeFloat(this.rotationYaw);
		buf.writeFloat(this.rotationPitch);
		this.tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.isOn = buf.readBoolean();
		this.isSuspended = buf.readBoolean();
		this.syncYaw = buf.readFloat();
		this.syncPitch = buf.readFloat();
		this.turnProgress = 3; //use 3-ply for extra smoothness
		this.tank.deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.isOn = nbt.getBoolean("isOn");
		this.isSuspended = nbt.getBoolean("isSuspended");
		this.forceSkip = nbt.getInteger("skip");
		this.rotationYaw = nbt.getFloat("yaw");
		this.rotationPitch = nbt.getFloat("pitch");
		this.state = nbt.getInteger("state");
		this.tank.readFromNBT(nbt, "t");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("isOn", this.isOn);
		nbt.setBoolean("isSuspended", this.isSuspended);
		nbt.setInteger("skip", this.forceSkip);
		nbt.setFloat("yaw", this.rotationYaw);
		nbt.setFloat("pitch", this.rotationPitch);
		nbt.setInteger("state", this.state);
		tank.writeToNBT(nbt, "t");
		return nbt;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {tank};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tank};
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					this.pos.getX() - 12,
					this.pos.getY(),
					this.pos.getZ() - 12,
					this.pos.getX() + 13,
					this.pos.getY() + 10,
					this.pos.getZ() + 13
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
	@Override
	public FluidTank getTankToPaste() {
		return tank;
	}
}
