package com.hbm.items.special;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hbm.lib.RefStrings;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockJukebox;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ItemModRecord extends ItemRecord {

	private static final Map modRecords = new HashMap();
	public final String recordName;

	public ItemModRecord(String string) {
		super(string, com.hbm.main.ModSoundEvents.resolve(getRecordResource("records." + string).toString()));
		recordName = string;
		modRecords.put(string, this);
	}


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack p_77648_1_, EntityPlayer p_77648_2_, World p_77648_3_, int p_77648_4_, int p_77648_5_, int p_77648_6_, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {

		if(com.hbm.world.WorldBlockState.read(p_77648_3_, p_77648_4_, p_77648_5_, p_77648_6_).getBlock() == Blocks.JUKEBOX && com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(p_77648_3_, p_77648_4_, p_77648_5_, p_77648_6_) ) == 0) {

			if(p_77648_3_.isRemote) {
				return true;

			} else {
				net.minecraft.tileentity.TileEntity jukeTe = p_77648_3_.getTileEntity(new net.minecraft.util.math.BlockPos(p_77648_4_, p_77648_5_, p_77648_6_)); if(jukeTe instanceof net.minecraft.block.BlockJukebox.TileEntityJukebox) ((net.minecraft.block.BlockJukebox.TileEntityJukebox) jukeTe).setRecord(p_77648_1_);
				p_77648_3_.playEvent((EntityPlayer) null, 1005, new net.minecraft.util.math.BlockPos(p_77648_4_, p_77648_5_, p_77648_6_), Item.getIdFromItem(this));
				p_77648_1_.shrink(1);
				return true;
			}

		} else {
			return false;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack p_77624_1_, net.minecraft.world.World tooltipWorld, List<String> p_77624_3_, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		p_77624_3_.add(this.getRecordNameLocal());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getRecordNameLocal() {
		return I18nUtil.resolveKey("item.record." + this.recordName + ".desc");
	}

	/**
	 * Return an item rarity from EnumRarity
	 */
	@Override
	public EnumRarity getRarity(ItemStack p_77613_1_) {
		return EnumRarity.RARE;
	}

	/**
	 * Return the record item corresponding to the given name.
	 */
	@SideOnly(Side.CLIENT)
	public static ItemRecord getRecord(String p_150926_0_) {
		return (ItemRecord) modRecords.get(p_150926_0_);
	}

	public static ResourceLocation getRecordResource(String name) {

		String s = "";

		if(name.equals("records.lc"))
			s = RefStrings.MODID + ":music.recordLambdaCore";
		else if(name.equals("records.ss"))
			s = RefStrings.MODID + ":music.recordSectorSweep";
		else if(name.equals("records.vc"))
			s = RefStrings.MODID + ":music.recordVortalCombat";
		else if(name.equals("records.glass"))
			s = RefStrings.MODID + ":music.transmission";
		else if(name.equals("records.gs"))
			s = RefStrings.MODID + ":music.recordGodSpeed";
		else if(name.equals("records.gp"))
			s = RefStrings.MODID + ":music.recordGoop";
		else if(name.equals("records.el"))
			s = RefStrings.MODID + ":music.recordEthereal";
		else
			throw new IllegalArgumentException("Unknown NTM record sound: " + name);
		return new ResourceLocation(s);
	}

	@Override
	public String getItemStackDisplayName(ItemStack p_77653_1_) {
		String s = (I18nUtil.resolveKey(Items.RECORD_11.getTranslationKey() + ".name")).trim();

		return s;
	}
}
