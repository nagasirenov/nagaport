package com.hbm.blocks.generic;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.ModDamageSource;
import com.hbm.tileentity.deco.TileEntityTrappedBrick;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class TrappedBrick extends BlockContainer {

	public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);

	public TrappedBrick(Material mat) {
		super(mat);
		this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, META);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(META, meta & 15);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(META);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {

		if(Trap.get(meta).type == TrapType.DETECTOR)
				return new TileEntityTrappedBrick();

		return null;
	}

    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {

        for (int i = 0; i < Trap.values().length; ++i) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @SuppressWarnings("incomplete-switch")

		@Override
	public void onEntityWalk(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.entity.Entity entityIn) {
		onEntityWalking(worldIn, pos.getX(), pos.getY(), pos.getZ(), entityIn);
	}

public void onEntityWalking(World world, int x, int y, int z, Entity entity) {

    	int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

    	if(world.isRemote || Trap.get(meta).type != TrapType.ON_STEP || !(entity instanceof EntityPlayer)) {
    		return;
    	}

    	EntityPlayer player = (EntityPlayer)entity;

		switch(Trap.get(meta)) {
		case FIRE:
			if(com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x, y + 1, z)))
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y + 1; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.FIRE; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			break;
		case SPIKES:
			if(com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x, y + 1, z)))
				com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y + 1, z, ModBlocks.spikes);
			List<Entity> targets = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(x, y + 1, z, x + 1, y + 2, z + 1));
			for(Entity e : targets)
				e.attackEntityFrom(ModDamageSource.spikes, 10);
			world.playSound(null, x + 0.5, y + 1.5, z + 0.5, com.hbm.main.ModSoundEvents.resolve("hbm:entity.slicer"), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
			break;
		case MINE:
			world.createExplosion(null, x + 0.5, y + 1.5, z + 0.5, 1F, false);
			break;
		case WEB:
			if(com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x, y + 1, z)))
				{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y + 1; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.WEB; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			break;
		case RAD_CONVERSION:
			for(int a = - 3; a <= 3; a++) {
				for(int b = - 3; b <= 3; b++) {
					for(int c = - 3; c <= 3; c++) {

						if(world.rand.nextBoolean())
							continue;

						Block bl = com.hbm.world.WorldBlockState.read(world, x + a, y + b, z + c).getBlock();
						if(bl == ModBlocks.brick_jungle || bl == ModBlocks.brick_jungle_cracked || bl == ModBlocks.brick_jungle_lava) {
							{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x + a; int nativeRegisteredWriteY = y + b; int nativeRegisteredWriteZ = z + c; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.brick_jungle_ooze; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
						}
					}
				}
			}
			break;
		case MAGIC_CONVERSTION:
			for(int a = - 3; a <= 3; a++) {
				for(int b = - 3; b <= 3; b++) {
					for(int c = - 3; c <= 3; c++) {

						if(world.rand.nextBoolean())
							continue;

						Block bl = com.hbm.world.WorldBlockState.read(world, x + a, y + b, z + c).getBlock();
						if(bl == ModBlocks.brick_jungle || bl == ModBlocks.brick_jungle_cracked || bl == ModBlocks.brick_jungle_lava) {
							{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x + a; int nativeRegisteredWriteY = y + b; int nativeRegisteredWriteZ = z + c; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.brick_jungle_mystic; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
						}
					}
				}
			}
			break;
		case SLOWNESS:
			player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.SLOWNESS, 300, 2));
			break;
		case WEAKNESS:
			player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.WEAKNESS, 300, 2));
			break;
		}

		com.hbm.sound.VanillaWorldSoundEffects.play(world, x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 0.3F, 0.6F);
		{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.brick_jungle; int nativeRegisteredWriteMeta = 0; int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
    }

	public static enum TrapType {
		ON_STEP,
		DETECTOR
	}

	public static enum Trap {

		FALLING_ROCKS(TrapType.DETECTOR),
		FIRE(TrapType.ON_STEP),
		ARROW(TrapType.DETECTOR),
		SPIKES(TrapType.ON_STEP),
		MINE(TrapType.ON_STEP),
		WEB(TrapType.ON_STEP),
		FLAMING_ARROW(TrapType.DETECTOR),
		PILLAR(TrapType.DETECTOR),
		RAD_CONVERSION(TrapType.ON_STEP),
		MAGIC_CONVERSTION(TrapType.ON_STEP),
		SLOWNESS(TrapType.ON_STEP),
		WEAKNESS(TrapType.ON_STEP),
		POISON_DART(TrapType.DETECTOR),
		ZOMBIE(TrapType.DETECTOR),
		SPIDERS(TrapType.DETECTOR);

		public TrapType type;

		private Trap(TrapType type) {
			this.type = type;
		}

		public static Trap get(int i) {

			if(i >= 0 && i < Trap.values().length)
				return Trap.values()[i];

			return FIRE;
		}
	}

}
