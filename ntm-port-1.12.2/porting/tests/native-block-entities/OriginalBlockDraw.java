import net.minecraft.block.Block;
import com.hbm.render.icon.IIcon;
/** Explicit call-level oracle; vertex equivalence is tested separately. */
public class OriginalBlockDraw {
 public boolean useInventoryTint=true;
 public static OriginalBlockDraw getInstance(){return new OriginalBlockDraw();}
 public void renderBlockAsItem(Block b,int meta,float brightness){BlockEntityNativeTest.blockDraw(b,meta,brightness,useInventoryTint);}
 public IIcon getBlockIconFromSideAndMetadata(Block b,int side,int meta){return BlockEntityNativeTest.oldIcon;}
}
