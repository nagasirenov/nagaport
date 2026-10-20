package com.hbm.blocks.generic;

import java.util.Random;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.IBlockMultiPass;
import com.hbm.blocks.MultipassContext;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.SolarSystem;
import com.hbm.items.ItemEnums.EnumChunkType;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockOre extends Block implements IBlockMultiPass, IBlockMulti, ITooltipProvider {

    @Override
    public Item getItemDropped(net.minecraft.block.state.IBlockState state, Random rand, int fortune) {
        return getItemDropped(getMetaFromState(state), rand, fortune);
    }

    @Override
    public int damageDropped(net.minecraft.block.state.IBlockState state) {
        return damageDropped(getMetaFromState(state));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(CreativeTabs tab, net.minecraft.util.NonNullList<ItemStack> list) {
        getSubBlocks(Item.getItemFromBlock(this), tab, list);
    }

    @Override
    public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos pos,
            net.minecraft.block.state.IBlockState state, EntityLivingBase player, ItemStack stack) {
        onBlockPlacedBy(world, pos.getX(), pos.getY(), pos.getZ(), player, stack);
    }


    public BlockOre withSoundType(net.minecraft.block.SoundType sound) { setSoundType(sound); return this; }


    @Override protected net.minecraft.block.state.BlockStateContainer createBlockState() { return MultipassContext.stateContainer(this); }
    @Override public net.minecraft.block.state.IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(MultipassContext.METADATA, metadata & 15); }
    @Override public int getMetaFromState(net.minecraft.block.state.IBlockState state) { return state.getValue(MultipassContext.METADATA); }
    @Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) { return net.minecraft.util.EnumBlockRenderType.MODEL; }
    @Override public net.minecraft.util.BlockRenderLayer getRenderLayer() { return net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public net.minecraft.block.state.IBlockState getExtendedState(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
        return ((net.minecraftforge.common.property.IExtendedBlockState) state).withProperty(MultipassContext.PROPERTY, getMultipassContext(state, world, pos));
    }


	// Any ore can be placed on any planet, with a planet enum to set the stone type

	// I'd rather a system that both spawns _and_ marks spawnable, to ensure we don't get any desync
	// but that aint trivial for the moment
	public Set<SolarSystem.Body> spawnsOn = new HashSet<>();

	public BlockOre(Material mat) {
		super(mat);
	}

	public static void addValidBody(Block ore, SolarSystem.Body body) {
		if(!(ore instanceof BlockOre)) return;
		((BlockOre) ore).spawnsOn.add(body);
	}

	public static void addAllBodies(Block ore) {
		for(SolarSystem.Body celestial : SolarSystem.Body.values()) {
			addValidBody(ore, celestial);
		}
	}

	public static void addAllExcept(Block ore, SolarSystem.Body body) {
		for(SolarSystem.Body celestial : SolarSystem.Body.values()) {
			if(celestial == body) continue;
			addValidBody(ore, celestial);
		}
	}

	// mappings from vanilla ores (for tooltips)
	public static Map<Block, BlockOre> vanillaMap = new HashMap<>();

	public BlockOre(Material mat, Block vanillaBlock) {
		this(mat);
		vanillaMap.put(vanillaBlock, this);
	}

	// Retained NTM scalar domain hook; native callbacks dispatch through it.
	public Item getItemDropped(int i, Random rand, int j) {
		if(this == ModBlocks.ore_fluorite) {
			return ModItems.fluorite;
		}
		if(this == ModBlocks.ore_niter) {
			return ModItems.niter;
		}
		if(this == ModBlocks.ore_sulfur) {
			return ModItems.sulfur;
		}
		if(this == ModBlocks.ore_glowstone) {
			return Items.GLOWSTONE_DUST;
		}
		if(this == ModBlocks.ore_fire) {
			return rand != null && rand.nextInt(10) == 0 ? ModItems.ingot_phosphorus : ModItems.powder_fire;
		}
		if(this == ModBlocks.ore_rare) {
			return ModItems.chunk_ore;
		}
		if(this == ModBlocks.ore_asbestos) {
			return ModItems.ingot_asbestos;
		}
		if(this == ModBlocks.ore_lignite) {
			return ModItems.lignite;
		}
		if(this == ModBlocks.ore_cinnebar) {
			return ModItems.cinnebar;
		}
		if(this == ModBlocks.ore_coltan) {
			return ModItems.fragment_coltan;
		}
		if(this == ModBlocks.ore_cobalt) {
			return ModItems.fragment_cobalt;
		}
		// Vanilla reproduction
		if(this == ModBlocks.ore_redstone) {
			return Items.REDSTONE;
		}
		if(this == ModBlocks.ore_lapis) {
			return Items.DYE;
		}
		if(this == ModBlocks.ore_emerald) {
			return Items.EMERALD;
		}
		if(this == ModBlocks.ore_quartz) {
			return Items.QUARTZ;
		}
		if(this == ModBlocks.ore_diamond) {
			return Items.DIAMOND;
		}

		return Item.getItemFromBlock(this);
	}

	@Override
	public int quantityDropped(Random rand) {
		if(this == ModBlocks.ore_fluorite) {
			return 2 + rand.nextInt(3);
		}
		if(this == ModBlocks.ore_niter) {
			return 2 + rand.nextInt(3);
		}
		if(this == ModBlocks.ore_sulfur) {
			return 2 + rand.nextInt(3);
		}
		if(this == ModBlocks.ore_glowstone) {
			return 1 + rand.nextInt(3);
		}
		if(this == ModBlocks.ore_cobalt) {
			return 4 + rand.nextInt(6);
		}
		if(this == ModBlocks.ore_redstone) {
			return 4 + rand.nextInt(2);
		}
		if(this == ModBlocks.ore_lapis) {
			return 4 + rand.nextInt(5);
		}
		return 1;
	}

	public boolean allowFortune = true;

	public BlockOre noFortune() {
		this.allowFortune = false;
		return this;
	}

	@Override
	public int quantityDroppedWithBonus(int fortune, Random rand) {

		if(fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped(0, rand, fortune) && allowFortune) {
			int mult = rand.nextInt(fortune + 2) - 1;

			if(mult < 0) {
				mult = 0;
			}

			return this.quantityDropped(rand) * (mult + 1);
		} else {
			return this.quantityDropped(rand);
		}
	}







	// Retained NTM scalar domain hook; native callbacks dispatch through it.
	public int damageDropped(int meta) {
		if(this == ModBlocks.ore_rare) return EnumChunkType.RARE.ordinal();
		if(this == ModBlocks.ore_lapis) return 4;
		if(getItemDropped(0, null, 0) != Item.getItemFromBlock(this)) return 0;
		return rectify(meta);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	// Retained NTM scalar domain hook; native callbacks dispatch through it.
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		for(int i = 0; i < getSubCount(); i++)
			list.add(new ItemStack(item, 1, i));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		if(!SpaceConfig.showOreLocations) return;
		if(spawnsOn.isEmpty()) return;

		if(spawnsOn.size() == SolarSystem.Body.values().length) {
			list.add(TextFormatting.GOLD + "Can be found anywhere");
			return;
		} else if(spawnsOn.size() == SolarSystem.Body.values().length - 1) {
			list.add(TextFormatting.GOLD + "Can be found anywhere except:");
			for(SolarSystem.Body body : SolarSystem.Body.values()) {
				if(spawnsOn.contains(body)) continue;
				list.add(TextFormatting.RED + " - " + I18nUtil.resolveKey("body." + body.name));
			}
			return;
		}

		list.add(TextFormatting.GOLD + "Can be found on:");
		for(SolarSystem.Body body : spawnsOn) {
			list.add(TextFormatting.AQUA + " - " + I18nUtil.resolveKey("body." + body.name));
		}
	}

	public EnumRarity getRarity(ItemStack stack) {
		if(this == ModBlocks.ore_australium) return EnumRarity.UNCOMMON;
		if(this == ModBlocks.ore_rare) return EnumRarity.UNCOMMON;
		return EnumRarity.COMMON;
	}

	// Retained NTM scalar domain hook; native callbacks dispatch through it.
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		int meta = stack.getItemDamage();
		// Keep stack evaluation before the original receiver/bounds checks.
        if(world == null) throw new NullPointerException();
        if(x >= -30000000 && x < 30000000 && z >= -30000000 && z < 30000000 && y >= 0 && y < 256) {
            net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(x, y, z);
            net.minecraft.block.state.IBlockState current = world.getBlockState(pos);
            net.minecraft.block.state.IBlockState changed = current.getBlock().getStateFromMeta(meta);
            if(changed == current && current.getBlock().getMetaFromState(current) != meta) {
                // The original chunk compared raw damage before storing its low nibble.
                net.minecraft.world.chunk.Chunk chunk = world.getChunk(pos);
                if(chunk.getBlockStorageArray()[y >> 4] != net.minecraft.world.chunk.Chunk.NULL_BLOCK_STORAGE) {
                    chunk.markDirty();
                    if(chunk.isPopulated()) world.notifyBlockUpdate(pos, current, changed, 2);
                }
            } else {
                world.setBlockState(pos, changed, 2);
            }
        }
	}

	@Override
	public int getPasses() {
		return 2;
	}

	@Override
	public boolean shouldRenderItemMulti() {
		return true;
	}



	@Override
	public int getSubCount() {
		return SolarSystem.Body.values().length;
	}

}
