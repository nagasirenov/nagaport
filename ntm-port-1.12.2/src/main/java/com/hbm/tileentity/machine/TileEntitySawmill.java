package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntitySawblade;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.machine.TileEntityMachineAutocrafter.InventoryCraftingAuto;
import com.hbm.util.BufferUtil;
import com.hbm.util.ItemStackUtil;

import api.hbm.tile.IHeatSource;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import com.hbm.lib.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntitySawmill extends TileEntityMachineBase {

	public int heat;
	public static final double diffusion = 0.1D;
	private int warnCooldown = 0;
	private int overspeed = 0;
	public boolean hasBlade = true;
	public int progress = 0;
	public static final int processingTime = 600;

	public float spin;
	public float lastSpin;

	public TileEntitySawmill() {
		super(3);
	}

	@Override
	public String getDefaultName() { return ""; }

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(hasBlade) {
				tryPullHeat();

				if(warnCooldown > 0)
					warnCooldown--;

				if(heat >= 100) {

					ItemStack result = this.getOutput(slots[0]);

					if(result != null) {
						progress += heat / 10;

						if(progress >= processingTime) {
							progress = 0;
							slots[0] = null;
							slots[1] = result;

							float sawdustChance = sawdustChance(result);

							if(sawdustChance > 0) {
								if(this.world.rand.nextFloat() < sawdustChance) {
									slots[2] = new ItemStack(ModItems.powder_sawdust);
								}
							}

							this.markDirty();
						}

					} else {
						this.progress = 0;
					}

					AxisAlignedBB aabb = new AxisAlignedBB(-1D, 0.375D, -1D, -0.875, 2.375D, 1D);
					aabb = BlockDummyable.getAABBRotationOffset(aabb, this.pos.getX() + 0.5, this.pos.getY(), this.pos.getZ() + 0.5, ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getRotation(ForgeDirection.UP));
					for(Object o : this.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
						EntityLivingBase e = (EntityLivingBase) o;
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

				} else {
					this.progress = 0;
				}

				if(heat > 300) {

					this.overspeed++;

					if(overspeed > 60 && warnCooldown == 0) {
						warnCooldown = 100;
						com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5, "hbm:block.warnOverspeed", 2.0F, 1.0F);
					}

					if(overspeed > 300) {
						this.hasBlade = false;
						this.world.newExplosion(null, this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5, 5F, false, false);

						int orientation = this.getBlockMetadata() - BlockDummyable.offset;
						ForgeDirection dir = ForgeDirection.getOrientation(orientation);
						EntitySawblade cog = new EntitySawblade(this.world, this.pos.getX() + 0.5 + dir.offsetX, this.pos.getY() + 1, this.pos.getZ() + 0.5 + dir.offsetZ).setOrientation(orientation);
						ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

						cog.motionX = rot.offsetX;
						cog.motionY = 1 + (heat - 100) * 0.0001D;
						cog.motionZ = rot.offsetZ;
						this.world.spawnEntityInWorld(cog);

						this.markDirty();
					}

				} else {
					this.overspeed = 0;
				}
			} else {
				this.overspeed = 0;
				this.warnCooldown = 0;
			}

			networkPackNT(150);

			this.heat = 0;

		} else {

			float momentum = heat * 25F / ((float) 300);

			this.lastSpin = this.spin;
			this.spin += momentum;

			if(this.spin >= 360F) {
				this.spin -= 360F;
				this.lastSpin -= 360F;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeInt(heat);
		buf.writeInt(progress);
		buf.writeBoolean(hasBlade);

		for (ItemStack slot : slots) {
			BufferUtil.writeItemStack(buf, slot);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.heat = buf.readInt();
		this.progress = buf.readInt();
		this.hasBlade = buf.readBoolean();

		for(int i = 0; i < slots.length; i++) {
			slots[i] = BufferUtil.readItemStack(buf);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.hasBlade = nbt.getBoolean("hasBlade");
		this.progress = nbt.getInteger("progress");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("hasBlade", hasBlade);
		nbt.setInteger("progress", progress);
		return nbt;
	}

	protected void tryPullHeat() {
		TileEntity con = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ()));

		if(con instanceof IHeatSource) {
			IHeatSource source = (IHeatSource) con;
			int heatSrc = (int) (source.getHeatStored() * diffusion);

			if(heatSrc > 0) {
				source.useUpHeat(heatSrc);
				this.heat += heatSrc;
				return;
			}
		}

		this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
	}

	protected InventoryCraftingAuto craftingInventory = new InventoryCraftingAuto(1, 1);

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return i == 0 && slots[0] == null && slots[1] == null && slots[2] == null && stack.stackSize == 1 && getOutput(stack) != null;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return i > 0;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] {0, 1, 2};
	}

	public float sawdustChance(ItemStack result) {
		Item item = result.getItem();
		Block block = Block.getBlockFromItem(item);
		if(item == ModItems.powder_sawdust) return 0;
		if(item == ModItems.stick_pvc || item == ModItems.stick_vinyl) return 0;
		if(block == ModBlocks.vinyl_planks || block == ModBlocks.pvc_planks) return 0;
		if(block == ModBlocks.vinyl_log || block == ModBlocks.pvc_log) return 0;
		if(item == Items.STICK) return 0.1F;
		return 0.5F;
	}

	public ItemStack getOutput(ItemStack input) {

		if(input == null)
			return null;

		Block block = Block.getBlockFromItem(input.getItem());

		craftingInventory.setInventorySlotContents(0, input);

		List<String> names = ItemStackUtil.getOreDictNames(input);

		if(input.getItem() == ModItems.stick_vinyl) {
			return new ItemStack(ModItems.dust);
		}
		if(input.getItem() == ModItems.stick_pvc) {
			return new ItemStack(ModItems.dust);
		}

		if(block == ModBlocks.vinyl_planks) {
			return new ItemStack(ModItems.stick_vinyl, 6);
		}
		if(block == ModBlocks.pvc_planks) {
			return new ItemStack(ModItems.stick_vinyl, 6);
		}

		if(block == ModBlocks.vinyl_log) {
			return new ItemStack(ModBlocks.vinyl_planks, 4 * 6 / 4);
		}
		if(block == ModBlocks.pvc_log) {
			return new ItemStack(ModBlocks.pvc_planks, 4 * 6 / 4);
		}

		if(names.contains("stickWood")) {
			return new ItemStack(ModItems.powder_sawdust);
		}

		if(names.contains("logWood")) {
			for(Object o : CraftingManager.getInstance().getRecipeList()) {
				IRecipe recipe = (IRecipe) o;
				if(recipe.matches(craftingInventory, this.world)) {
					ItemStack out = recipe.getCraftingResult(craftingInventory);
					if(out != null) {
						out = out.copy(); //for good measure
						out.stackSize = out.stackSize * 6 / 4; //4 planks become 6
						return out;
					}
				}
			}
		}

		if(names.contains("plankWood")) {
			return new ItemStack(Items.STICK, 6);
		}

		if(names.contains("treeSapling")) {
			return new ItemStack(Items.STICK, 1);
		}

		return null;
	}

	public static HashMap getRecipes() {

		HashMap<Object, Object[]> recipes = new HashMap<Object, Object[]>();

		// Overrides
		recipes.put(new ComparableStack(ModBlocks.vinyl_log), new ItemStack[] { new ItemStack(ModBlocks.vinyl_planks, 6)});
		recipes.put(new ComparableStack(ModBlocks.pvc_log), new ItemStack[] { new ItemStack(ModBlocks.pvc_planks, 6)});

		recipes.put(new ComparableStack(ModItems.stick_vinyl), new ItemStack[] { new ItemStack(ModItems.dust)});
		recipes.put(new ComparableStack(ModItems.stick_pvc), new ItemStack[] { new ItemStack(ModItems.dust)});

		recipes.put(new ComparableStack(ModBlocks.vinyl_planks), new ItemStack[] { new ItemStack(ModItems.stick_vinyl, 6)});
		recipes.put(new ComparableStack(ModBlocks.pvc_planks), new ItemStack[] { new ItemStack(ModItems.stick_pvc, 6)});

		// Generics
		List<ItemStack> logs = OreDictionary.getOres(OreDictManager.KEY_LOG);
		List<ItemStack> planks = OreDictionary.getOres(OreDictManager.KEY_PLANKS);
		List<ItemStack> sticks = OreDictionary.getOres(OreDictManager.KEY_STICK);
		List<ItemStack> saplings = OreDictionary.getOres(OreDictManager.KEY_SAPLING);

		for(ItemStack log : logs) {
			ComparableStack stack = new ComparableStack(log);
			if(recipes.containsKey(stack)) continue;
			recipes.put(stack, new ItemStack[] { new ItemStack(Blocks.PLANKS, 6), ItemStackUtil.addTooltipToStack(new ItemStack(ModItems.powder_sawdust), TextFormatting.RED + "50%") });
		}
		for(ItemStack plank : planks) {
			ComparableStack stack = new ComparableStack(plank);
			if(recipes.containsKey(stack)) continue;
			recipes.put(stack, new ItemStack[] { new ItemStack(Items.STICK, 6), ItemStackUtil.addTooltipToStack(new ItemStack(ModItems.powder_sawdust), TextFormatting.RED + "10%") });
		}
		for(ItemStack stick : sticks) {
			ComparableStack stack = new ComparableStack(stick);
			if(recipes.containsKey(stack)) continue;
			recipes.put(stack, new ItemStack[] { new ItemStack(ModItems.powder_sawdust) });
		}
		for(ItemStack sapling : saplings) {
			ComparableStack stack = new ComparableStack(sapling);
			if(recipes.containsKey(stack)) continue;
			recipes.put(stack, new ItemStack[] { new ItemStack(Items.STICK, 1), ItemStackUtil.addTooltipToStack(new ItemStack(ModItems.powder_sawdust), TextFormatting.RED + "10%") });
		}

		return recipes;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 1,
					this.pos.getY(),
					this.pos.getZ() - 1,
					this.pos.getX() + 2,
					this.pos.getY() + 2,
					this.pos.getZ() + 2
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
