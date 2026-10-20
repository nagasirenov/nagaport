import java.util.Objects;
import java.util.OptionalInt;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.util.math.ChunkPos;
/** Evidence-only capability experiment. NOT production fallback or activation. */
public final class OwnedHeightCandidate {
 public static OptionalInt assess(World world,int x,int z) {
  Objects.requireNonNull(world);
  if(world.getClass()!=WorldServer.class&&world.getClass()!=WorldServerMulti.class)return OptionalInt.empty();
  if(x < -30000000||z < -30000000||x >= 30000000||z >= 30000000)return OptionalInt.of(64);
  ChunkProviderServer provider=((WorldServer)world).getChunkProvider();
  if(provider==null||provider.getClass()!=ChunkProviderServer.class||provider.world!=world)return OptionalInt.empty();
  long key=ChunkPos.asLong(x>>4,z>>4);
  if(!provider.loadedChunks.containsKey(key))return OptionalInt.of(0);
  Chunk chunk=provider.loadedChunks.get(key);
  // Public-map corruption or a race is not silently interpreted as absent.
  if(chunk==null)return OptionalInt.empty();
  return OptionalInt.of(chunk.getHeightValue(x&15,z&15));
 }
}
