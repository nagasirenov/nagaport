import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
/** Explicit oracle engine/GPU boundary; not a production renderer or native substitute. */
public abstract class OriginalRenderBoundary {
 public float shadowSize;
 protected void bindTexture(ResourceLocation loc){BlockEntityNativeTest.bind(loc);}
 protected boolean bindEntityTexture(Entity e){bindTexture(getEntityTexture(e));return true;}
 protected abstract ResourceLocation getEntityTexture(Entity e);
 public abstract void doRender(Entity e,double x,double y,double z,float a,float b);
}
