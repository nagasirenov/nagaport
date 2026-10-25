package com.hbm.items.tool;

import java.util.List;
import java.util.Locale;

import com.hbm.entity.cart.*;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.util.EnumUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class ItemModMinecart extends Item {

	private IIcon[] icons;
	private IIcon[] bases = new IIcon[4];

	public static final String CART_BASE_NBT = "cartBase";

	public static enum EnumCartBase {
		VANILLA,
		WOOD,
		STEEL,
		PAINTED
	}

	public static enum EnumMinecart {
		EMPTY		(EnumCartBase.WOOD, EnumCartBase.STEEL, EnumCartBase.PAINTED),
		CRATE		(EnumCartBase.VANILLA),
		DESTROYER	(EnumCartBase.STEEL, EnumCartBase.PAINTED),
		POWDER		(EnumCartBase.WOOD, EnumCartBase.STEEL, EnumCartBase.PAINTED),
		SEMTEX		(EnumCartBase.WOOD, EnumCartBase.STEEL, EnumCartBase.PAINTED);

		public int types;

		private EnumMinecart(EnumCartBase... types) {
			this.types = 0;
			for(EnumCartBase type : types) {
				this.types |= (1 << type.ordinal());
			}
		}

		private EnumMinecart(int types) {
			this.types = types;
		}

		public boolean supportsBase(int type) {
			return (this.types & (1 << type)) > 0;
		}

		public boolean supportsBase(EnumCartBase type) {
			return supportsBase(type.ordinal());
		}

		/*public boolean isVanilla() {
			return isType(0);
		}

		public boolean isSteel() {
			return isType(1);
		}

		public boolean isWood() {
			return isType(2);
		}

		public boolean isPainted() {
			return isType(3);
		}*/
	}

	public ItemModMinecart() {
		this.setMaxStackSize(4);
		this.setCreativeTab(CreativeTabs.TRANSPORTATION);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, dispenseBehavior);
	}

	@Override
	public Item setTranslationKey(String unlocalizedName) {
		super.setTranslationKey(unlocalizedName);
		com.hbm.render.item.NativeItemTextureRegistry.texture(this, RefStrings.MODID + ":"+ unlocalizedName);
		return this;
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		EnumMinecart cart = EnumUtil.grabEnumSafely(EnumMinecart.class, stack.getItemDamage());
		return super.getTranslationKey() + "." + cart.name().toLowerCase(Locale.US);
	}


		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void getSubItems(net.minecraft.creativetab.CreativeTabs tab, net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> list) {
		if(this.isInCreativeTab(tab)) {
			getSubItems(this, tab, list);
		}
	}

@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for(int i = 0; i < EnumMinecart.values().length; i++) {
			EnumMinecart cart = EnumMinecart.values()[i];

			for(EnumCartBase base : EnumCartBase.values()) {
				if(cart.supportsBase(base)) {
					list.add(createCartItem(base, cart));
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {

		for(int i = 0; i < EnumCartBase.values().length; i++) {
			EnumCartBase base = EnumCartBase.values()[i];
			bases[i] = reg.registerIcon(com.hbm.render.item.NativeItemTextureRegistry.texture(this) + "." + base.name().toLowerCase(Locale.US));
		}

		EnumMinecart[] enums = EnumMinecart.values();
		this.icons = new IIcon[enums.length];

		for(int i = 0; i < icons.length; i++) {
			Enum num = enums[i];
			this.icons[i] = reg.registerIcon(com.hbm.render.item.NativeItemTextureRegistry.texture(this) + "_overlay." + num.name().toLowerCase(Locale.US));
		}
	}

	public static EnumCartBase getBaseType(ItemStack stack) {
		if(!stack.hasTagCompound())
			return EnumCartBase.VANILLA;

		int meta = stack.getTagCompound().getInteger(CART_BASE_NBT);
		return EnumUtil.grabEnumSafely(EnumCartBase.class, meta);
	}

	public static ItemStack createCartItem(EnumCartBase base, EnumMinecart cart) {
		ItemStack stack = new ItemStack(ModItems.cart, 1, cart.ordinal());
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger(CART_BASE_NBT, base.ordinal());
		return stack;
	}

	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int pass) {

		if(pass == 0) {
			EnumCartBase base = getBaseType(stack);
			return this.bases[base.ordinal()];
		}

		return this.icons[stack.getItemDamage()];
	}

	private static final IBehaviorDispenseItem dispenseBehavior = new BehaviorDefaultDispenseItem() {
		private final BehaviorDefaultDispenseItem behaviourDefaultDispenseItem = new BehaviorDefaultDispenseItem();

		@Override
		public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
			EnumFacing enumfacing = source.getBlockState().getValue(BlockDispenser.FACING);
			World world = source.getWorld();
			double x = source.getX() + enumfacing.getXOffset() * 1.125D;
			double y = source.getY() + enumfacing.getYOffset() * 1.125D;
			double z = source.getZ() + enumfacing.getZOffset() * 1.125D;
			BlockPos railPos = source.getBlockPos().offset(enumfacing);
			int iX = railPos.getX();
			int iY = railPos.getY();
			int iZ = railPos.getZ();
			Block block = com.hbm.world.WorldBlockState.read(world, iX, iY, iZ).getBlock();
			double yOffset;

			if(BlockRailBase.isRailBlock(world.getBlockState(railPos))) {
				yOffset = 0.0D;
			} else {
				if(block.getDefaultState().getMaterial() != Material.AIR || !BlockRailBase.isRailBlock(world.getBlockState(railPos.down()))) {
					return this.behaviourDefaultDispenseItem.dispense(source, stack);
				}

				yOffset = -1.0D;
			}

			EntityMinecart entityminecart = createMinecart(world, x, y + yOffset, z, stack);

			if(stack.hasDisplayName()) {
				entityminecart.setCustomNameTag(stack.getDisplayName());
			}

			world.spawnEntity(entityminecart);
			stack.splitStack(1);
			return stack;
		}

		protected void playDispenseSound(IBlockSource source) {
			source.getWorld().playEvent(1000, source.getBlockPos(), 0);
		}
	};


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer entity, World world, int x, int y, int z, int side, float fx, float fy, float fz) {
		if(BlockRailBase.isRailBlock(world, new net.minecraft.util.math.BlockPos(x, y, z))) {
			if(!world.isRemote) {

				EntityMinecart entityminecart = createMinecart(world, x + fx, y + fy, z + fz, stack);

				if(stack.hasDisplayName()) {
					entityminecart.setCustomNameTag(stack.getDisplayName());
				}

				world.spawnEntity(entityminecart);
			}

			stack.shrink(1);
			return true;
		} else {
			return false;
		}
	}

	public static EntityMinecart createMinecart(World world, double x, double y, double z, ItemStack stack) {
		EnumMinecart type = (EnumMinecart) EnumMinecart.values()[stack.getItemDamage()];
		EnumCartBase base = getBaseType(stack);
		switch(type) {
		case CRATE: return new EntityMinecartCrate(world, x, y, z, base, stack);
		case DESTROYER: return new EntityMinecartDestroyer(world, x, y, z, base);
		case EMPTY: return new EntityMinecartOre(world, x, y, z, base);
		case POWDER: return new EntityMinecartPowder(world, x, y, z, base);
		case SEMTEX: return new EntityMinecartSemtex(world, x, y, z, base);
		default: return new EntityMinecartEmpty(world, x, y, z);
		}
	}
}
