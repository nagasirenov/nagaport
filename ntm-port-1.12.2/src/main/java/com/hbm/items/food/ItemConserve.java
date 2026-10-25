package com.hbm.items.food;

import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import java.util.Locale;

import com.hbm.entity.effect.EntityVortex;
import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ModItems;
import com.hbm.util.EnumUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

//tfw no multiple inheritance
public class ItemConserve extends ItemEnumMulti {

	public ItemConserve() {
		super(EnumFoodType.class, true, true);
	}

	@Override
	public net.minecraft.item.ItemStack onItemUseFinish(ItemStack stack, World world, net.minecraft.entity.EntityLivingBase entity) {
		EntityPlayer player = (EntityPlayer) entity;
		EnumFoodType num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());

		stack.shrink(1);
		player.getFoodStats().addStats(num.foodLevel, num.saturation);
		world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("random.burp"), net.minecraft.util.SoundCategory.PLAYERS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
		this.onFoodEaten(stack, world, player);
		return stack;
	}

	//the fancy enum lambdas and method references and whatever can come later if necessary
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		player.inventory.addItemStackToInventory(new ItemStack(ModItems.can_key));
		EnumFoodType num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());

		if(num == EnumFoodType.BHOLE && !world.isRemote) {
			EntityVortex vortex = (EntityVortex) new EntityVortex(world, 0.5F).setShrinkRate(0.01F).noBreak();
			vortex.posX = player.posX;
			vortex.posY = player.posY;
			vortex.posZ = player.posZ;
			world.spawnEntity(vortex);

		} else if(num == EnumFoodType.RECURSION && world.rand.nextInt(10) > 0) {
			player.inventory.addItemStackToInventory(stackFromEnum(EnumFoodType.RECURSION));

		} else if(num == EnumFoodType.FIST) {
			player.attackEntityFrom(DamageSource.MAGIC, 2F);
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 32;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.EAT;
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}


	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(player.canEat(false))
			player.setActiveHand(net.minecraft.util.EnumHand.MAIN_HAND);

		return stack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
		String unloc = this.getTranslationKey(itemstack) + ".desc";
		String loc = I18nUtil.resolveKey(unloc);

		if(!unloc.equals(loc)) {
			String[] locs = loc.split("\\$");

			for(String s : locs) {
				list.add(s);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		// The original built conserve icons from the setTextureName base "canned".
		String base = com.hbm.render.item.NativeItemTextureRegistry.texture(this);
		if(base == null) base = this.getIconString();
		if(base == null) base = this.getRegistryName().toString();
		Enum[] enums = theEnum.getEnumConstants();
		this.icons = new IIcon[enums.length];

		for(int i = 0; i < icons.length; i++) {
			Enum num = enums[i];
			this.icons[i] = reg.registerIcon(base + "_" + num.name().toLowerCase(Locale.US));
		}
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		Enum num = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
		return "item.canned_" + num.name().toLowerCase(Locale.US);
	}

	public static enum EnumFoodType {
		BEEF(8, 0.75F),
		TUNA(4, 0.75F),
		MYSTERY(6, 0.5F),
		PASHTET(4, 0.5F),
		CHEESE(3, 1F),
		SLIME(15, 5F),
		MILK(5, 0.25F),
		ASS(6, 0.75F), // :3
		PIZZA(8, 075F),
		TUBE(2, 0.25F),
		TOMATO(4, 0.5F),
		ASBESTOS(7, 1F),
		BHOLE(10, 1F),
		HOTDOGS(5, 0.75F),
		LEFTOVERS(1, 0.1F),
		YOGURT(3, 0.5F),
		STEW(5, 0.5F),
		CHINESE(6, 0.1F),
		OIL(3, 1F),
		FIST(6, 0.75F),
		SPAM(8, 1F),
		FRIED(10, 0.75F),
		NAPALM(6, 1F),
		DIESEL(6, 1F),
		KEROSENE(6, 1F),
		RECURSION(1, 1F),
		BARK(2, 1F);

		protected int foodLevel;
		protected float saturation;

		private EnumFoodType(int level, float sat) {
			this.foodLevel = level;
			this.saturation = sat;
		}
	}
}
