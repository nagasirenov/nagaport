package com.hbm.render.item;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.Fluids.CD_Canister;
import com.hbm.inventory.fluid.Fluids.CD_Gastank;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemCanister;
import com.hbm.items.machine.ItemFluidDuct;
import com.hbm.items.machine.ItemFluidIDMulti;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.items.machine.ItemFluidIdentifier;
import com.hbm.items.machine.ItemFluidTank;
import com.hbm.items.machine.ItemFluidTemplate;
import com.hbm.items.machine.ItemGasTank;
import com.hbm.items.special.ItemAutogen;
import com.hbm.items.tool.ItemPipette;
import com.hbm.lib.RefStrings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * ItemColors and IItemColor bridge for fluid identifiers, fluid templates,
 * pipes, tanks, canisters, pipettes, and all items with legacy color calculations.
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class ModItemColors implements IItemColor {

	public static final ModItemColors INSTANCE = new ModItemColors();

	private static final Map<Class<?>, Method> COLOR_METHOD_CACHE = new IdentityHashMap<>();
	private static final Method NO_METHOD;

	static {
		Method dummy = null;
		try {
			dummy = Object.class.getDeclaredMethod("hashCode");
		} catch(NoSuchMethodException ignored) { }
		NO_METHOD = dummy;
	}

	private ModItemColors() { }

	@Override
	public int colorMultiplier(ItemStack stack, int tintIndex) {
		if(stack == null || stack.isEmpty()) return 0xFFFFFF;

		Item item = stack.getItem();

		if(item instanceof IItemFluidIdentifier) {
			if(tintIndex == 0) {
				return 0xFFFFFF;
			} else if(tintIndex == 1) {
				FluidType primary;
				if(item instanceof ItemFluidIDMulti) {
					primary = ItemFluidIDMulti.getType(stack, true);
					if(primary == Fluids.NONE && stack.getItemDamage() > 0) {
						primary = Fluids.fromID(stack.getItemDamage());
					}
				} else if(item instanceof ItemFluidIdentifier) {
					primary = ItemFluidIdentifier.getType(stack);
				} else if(item instanceof ItemFluidTemplate) {
					primary = ItemFluidTemplate.getType(stack);
				} else {
					primary = ((IItemFluidIdentifier) item).getType(null, 0, 0, 0, stack);
					if(primary == null || primary == Fluids.NONE) {
						primary = Fluids.fromID(stack.getItemDamage());
					}
				}
				if(primary == null || primary == Fluids.NONE) return 0xFFFFFF;
				int color = primary.getColor();
				return color < 0 ? 0xFFFFFF : color;
			}
			return 0xFFFFFF;
		}

		if(item instanceof ItemFluidDuct) {
			if(tintIndex == 0) {
				return 0xFFFFFF;
			} else if(tintIndex == 1) {
				FluidType fluid = Fluids.fromID(stack.getItemDamage());
				if(fluid == null || fluid == Fluids.NONE) return 0xFFFFFF;
				int color = fluid.getColor();
				return color < 0 ? 0xFFFFFF : color;
			}
			return 0xFFFFFF;
		}

		if(item instanceof ItemFluidIcon) {
			if(tintIndex == 0) {
				FluidType fluid = Fluids.fromID(stack.getItemDamage());
				if(fluid == null || fluid == Fluids.NONE) return 0xFFFFFF;
				int color = fluid.getColor();
				return color < 0 ? 0xFFFFFF : color;
			}
			return 0xFFFFFF;
		}

		if(item instanceof ItemAutogen) {
			return ((ItemAutogen) item).getColorFromItemStack(stack, tintIndex);
		}

		if(item instanceof ItemFluidTank) {
			if(tintIndex == 0) {
				return 0xFFFFFF;
			} else if(tintIndex == 1) {
				FluidType fluid = Fluids.fromID(stack.getItemDamage());
				if(fluid == null || fluid == Fluids.NONE) return 0xFFFFFF;
				int color = fluid.getColor();
				return color < 0 ? 0xFFFFFF : color;
			}
			return 0xFFFFFF;
		}

		if(item instanceof ItemCanister) {
			if(tintIndex == 0) {
				return 0xFFFFFF;
			} else if(tintIndex == 1) {
				CD_Canister canister = Fluids.fromID(stack.getItemDamage()).getContainer(CD_Canister.class);
				int color = canister == null ? -1 : canister.color;
				return color < 0 ? 0xFFFFFF : color;
			}
			return 0xFFFFFF;
		}

		if(item instanceof ItemPipette) {
			if(tintIndex == 0) {
				return 0xFFFFFF;
			} else if(tintIndex == 1) {
				FluidType fluid = ((ItemPipette) item).getType(stack);
				if(fluid == null || fluid == Fluids.NONE) return 0xFFFFFF;
				int color = fluid.getColor();
				return color < 0 ? 0xFFFFFF : color;
			}
			return 0xFFFFFF;
		}

		if(item instanceof ItemGasTank) {
			if(tintIndex == 0) {
				return 0xFFFFFF;
			} else {
				CD_Gastank tank = Fluids.fromID(stack.getItemDamage()).getContainer(CD_Gastank.class);
				if(tank == null) return 0xFFFFFF;
				return tintIndex == 1 ? tank.bottleColor : tank.labelColor;
			}
		}

		Method method = findColorMethod(item.getClass());
		if(method != null) {
			try {
				Object result = method.invoke(item, stack, tintIndex);
				if(result instanceof Integer) {
					return (Integer) result;
				}
			} catch(Exception ignored) { }
		}

		return 0xFFFFFF;
	}

	private static Method findColorMethod(Class<?> clazz) {
		synchronized(COLOR_METHOD_CACHE) {
			Method cached = COLOR_METHOD_CACHE.get(clazz);
			if(cached != null) return cached == NO_METHOD ? null : cached;

			for(Class<?> cur = clazz; cur != null && cur != Item.class; cur = cur.getSuperclass()) {
				try {
					Method m = cur.getDeclaredMethod("getColorFromItemStack", ItemStack.class, int.class);
					m.setAccessible(true);
					COLOR_METHOD_CACHE.put(clazz, m);
					return m;
				} catch(NoSuchMethodException ignored) { }
			}

			COLOR_METHOD_CACHE.put(clazz, NO_METHOD);
			return null;
		}
	}

	@SubscribeEvent
	public static void onColorHandler(ColorHandlerEvent.Item event) {
		registerItemColors(event.getItemColors());
	}

	public static void registerItemColors() {
		if(Minecraft.getMinecraft() != null && Minecraft.getMinecraft().getItemColors() != null) {
			registerItemColors(Minecraft.getMinecraft().getItemColors());
		}
	}

	public static void registerItemColors(ItemColors itemColors) {
		if(itemColors == null) return;

		Set<Item> registered = Collections.newSetFromMap(new IdentityHashMap<>());

		if(ModItems.fluid_identifier_multi != null && registered.add(ModItems.fluid_identifier_multi)) {
			itemColors.registerItemColorHandler(INSTANCE, ModItems.fluid_identifier_multi);
		}
		if(ModItems.fluid_icon != null && registered.add(ModItems.fluid_icon)) {
			itemColors.registerItemColorHandler(INSTANCE, ModItems.fluid_icon);
		}
		if(ModItems.fluid_duct != null && registered.add(ModItems.fluid_duct)) {
			itemColors.registerItemColorHandler(INSTANCE, ModItems.fluid_duct);
		}
		if(ModItems.pipe != null && registered.add(ModItems.pipe)) {
			itemColors.registerItemColorHandler(INSTANCE, ModItems.pipe);
		}
		if(ModItems.fluid_tank_full != null && registered.add(ModItems.fluid_tank_full)) {
			itemColors.registerItemColorHandler(INSTANCE, ModItems.fluid_tank_full);
		}
		if(ModItems.fluid_tank_lead_full != null && registered.add(ModItems.fluid_tank_lead_full)) {
			itemColors.registerItemColorHandler(INSTANCE, ModItems.fluid_tank_lead_full);
		}
		if(ModItems.fluid_barrel_full != null && registered.add(ModItems.fluid_barrel_full)) {
			itemColors.registerItemColorHandler(INSTANCE, ModItems.fluid_barrel_full);
		}
		if(ModItems.fluid_pack_full != null && registered.add(ModItems.fluid_pack_full)) {
			itemColors.registerItemColorHandler(INSTANCE, ModItems.fluid_pack_full);
		}
		if(ModItems.canister_full != null && registered.add(ModItems.canister_full)) {
			itemColors.registerItemColorHandler(INSTANCE, ModItems.canister_full);
		}
		if(ModItems.pipette != null && registered.add(ModItems.pipette)) {
			itemColors.registerItemColorHandler(INSTANCE, ModItems.pipette);
		}
		if(ModItems.pipette_boron != null && registered.add(ModItems.pipette_boron)) {
			itemColors.registerItemColorHandler(INSTANCE, ModItems.pipette_boron);
		}
		if(ModItems.pipette_laboratory != null && registered.add(ModItems.pipette_laboratory)) {
			itemColors.registerItemColorHandler(INSTANCE, ModItems.pipette_laboratory);
		}

		for(Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
			if(item == null) continue;
			if(registered.contains(item)) continue;

			if(item instanceof IItemFluidIdentifier
					|| item instanceof ItemFluidIdentifier
					|| item instanceof ItemFluidTemplate
					|| item instanceof ItemFluidDuct
					|| item instanceof ItemFluidIcon
					|| item instanceof ItemAutogen
					|| item instanceof ItemFluidTank
					|| item instanceof ItemCanister
					|| item instanceof ItemPipette
					|| item instanceof ItemGasTank
					|| findColorMethod(item.getClass()) != null) {
				registered.add(item);
				itemColors.registerItemColorHandler(INSTANCE, item);
			}
		}
	}
}
