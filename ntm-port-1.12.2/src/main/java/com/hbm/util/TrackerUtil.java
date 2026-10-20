package com.hbm.util;

import com.hbm.interfaces.NotableComments;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * This absolute fucking mess of a class is the direct consequence of mojank's terrible entity tracker that allows for 0 flexibility with how entities are synced.
 *
 * @author hbm
 */
@NotableComments
public class TrackerUtil {

	/** Grabs the tracker entry from the given entity */
	public static EntityTrackerEntry getTrackerEntry(WorldServer world, int entityId) {
		EntityTracker entitytracker = world.getEntityTracker();
		IntHashMap<EntityTrackerEntry> map = ReflectionHelper.getPrivateValue(EntityTracker.class, entitytracker, "trackedEntityHashTable", "field_72794_c");
		EntityTrackerEntry entry = map.lookup(entityId);
		return entry;
	}

	/** Force-teleports the given entity using the tracker, resetting the tick count to 0 to prevent movement during this tick */
	public static void sendTeleport(World world, Entity e) {

		if(world instanceof WorldServer) {
			WorldServer server = (WorldServer) world;
			EntityTrackerEntry entry = getTrackerEntry(server, e.getEntityId());
			// Native 1.12 teleport packet carries double coordinates and on-ground state.
			SPacketEntityTeleport packet = new SPacketEntityTeleport(e);
			// Preserve the original floor quantization, including negative angles; the native
			// Entity constructor otherwise truncates toward zero. Never mutate the entity.
			ReflectionHelper.setPrivateValue(SPacketEntityTeleport.class, packet, (byte)MathHelper.floor(e.rotationYaw * 256.0F / 360.0F), "yaw", "field_149455_e");
			ReflectionHelper.setPrivateValue(SPacketEntityTeleport.class, packet, (byte)MathHelper.floor(e.rotationPitch * 256.0F / 360.0F), "pitch", "field_149453_f");
			entry.sendPacketToTrackedPlayers(packet);
			//this prevents the tracker from sending movement updates in the same tick
			entry.updateCounter = 0;
		}
	}

	public static void setTrackingRange(World world, Entity e, int range) {

		if(world instanceof WorldServer) {
			WorldServer server = (WorldServer) world;
			EntityTrackerEntry entry = getTrackerEntry(server, e.getEntityId());
			if(entry != null) ReflectionHelper.setPrivateValue(EntityTrackerEntry.class, entry, range, "range", "field_73130_b");
		}
	}
}
