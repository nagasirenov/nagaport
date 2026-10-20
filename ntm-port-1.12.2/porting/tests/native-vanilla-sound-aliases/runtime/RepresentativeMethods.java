import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
/** Exact complete onSuck method. Only unrelated mod superclass/block type are boundaries. */
public final class RepresentativeMethods {
 public static int superCalls;
 public static class BlockOreFluid { }
 public static class Base extends TileEntity {
  public void onSuck(BlockOreFluid block,int x,int y,int z){superCalls++;}
 }
 public static class OilWell extends Base {
@Override
	public void onSuck(BlockOreFluid block, int x, int y, int z) {
		com.hbm.sound.VanillaWorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "game.neutral.swim.splash", 2.0F, 0.5F);

		super.onSuck(block, x, y, z);
	}
 }
 public static void invoke(World world){OilWell well=new OilWell();well.setWorld(world);well.setPos(new BlockPos(-2,17,30));well.onSuck(null,0,0,0);}
}
