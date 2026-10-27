package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.hbm.blocks.IBlockSideRotation;
import com.hbm.blocks.SideRotationModelState;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.StructureConfig;
import com.hbm.itempool.ItemPool;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.LootGenerator;
import com.hbm.util.i18n.I18nUtil;
import com.hbm.world.gen.nbt.INBTTileEntityTransformable;
import com.mojang.authlib.GameProfile;

import api.hbm.block.IToolable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;
import com.hbm.lib.WeightedRandomChestContent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

public class BlockWandLoot extends BlockContainer implements ILookOverlay, IToolable, ITooltipProvider, IBlockSideRotation {

	private static final ResourceLocation blockTexture = new ResourceLocation(RefStrings.MODID, "blocks/wand_loot");
	private static final ResourceLocation iconTop = new ResourceLocation(RefStrings.MODID, "blocks/wand_loot_top");
	private static final Collection<ResourceLocation> MODEL_TEXTURES = Collections.unmodifiableList(Arrays.asList(blockTexture, iconTop));

	public BlockWandLoot() {
		super(Material.IRON);
		setDefaultState(blockState.getBaseState().withProperty(SideRotationModelState.METADATA, 0));
	}


	@Override
	public ResourceLocation getModelTexture(int side, int meta) {
		return (side <= 1) ? iconTop : blockTexture;
	}

	@Override
	public int getRotationFromSide(IBlockAccess world, int x, int y, int z, int side) {
		if(side == 0) return IBlockSideRotation.topToBottom(getMetaFromState(world.getBlockState(new BlockPos(x, y, z))));
		if(side == 1) return getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));
		return 0;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public Collection<ResourceLocation> getModelTextures() {
		return MODEL_TEXTURES;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return SideRotationModelState.create(this);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(SideRotationModelState.METADATA, meta & 15);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(SideRotationModelState.METADATA);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return SideRotationModelState.extend(state, world, pos);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if(i == 0) world.setBlockState(pos, getStateFromMeta(3), 2);
		if(i == 1) world.setBlockState(pos, getStateFromMeta(2), 2);
		if(i == 2) world.setBlockState(pos, getStateFromMeta(0), 2);
		if(i == 3) world.setBlockState(pos, getStateFromMeta(1), 2);

		TileEntity te = world.getTileEntity(pos);
		if(!(te instanceof TileEntityWandLoot)) return;
		((TileEntityWandLoot) te).placedRotation = player.rotationYaw;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

		if(!(te instanceof TileEntityWandLoot)) return;

		TileEntityWandLoot loot = (TileEntityWandLoot) te;

		List<String> text = new ArrayList<String>();
		text.add("Will replace with: " + loot.replaceBlock.getTranslationKey());
		text.add("   meta: " + loot.replaceMeta);
		text.add("Loot pool: " + loot.poolName);
		if(loot.replaceBlock != ModBlocks.deco_loot) {
			text.add("Minimum items: " + loot.minItems);
			text.add("Maximum items: " + loot.maxItems);
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add("Define loot crates/piles in .nbt structures");
		list.add(TextFormatting.GOLD + "Use screwdriver to increase/decrease minimum loot");
		list.add(TextFormatting.GOLD + "Use hand drill to increase/decrease maximum loot");
		list.add(TextFormatting.GOLD + "Use defuser to cycle loot types");
		list.add(TextFormatting.GOLD + "Use container block to set the block that spawns with loot inside");
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);

		if(!(te instanceof TileEntityWandLoot)) return false;

		TileEntityWandLoot loot = (TileEntityWandLoot) te;

		if(!player.isSneaking()) {

			Block block = getLootableBlock(world, player.getHeldItem(hand));

			if(block != null) {
				loot.replaceBlock = block;
				loot.replaceMeta = player.getHeldItem(hand).getItemDamage();

				List<String> poolNames = loot.getPoolNames(block == ModBlocks.deco_loot);
				if(!poolNames.contains(loot.poolName)) {
					loot.poolName = poolNames.get(0);
				}

				return true;
			}
		}

		return false;
	}

	private Block getLootableBlock(World world, ItemStack stack) {
		Block block = ModBlocks.getBlockFromStack(stack);
		if(block == null) return null;

		if(block == ModBlocks.deco_loot) return block;

		if(block instanceof ITileEntityProvider) {
			TileEntity te = ((ITileEntityProvider) block).createNewTileEntity(world, 12);
			if(te instanceof IInventory) return block;
		}

		return null;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

		if(!(te instanceof TileEntityWandLoot)) return false;

		TileEntityWandLoot loot = (TileEntityWandLoot) te;

		switch(tool) {
		case SCREWDRIVER:
			if(player.isSneaking()) {
				loot.minItems--;
				if(loot.minItems < 0) loot.minItems = 0;
			} else {
				loot.minItems++;
				loot.maxItems = Math.max(loot.minItems, loot.maxItems);
			}

			return true;

		case HAND_DRILL:
			if(player.isSneaking()) {
				loot.maxItems--;
				if(loot.maxItems < 0) loot.maxItems = 0;
				loot.minItems = Math.min(loot.minItems, loot.maxItems);
			} else {
				loot.maxItems++;
			}

			return true;

		case DEFUSER:
			List<String> poolNames = loot.getPoolNames(loot.replaceBlock == ModBlocks.deco_loot);
			int index = poolNames.indexOf(loot.poolName);

			index += player.isSneaking() ? -1 : 1;
			index = MathHelper.clamp(index, 0, poolNames.size() - 1);

			loot.poolName = poolNames.get(index);

			return true;

		default: return false;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityWandLoot();
	}

	public static class TileEntityWandLoot extends TileEntityLoadedBase implements INBTTileEntityTransformable, net.minecraft.util.ITickable {

		@Override
		public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
			return oldState.getBlock() != newState.getBlock();
		}


		private boolean triggerReplace;

		private Block replaceBlock = ModBlocks.deco_loot;
		private int replaceMeta;

		private String poolName = LootGenerator.LOOT_BOOKLET;
		private int minItems;
		private int maxItems = 1;

		private float placedRotation;

		private static final GameProfile FAKE_PROFILE = new GameProfile(UUID.fromString("839eb18c-50bc-400c-8291-9383f09763e7"), "[NTM]");
		private static FakePlayer fakePlayer;

		@Override
		public void update() {
			if(this.isInvalid()) return;
			if(!(this.world.getBlockState(this.pos).getBlock() instanceof BlockWandLoot)) {
				MainRegistry.logger.warn("Somehow the block at: " + this.pos.getX() + ", " + this.pos.getY() + ", " + this.pos.getZ() + " isn't a loot block, cleaning up orphan TE!");
				this.invalidate();
				if(this.hasWorld()) {
					this.world.removeTileEntity(this.pos);
				}
				return;
			}
			if(!this.world.isRemote) {
				if(triggerReplace) {
					// On the first tick of this TE, replace with intended block and fill with loot
					replace();
				} else {
					networkPackNT(15);
				}
			}
		}

		private void replace() {
			if(this.isInvalid()) return;
			if(!(this.world.getBlockState(this.pos).getBlock() instanceof BlockWandLoot)) {
				MainRegistry.logger.warn("Somehow the block at: " + this.pos.getX() + ", " + this.pos.getY() + ", " + this.pos.getZ() + " isn't a loot block, cleaning up orphan TE!");
				this.invalidate();
				if(this.hasWorld()) {
					this.world.removeTileEntity(this.pos);
				}
				return;
			}

			WeightedRandomChestContent[] pool = ItemPool.getPool(poolName);

			this.world.setBlockState(this.pos, replaceBlock.getStateFromMeta(replaceMeta), 2);

			TileEntity te = this.world.getTileEntity(this.pos);

			if(te == null || te instanceof TileEntityWandLoot) {
				// Some generator has broken the TE->block relationship, which, honestly, rude.
				// so we're just gonna hop in and force update the TE

				MainRegistry.logger.warn("TE set incorrectly at: " + this.pos.getX() + ", " + this.pos.getY() + ", " + this.pos.getZ() + ". If you're using some sort of world generation mod, report it to the author!");

				te = replaceBlock.createTileEntity(this.world, replaceBlock.getStateFromMeta(replaceMeta));
				this.world.setTileEntity(this.pos, te);
			}

			if(te instanceof IInventory) {
				int count = minItems;
				if(maxItems - minItems > 0) count += this.world.rand.nextInt(maxItems - minItems);
				count = (int)Math.floor(count * StructureConfig.lootAmountFactor);
				WeightedRandomChestContent.generateChestContents(this.world.rand, pool, (IInventory) te, count);
			} else if(te instanceof BlockLoot.TileEntityLoot) {
				LootGenerator.applyLoot(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), poolName);
			}

			// Shouldn't happen but let's guard anyway, if it fails we just don't rotate the chest block correctly
			if(!(this.world instanceof WorldServer)) return;

			try {
				if(fakePlayer == null || fakePlayer.world != this.world) {
					fakePlayer = FakePlayerFactory.get((WorldServer)this.world, FAKE_PROFILE);
				}

				fakePlayer.rotationYaw = fakePlayer.rotationYawHead = placedRotation;

				ItemStack fakeStack = new ItemStack(replaceBlock, 1, replaceMeta);

				replaceBlock.onBlockPlacedBy(this.world, this.pos, this.world.getBlockState(this.pos), fakePlayer, fakeStack);
			} catch(Exception ex) {
				MainRegistry.logger.warn("Failed to correctly rotate loot block at: " + this.pos.getX() + ", " + this.pos.getY() + ", " + this.pos.getZ());
				MainRegistry.logger.catching(ex);
			}
		}

		private List<String> getPoolNames(boolean loot) {
			if(loot) return Arrays.asList(LootGenerator.getLootNames());

			List<String> names = new ArrayList<>();
			names.addAll(ItemPool.pools.keySet());
			return names;
		}

		@Override
		public void transformTE(World world, int coordBaseMode) {
			triggerReplace = !StructureConfig.debugStructures;
			placedRotation = MathHelper.wrapDegrees(placedRotation + coordBaseMode * 90);
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			Block writeBlock = replaceBlock == null ? ModBlocks.deco_loot : replaceBlock;
			nbt.setString("block", writeBlock.getRegistryName().toString());
			nbt.setInteger("meta", replaceMeta);
			nbt.setInteger("min", minItems);
			nbt.setInteger("max", maxItems);
			nbt.setString("pool", poolName);
			nbt.setFloat("rot", placedRotation);

			nbt.setBoolean("trigger", triggerReplace);
			return nbt;
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			replaceBlock = Block.getBlockFromName(nbt.getString("block"));
			replaceMeta = nbt.getInteger("meta");
			minItems = nbt.getInteger("min");
			maxItems = nbt.getInteger("max");
			poolName = nbt.getString("pool");
			placedRotation = nbt.getFloat("rot");

			if(replaceBlock == null) replaceBlock = ModBlocks.deco_loot;

			triggerReplace = nbt.getBoolean("trigger");
		}

		@Override
		public void serialize(ByteBuf buf) {
			buf.writeInt(Block.getIdFromBlock(replaceBlock));
			buf.writeInt(replaceMeta);
			buf.writeInt(minItems);
			buf.writeInt(maxItems);
			BufferUtil.writeString(buf, poolName);
		}

		@Override
		public void deserialize(ByteBuf buf) {
			replaceBlock = Block.getBlockById(buf.readInt());
			replaceMeta = buf.readInt();
			minItems = buf.readInt();
			maxItems = buf.readInt();
			poolName = BufferUtil.readString(buf);
		}

	}

}
