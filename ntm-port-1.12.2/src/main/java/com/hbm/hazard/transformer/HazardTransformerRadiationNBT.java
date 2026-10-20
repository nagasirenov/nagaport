package com.hbm.hazard.transformer;

import java.util.List;

import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.type.HazardTypeNeutron;

import net.minecraft.item.ItemStack;

public class HazardTransformerRadiationNBT extends HazardTransformerBase {
	
	public static final String RAD_KEY = "hfrHazRadiation";

	@Override
	public void transformPre(ItemStack stack, List<HazardEntry> entries) { }

	@Override
	public void transformPost(ItemStack stack, List<HazardEntry> entries) {
		float level = 0;
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey(RAD_KEY)) {
			level += stack.getTagCompound().getFloat(RAD_KEY);
		}
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey(HazardTypeNeutron.NEUTRON_KEY)) {
			level += stack.getTagCompound().getFloat(HazardTypeNeutron.NEUTRON_KEY);
		}
		entries.add(new HazardEntry(HazardRegistry.RADIATION, level));
	}

}
