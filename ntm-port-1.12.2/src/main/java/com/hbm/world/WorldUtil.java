package com.hbm.world;

import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.BiomeSyncPacket;
import com.hbm.util.Compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class WorldUtil {

	private static final MethodHandle getBiomeShortHandle;

	static {
		if(Loader.isModLoaded(Compat.MOD_EIDS)) {
			try {
				MethodHandles.Lookup lookup = MethodHandles.publicLookup();
				MethodType methodType = MethodType.methodType(short[].class);
				getBiomeShortHandle = lookup.findVirtual(Chunk.class, "getBiomeShortArray", methodType);
			} catch(Exception e) {
				throw new AssertionError();
			}
		} else {
			getBiomeShortHandle = null;
		}
	}

	public static void setBiome(World world, int x, int z, Biome biome) {
		Chunk chunk = world.getChunk(new net.minecraft.util.math.BlockPos(x, 0, z));
		int biomeId = Biome.getIdForBiome(biome);
		if(Loader.isModLoaded(Compat.MOD_EIDS)) {
			short[] array = getBiomeShortArray(chunk);
			array[(z & 15) << 4 | x & 15] = (short) biomeId;
		} else {
			chunk.getBiomeArray()[(z & 15) << 4 | (x & 15)] = (byte)(biomeId & 255);
		}
		chunk.setModified(true);
	}

	public static void syncBiomeChange(World world, int x, int z) {
		Chunk chunk = world.getChunk(new net.minecraft.util.math.BlockPos(x, 0, z));
		if(Loader.isModLoaded(Compat.MOD_EIDS)) {
			PacketDispatcher.wrapper.sendToAllAround(new BiomeSyncPacket(x >> 4, z >> 4, getBiomeShortArray(chunk)), new TargetPoint(world.provider.getDimension(), x, 128, z, 1024D));
		} else {
			PacketDispatcher.wrapper.sendToAllAround(new BiomeSyncPacket(x >> 4, z >> 4, chunk.getBiomeArray()), new TargetPoint(world.provider.getDimension(), x, 128, z, 1024D));
		}
	}

	public static void syncBiomeChangeBlock(World world, int x, int z) {
		Chunk chunk = world.getChunk(new net.minecraft.util.math.BlockPos(x, 0, z));
		if(Loader.isModLoaded(Compat.MOD_EIDS)) {
			short biome = getBiomeShortArray(chunk)[(z & 15) << 4 | (x & 15)];
			PacketDispatcher.wrapper.sendToAllAround(new BiomeSyncPacket(x, z, biome), new TargetPoint(world.provider.getDimension(), x, 128, z, 1024D));
		} else {
			byte biome = chunk.getBiomeArray()[(z & 15) << 4 | (x & 15)];
			PacketDispatcher.wrapper.sendToAllAround(new BiomeSyncPacket(x, z, biome), new TargetPoint(world.provider.getDimension(), x, 128, z, 1024D));
		}
	}

	public static void syncBiomeChange(World world, Chunk chunk) {
		/* "let's not make all this valuable information accessible, at all, hehe hoho huehue" -mojank, probably */
		/*if(!(world instanceof WorldServer)) return;
		WorldServer server = (WorldServer) world;
		PlayerManager man = server.getPlayerManager();
		Method getOrCreateChunkWatcher = ReflectionHelper.findMethod(PlayerManager.class, man, new String[] {"getOrCreateChunkWatcher"}, int.class, int.class, boolean.class);
		int x = chunk.getPos().x;
		int z = chunk.getPos().z;
		PlayerManager.PlayerInstance playerinstance = (PlayerInstance) getOrCreateChunkWatcher.invoke(man, x, z, false);*/

		/* this sucks ass */
		ChunkPos coord = chunk.getPos();
		if(Loader.isModLoaded(Compat.MOD_EIDS)) {
			PacketDispatcher.wrapper.sendToAllAround(new BiomeSyncPacket(coord.x, coord.z, getBiomeShortArray(chunk)), new TargetPoint(world.provider.getDimension(), coord.getXStart() + 8, 128, coord.getZStart() + 8, 1024D));
		} else {
			PacketDispatcher.wrapper.sendToAllAround(new BiomeSyncPacket(coord.x, coord.z, chunk.getBiomeArray()), new TargetPoint(world.provider.getDimension(), coord.getXStart() + 8, 128, coord.getZStart() + 8, 1024D));
		}
	}

	public static short[] getBiomeShortArray(Chunk chunk) {
		try {
			return (short[]) getBiomeShortHandle.invokeExact(chunk);
		} catch(Throwable ex) {
			throw new AssertionError();
		}
	}

	/**Chunkloads the chunk the entity is going to spawn in and then spawns it
	 * @param entity The entity to be spawned**/

	/*fun fact: this is based off of joinEntityInSurroundings in World
	  however, since mojang is staffed by field mice, that function is client side only and half-baked
	 */
	public static void loadAndSpawnEntityInWorld(Entity entity) {

		World world = entity.world;
		int chunkX = MathHelper.floor(entity.posX / 16.0D);
		int chunkZ = MathHelper.floor(entity.posZ / 16.0D);
		byte loadRadius = 2;

		for(int k = chunkX - loadRadius; k <= chunkX + loadRadius; ++k) {
			for(int l = chunkZ - loadRadius; l <= chunkZ + loadRadius; ++l) {
				world.getChunk(k, l);
			}
		}

		if(!world.loadedEntityList.contains(entity)) {
			if(!MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(entity, world))) {
				world.getChunk(chunkX, chunkZ).addEntity(entity);
				world.loadedEntityList.add(entity);
				world.onEntityAdded(entity);
			}
		}
	}

	public static Chunk provideChunk(WorldServer world, int chunkX, int chunkZ) {
		try {
			ChunkProviderServer provider = world.getChunkProvider();
			Chunk chunk = provider.getLoadedChunk(chunkX, chunkZ);
			if(chunk != null) return chunk;
			return loadChunk(world, provider, chunkX, chunkZ);
		} catch(Throwable x) {
			return null;
		}
	}

	private static Chunk loadChunk(WorldServer world, ChunkProviderServer provider, int chunkX, int chunkZ) {
		if(provider.isChunkGeneratedAt(chunkX, chunkZ)) {
			return provider.loadChunk(chunkX, chunkZ);
		}
		return null;
	}
}
