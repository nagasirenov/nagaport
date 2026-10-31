package com.hbm.tileentity.machine.albion;

import java.util.List;

import com.hbm.handler.CompatHandler;
import com.hbm.inventory.container.ContainerPADetector;
import com.hbm.inventory.gui.GUIPADetector;
import com.hbm.inventory.recipes.ParticleAcceleratorRecipes;
import com.hbm.inventory.recipes.ParticleAcceleratorRecipes.ParticleAcceleratorRecipe;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.albion.TileEntityPASource.PAState;
import com.hbm.tileentity.machine.albion.TileEntityPASource.Particle;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityPADetector extends TileEntityCooledBase implements IGUIProvider, IParticleUser, SimpleComponent, CompatHandler.OCComponent {

	public static final long usage = 100_000;

	public TileEntityPADetector() {
		super(5);
	}

	@Override
	public String getDefaultName() {
		return "container.paDetector";
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {
			this.power = Library.chargeTEFromItems(slots, 0, power, this.getMaxPower());
		}

		super.update();
	}

	@Override
	public DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		return new DirPos[] {
				new DirPos(this.pos.getX() - rot.offsetX * 5, this.pos.getY(), this.pos.getZ() - rot.offsetZ * 5, rot.getOpposite()),
				new DirPos(this.pos.getX() - rot.offsetX * 5, this.pos.getY() + 1, this.pos.getZ() - rot.offsetZ * 5, rot.getOpposite()),
				new DirPos(this.pos.getX() - rot.offsetX * 5, this.pos.getY() - 1, this.pos.getZ() - rot.offsetZ * 5, rot.getOpposite()),
				new DirPos(this.pos.getX() - rot.offsetX * 5 + dir.offsetX, this.pos.getY(), this.pos.getZ() - rot.offsetZ * 5 + dir.offsetZ, rot.getOpposite()),
				new DirPos(this.pos.getX() - rot.offsetX * 5 - dir.offsetX, this.pos.getY(), this.pos.getZ() - rot.offsetZ * 5 - dir.offsetZ, rot.getOpposite()),
		};
	}

	@Override
	public long getMaxPower() {
		return 1_000_000;
	}

	@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return slot == 1 || slot == 2; }
	@Override public boolean canExtractItem(int slot, ItemStack stack, net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal(); return slot == 3 || slot == 4; }
	@Override public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal(); return new int[] { 1, 2, 3, 4 }; }

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					this.pos.getX() - 4,
					this.pos.getY() - 2,
					this.pos.getZ() - 4,
					this.pos.getX() + 5,
					this.pos.getY() + 3,
					this.pos.getZ() + 5
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerPADetector(player.inventory, this);
	}

	@Override
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIPADetector(player.inventory, this);
	}

	@Override
	public boolean canParticleEnter(Particle particle, ForgeDirection dir, int x, int y, int z) {
		ForgeDirection detectorDir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getRotation(ForgeDirection.DOWN);
		BlockPos input = new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()).offset(detectorDir, -4);
		return input.compare(x, y, z) && detectorDir == dir;
	}

	@Override
	public void onEnter(Particle particle, ForgeDirection dir) {
		particle.invalid = true;
		//particle will crash if not perfectly focused
		if(particle.defocus > 0) { particle.crash(PAState.CRASH_DEFOCUS); return; }
		if(this.power < usage) { particle.crash(PAState.CRASH_NOPOWER); return; }
		if(!isCool()) { particle.crash(PAState.CRASH_NOCOOL); return; }
		this.power -= usage;

		for(ParticleAcceleratorRecipe recipe : ParticleAcceleratorRecipes.recipes) {
			if(!recipe.matchesRecipe(particle.input1, particle.input2)) continue; // another W for continue

			if(particle.momentum < recipe.momentum) {
				particle.crash(PAState.CRASH_UNDERSPEED);
				return;
			}

			if(canAccept(recipe)) {
				if(recipe.output1.getItem().hasContainerItem(recipe.output1)) this.decrStackSize(1, 1);
				if(recipe.output2 != null && recipe.output2.getItem().hasContainerItem(recipe.output2)) this.decrStackSize(2, 1);

				if(slots[3] == null) {
					slots[3] = recipe.output1.copy();
				} else {
					slots[3].grow(recipe.output1.getCount());
				}

				if(recipe.output2 != null) {
					if(slots[4] == null) {
						slots[4] = recipe.output2.copy();
					} else {
						slots[4].grow(recipe.output2.getCount());
					}
				}
			}

			if((recipe.output1 != null && recipe.output1.getItem() == ModItems.particle_digamma) || (recipe.output2 != null && recipe.output2.getItem() == ModItems.particle_digamma)) {
				List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5).grow(100, 50, 100));
				for(EntityPlayer player : players) com.hbm.advancement.NtmAdvancements.award(player, MainRegistry.achOmega12);
			}

			particle.crash(PAState.SUCCESS);
			return;
		}

		particle.crash(PAState.CRASH_NORECIPE);
	}

	public boolean canAccept(ParticleAcceleratorRecipe recipe) {
		return checkSlot(recipe.output1, 1, 3) && checkSlot(recipe.output2, 2, 4);
	}

	public boolean checkSlot(ItemStack output, int containerSlot, int outputSlot) {
		if(output != null) {
			if(slots[outputSlot] != null) {
				//cancel if: output item does not match, meta does not match, resulting stacksize exceeds stack limit
				if(slots[outputSlot].getItem() != output.getItem() || slots[outputSlot].getItemDamage() != output.getItemDamage() || slots[outputSlot].getCount() + output.getCount() > output.getMaxStackSize()) return false;
			}
			if(output.getItem().hasContainerItem(output)) {
				ItemStack container = output.getItem().getContainerItem(output);
				//cancel if: container slot is empty, container item does not match, meta does not match
				if(slots[containerSlot] == null || slots[containerSlot].getItem() != container.getItem() || slots[containerSlot].getItemDamage() != container.getItemDamage()) return false;
			}
		}

		return true;
	}

	@Override
	public BlockPos getExitPos(Particle particle) {
		return null;
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "ntm_pa_detector";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getEnergyInfo(Context context, Arguments args) {
		return new Object[] {getPower(), getMaxPower()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getCoolant(Context context, Arguments args) {
		return new Object[] {
			coolantTanks[0].getFill(), coolantTanks[0].getMaxFill(),
			coolantTanks[1].getFill(), coolantTanks[1].getMaxFill(),
		};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getCrafting(Context context, Arguments args) {
		Object[] items = new Object[] {"", 0, "", 0, "", 0, "", 0};
		for (int i = 0; i < 4; i++) {
			ItemStack slot = slots[i+1];
			if (slot != null) {
				items[i*2] = slot.getTranslationKey();
				items[(i*2)+1] = slot.getCount();
			}
		}
		return items;
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getInfo(Context context, Arguments args) {
		Object[] items = new Object[] {"", 0, "", 0, "", 0, "", 0};
		for (int i = 0; i < 4; i++) {
			ItemStack slot = slots[i+1];
			if (slot != null) {
				items[i*2] = slot.getTranslationKey();
				items[(i*2)+1] = slot.getCount();
			}
		}

		return new Object[] {
			getPower(), getMaxPower(),

			coolantTanks[0].getFill(), coolantTanks[0].getMaxFill(),
			coolantTanks[1].getFill(), coolantTanks[1].getMaxFill(),

			items[0], items[1], items[2], items[3],
			items[4], items[5], items[6], items[7]
		};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String[] methods() {
		return new String[] {
			"getEnergyInfo",
			"getCoolant",
			"getCrafting",
			"getInfo"
		};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public Object[] invoke(String method, Context context, Arguments args) throws Exception {
		switch (method) {
			case "getEnergyInfo": return getEnergyInfo(context, args);
			case "getCoolant": return getCoolant(context, args);
			case "getCrafting": return getCrafting(context, args);
			case "getInfo": return getInfo(context, args);
		}
		throw new NoSuchMethodException();
	}
}
