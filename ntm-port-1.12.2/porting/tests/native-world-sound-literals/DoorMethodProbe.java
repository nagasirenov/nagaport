import java.lang.reflect.*;import java.util.*;import net.minecraft.world.*;import net.minecraft.tileentity.TileEntity;import net.minecraft.util.math.BlockPos;import net.minecraft.util.ResourceLocation;import net.minecraft.util.SoundEvent;import net.minecraftforge.fml.common.registry.ForgeRegistries;import com.hbm.main.ModSoundEvents;import sun.misc.Unsafe;
/** Exact complete two production methods, actual native TileEntity/World base; unrelated door owner omitted. */
public final class DoorMethodProbe extends TileEntity { public int state;
public void finishOpen() {
		state = 2;

		com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:block.reactorStop", 0.5F,
				1.0F);
	}
public void finishClose() {
		state = 0;

		com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:block.reactorStop", 0.5F,
				1.0F);
	}
public static void main(String[] args)throws Exception {net.minecraft.init.Bootstrap.register();ModSoundEvents.registerSounds(new net.minecraftforge.event.RegistryEvent.Register<SoundEvent>(new ResourceLocation("minecraft:soundevents"),ForgeRegistries.SOUND_EVENTS));Field uf=Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);Unsafe u=(Unsafe)uf.get(null);World w=(World)u.allocateInstance(net.minecraft.client.multiplayer.WorldClient.class);List<Object[]> calls=new ArrayList<>();IWorldEventListener observer=(IWorldEventListener)Proxy.newProxyInstance(DoorMethodProbe.class.getClassLoader(),new Class[]{IWorldEventListener.class},(p,m,a)->{if(m.getName().equals("playSoundToAllNearExcept"))calls.add(a);return null;});Field f=World.class.getDeclaredField("eventListeners");f.setAccessible(true);f.set(w,new ArrayList<>(Arrays.asList(observer)));DoorMethodProbe d=new DoorMethodProbe();d.setWorld(w);d.setPos(new BlockPos(-7,29,30000000));d.finishOpen();if(d.state!=2||calls.size()!=1)throw new AssertionError("finishOpen sequence");d.finishClose();if(d.state!=0||calls.size()!=2)throw new AssertionError("finishClose sequence");for(Object[] c:calls)if(c[1]!=ModSoundEvents.worldEffect(new ResourceLocation("hbm:block.reactorStop"))||!c[3].equals(-7D)||!c[4].equals(29D)||!c[5].equals(30000000D)||!c[6].equals(.5F)||!c[7].equals(1F))throw new AssertionError("exact sound callback");d.setWorld(null);try{d.finishOpen();throw new AssertionError("missing null failure");}catch(NullPointerException expected){if(d.state!=2)throw new AssertionError("assignment order");}System.out.println("PASS 2 exact complete production methods on native TileEntity, state/callback values and null failure assignment order.");}}
