package com.hbm.entity.item;

import java.util.ArrayList;
import java.util.Iterator;

import com.hbm.blocks.BlockFallingNT;
import com.hbm.blocks.ISpotlight;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityFallingBlockNT extends Entity {

	private static final DataParameter<Integer> BLOCK = EntityDataManager.createKey(EntityFallingBlockNT.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> META = EntityDataManager.createKey(EntityFallingBlockNT.class, DataSerializers.VARINT);
    private Block fallingBlock;
	private int fallingMeta = -1;
	public int fallingTicks;
	public boolean canDrop;
	private boolean destroyOnLand;
	private boolean canHurtEntities;
	private int damageCap;
	private float damageAmount;
	public NBTTagCompound tileNBT;

	public EntityFallingBlockNT(World world) {
		super(world);
		this.canDrop = true;
		this.damageCap = 40;
		this.damageAmount = 2.0F;
		this.setSize(0.98F, 0.98F);
		
	}

	public EntityFallingBlockNT(World world, double x, double y, double z, Block block) {
		this(world, x, y, z, block, 0);
	}

	public EntityFallingBlockNT(World world, double x, double y, double z, Block block, int meta) {
		super(world);
        // Preserve the original spawning constructor's Entity-default dimensions.
        this.canDrop = true;
        this.damageCap = 40;
        this.damageAmount = 2.0F;
		this.fallingBlock = block;
		this.dataManager.set(BLOCK, Block.getIdFromBlock(fallingBlock));
		this.fallingMeta = meta;
		this.dataManager.set(META, fallingMeta);
		this.preventEntitySpawning = true;
		this.setPosition(x, y, z);
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;
	}

	@Override protected void entityInit() {
		this.dataManager.register(BLOCK, 0);
		this.dataManager.register(META, 0);
	}
	
    public Block getBlock() { return Block.getBlockById(this.dataManager.get(BLOCK)); }
    public int getMeta() { return this.dataManager.get(META); }
    public IBlockState getFallingState() { return getBlock().getStateFromMeta(getMeta()); }

	@Override protected boolean canTriggerWalking() { return false; }
	@Override public boolean canBeCollidedWith() { return !this.isDead; }

	public void onUpdate() {
		
		if(this.getFallingState().getMaterial() == Material.AIR || this.getBlock() instanceof ISpotlight) {
			this.setDead();
		} else {
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			++this.fallingTicks;
			this.motionY -= 0.04D;
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.98D;
			this.motionY *= 0.98D;
			this.motionZ *= 0.98D;

			if(!this.world.isRemote) {
				int x = MathHelper.floor(this.posX);
				int y = MathHelper.floor(this.posY);
				int z = MathHelper.floor(this.posZ);
				int meta = this.getMeta();

				if(this.fallingTicks == 1) {
					if(this.world.getBlockState(new BlockPos(x, y, z)).getBlock() != this.getBlock()) {
						this.setDead();
						return;
					}

					this.world.setBlockToAir(new BlockPos(x, y, z));
				}

				if(this.onGround) {
					this.motionX *= 0.7D;
					this.motionZ *= 0.7D;
					this.motionY *= -0.5D;

					if(this.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.PISTON_EXTENSION) {
						this.setDead();

						IBlockState replacedState = this.world.getBlockState(new BlockPos(x, y, z));
						if(!this.destroyOnLand && replacementCheck(x, y, z) && this.world.setBlockState(new BlockPos(x, y, z), this.getFallingState(), 3)) {

							if(this.getBlock() instanceof BlockFalling) ((BlockFalling) this.getBlock()).onEndFalling(this.world, new BlockPos(x, y, z), this.getFallingState(), replacedState);
							if(this.getBlock() instanceof BlockFallingNT) ((BlockFallingNT) this.getBlock()).onLand(this.world, x, y, z, meta);

							if(this.tileNBT != null && this.getBlock() instanceof ITileEntityProvider) {
								TileEntity tileentity = this.world.getTileEntity(new BlockPos(x, y, z));

								if(tileentity != null) {
									NBTTagCompound nbt = new NBTTagCompound();
									tileentity.writeToNBT(nbt);
									Iterator it = this.tileNBT.getKeySet().iterator();

									while(it.hasNext()) {
										String s = (String) it.next();
										NBTBase nbtbase = this.tileNBT.getTag(s);

										if(!s.equals("x") && !s.equals("y") && !s.equals("z")) {
											nbt.setTag(s, nbtbase.copy());
										}
									}

									tileentity.readFromNBT(nbt);
									tileentity.markDirty();
								}
							}
						} else if(this.canDrop && !this.destroyOnLand && this.getBlock().getItemDropped(this.getFallingState(), rand, 0) != null) {
							this.entityDropItem(new ItemStack(this.getBlock().getItemDropped(this.getFallingState(), rand, 0), 1, this.getBlock().damageDropped(this.getFallingState())), 0.0F);
						}
					}
				} else if(this.fallingTicks > 100 && !this.world.isRemote && (y < 1 || y > 256) || this.fallingTicks > 600) {
					if(this.canDrop && this.getBlock().getItemDropped(this.getFallingState(), rand, 0) != null) {
						this.entityDropItem(new ItemStack(this.getBlock().getItemDropped(this.getFallingState(), rand, 0), 1, this.getBlock().damageDropped(this.getFallingState())), 0.0F);
					}

					this.setDead();
				}
			}
		}
	}
	
	public boolean replacementCheck(int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
        return world.getBlockState(pos).getBlock().isReplaceable(world, pos) && this.getBlock().canPlaceBlockAt(world, pos);
	}

	@Override
	public void fall(float fallDistance, float damageMultiplier) {
		
		if(this.canHurtEntities) {
			int fall = MathHelper.ceil(fallDistance - 1.0F);

			if(fall > 0) {
				ArrayList arraylist = new ArrayList(this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox()));
				boolean isAnvil = this.getBlock() == Blocks.ANVIL;
				DamageSource damagesource = isAnvil ? DamageSource.ANVIL : DamageSource.FALLING_BLOCK;
				Iterator iterator = arraylist.iterator();

				while(iterator.hasNext()) {
					Entity entity = (Entity) iterator.next();
					entity.attackEntityFrom(damagesource, (float) Math.min(MathHelper.floor((float) fall * this.damageAmount), this.damageCap));
				}

				if(isAnvil && (double) this.rand.nextFloat() < 0.05D + (double) fall * 0.05D) {
					int j = this.getMeta() >> 2;
					int k = this.getMeta() & 3;
					++j;

					if(j > 2) {
						this.destroyOnLand = true;
					} else {
						this.fallingMeta = k | j << 2;
                        this.dataManager.set(META, this.fallingMeta);
					}
				}
			}
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setByte("Tile", (byte) Block.getIdFromBlock(this.getBlock()));
		nbt.setInteger("TileID", Block.getIdFromBlock(this.getBlock()));
		nbt.setByte("Data", (byte) this.getMeta());
		nbt.setByte("Time", (byte) this.fallingTicks);
		nbt.setBoolean("DropItem", this.canDrop);
		nbt.setBoolean("HurtEntities", this.canHurtEntities);
		nbt.setFloat("FallHurtAmount", this.damageAmount);
		nbt.setInteger("FallHurtMax", this.damageCap);

		if(this.tileNBT != null) {
			nbt.setTag("TileEntityData", this.tileNBT);
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		
		if(nbt.hasKey("TileID", 99)) {
			this.fallingBlock = Block.getBlockById(nbt.getInteger("TileID"));
		} else {
			this.fallingBlock = Block.getBlockById(nbt.getByte("Tile") & 255);
		}

		this.fallingMeta = nbt.getByte("Data") & 255;
		this.fallingTicks = nbt.getByte("Time") & 255;

		if(nbt.hasKey("HurtEntities", 99)) {
			this.canHurtEntities = nbt.getBoolean("HurtEntities");
			this.damageAmount = nbt.getFloat("FallHurtAmount");
			this.damageCap = nbt.getInteger("FallHurtMax");
		} else if(this.fallingBlock == Blocks.ANVIL) {
			this.canHurtEntities = true;
		}

		if(nbt.hasKey("DropItem", 99)) {
			this.canDrop = nbt.getBoolean("DropItem");
		}

		if(nbt.hasKey("TileEntityData", 10)) {
			this.tileNBT = nbt.getCompoundTag("TileEntityData");
		}

		if(this.fallingBlock == null || this.fallingBlock.getDefaultState().getMaterial() == Material.AIR) {
			this.fallingBlock = Blocks.SAND;
		}
        this.dataManager.set(BLOCK, Block.getIdFromBlock(this.fallingBlock));
        this.dataManager.set(META, this.fallingMeta);
	}

	public void func_145806_a(boolean p_145806_1_) {
		this.canHurtEntities = p_145806_1_;
	}

	@Override
	public void addEntityCrashInfo(CrashReportCategory report) {
		super.addEntityCrashInfo(report);
		report.addCrashSection("Immitating block ID", Integer.valueOf(Block.getIdFromBlock(this.getBlock())));
		report.addCrashSection("Immitating block data", Integer.valueOf(this.fallingMeta));
	}

	@SideOnly(Side.CLIENT)
	public World getWorldForRender() {
		return this.world;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderOnFire() {
		return false;
	}

	public Block getBlockForRender() {
		return this.getBlock();
	}
}
