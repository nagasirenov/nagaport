package com.hbm.items.special;

import java.util.List;

import com.hbm.blocks.rail.IRailNTM;
import com.hbm.blocks.rail.IRailNTM.MoveContext;
import com.hbm.blocks.rail.IRailNTM.RailCheckType;
import com.hbm.entity.train.EntityRailCarBase;
import com.hbm.entity.train.TrainCargoTram;
import com.hbm.entity.train.TrainCargoTramTrailer;
import com.hbm.items.ItemEnumMulti;
import com.hbm.util.EnumUtil;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemTrain extends ItemEnumMulti {

	public ItemTrain() {
		super(EnumTrainType.class, true, true);
		this.setCreativeTab(null);//CreativeTabs.tabTransport);
		this.setMaxStackSize(1);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		EnumTrainType train = EnumUtil.grabEnumSafely(this.theEnum, stack.getItemDamage());

		if(train.engine != null) list.add(TextFormatting.GREEN + "Engine: " + TextFormatting.RESET + train.engine);
		list.add(TextFormatting.GREEN + "Gauge: " + TextFormatting.RESET + train.gauge);
		if(train.maxSpeed != null) list.add(TextFormatting.GREEN + "Max Speed: " + TextFormatting.RESET + train.maxSpeed);
		if(train.acceleration != null) list.add(TextFormatting.GREEN + "Acceleration: " + TextFormatting.RESET + train.acceleration);
		if(train.brakeThreshold != null) list.add(TextFormatting.GREEN + "Engine Brake Threshold: " + TextFormatting.RESET + train.brakeThreshold);
		if(train.parkingBrake != null) list.add(TextFormatting.GREEN + "Parking Brake: " + TextFormatting.RESET + train.parkingBrake);
	}

	public static enum EnumTrainType {
		
		//                                              Engine          Gauge               Max Speed   Accel.      Eng. Brake  Parking Brake
		CARGO_TRAM(TrainCargoTram.class, 				"Electric",		"Standard Gauge",	"10m/s",	"0.2m/s²",	"<1m/s",	"Yes"),
		CARGO_TRAM_TRAILER(TrainCargoTramTrailer.class,	null,			"Standard Gauge",	"Yes",		null,		null,		"No");
		
		public Class<? extends EntityRailCarBase> train;
		public String engine;
		public String maxSpeed;
		public String acceleration;
		public String brakeThreshold;
		public String parkingBrake;
		public String gauge;
		private EnumTrainType(Class<? extends EntityRailCarBase> train, String engine, String gauge, String maxSpeed, String acceleration, String brakeThreshold, String parkingBrake) {
			this.train = train;
			this.engine = engine;
			this.maxSpeed = maxSpeed;
			this.acceleration = acceleration;
			this.brakeThreshold = brakeThreshold;
			this.parkingBrake = parkingBrake;
			this.gauge = gauge;
		}
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer entity, World world, int x, int y, int z, int side, float fx, float fy, float fz) {
		
		Block b = world.getBlock(x, y, z);
		
		if(b instanceof IRailNTM) {
			
			EnumTrainType type = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());
			EntityRailCarBase train = null;
			try { train = type.train.getConstructor(World.class).newInstance(world); } catch(Exception e) { }
			
			if(train != null && train.getGauge() == ((IRailNTM) b).getGauge(world, x, y, z)) {
				
				train.setPosition(x + fx, y + fy, z + fz);
				BlockPos anchor = train.getCurrentAnchorPos();
				train.rotationYaw = entity.rotationYaw;
				Vec3d corePos = train.getRelPosAlongRail(anchor, 0, new MoveContext(RailCheckType.CORE, 0));
				if(corePos != null) {
					train.setPosition(corePos.x, corePos.y, corePos.z);
					Vec3d frontPos = train.getRelPosAlongRail(anchor, train.getLengthSpan(), new MoveContext(RailCheckType.FRONT, train.getCollisionSpan() - train.getLengthSpan()));
					Vec3d backPos = train.getRelPosAlongRail(anchor, -train.getLengthSpan(), new MoveContext(RailCheckType.BACK, train.getCollisionSpan() - train.getLengthSpan()));
					if(frontPos != null && backPos != null) {
						if(!world.isRemote) {
							train.rotationYaw = train.generateYaw(frontPos, backPos);
							world.spawnEntityInWorld(train);
						}
						stack.stackSize--;
						return true;
					}
				}
			}
		}
		
		return false;
	}
}
