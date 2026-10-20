import com.hbm.render.util.NativeItemBaseModel;
import com.hbm.render.item.nativeitem.*;
import com.hbm.render.item.nativeitem.callback.*;
import com.hbm.render.item.nativeitem.direct.*;
import com.hbm.render.item.nativeitem.staticfamily.*;
import java.util.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.*;
import net.minecraft.init.Items;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.client.event.ModelBakeEvent;

/** Actual owner bake + immutable original geometry provenance, including fresh reload models. */
public final class NativeBaseModelOwnershipTest {
 static int checks;static void check(boolean ok,String msg){checks++;if(!ok)throw new AssertionError(msg);}
 public static void run(IBakedModel base) {
  Item item=Items.DIAMOND;ModelResourceLocation key=StaticMeshItemRegistration.modelLocation(item);
  com.hbm.render.item.ItemRenderBase callback=new com.hbm.render.item.ItemRenderBase(){};
  CallbackItemRegistration callbacks=new CallbackItemRegistration(Collections.singletonMap(item,callback));
  DirectMeshItemRenderer direct=new DirectMeshItemRenderer(){public javax.vecmath.Matrix4f localTransform(TransformType t,long clock){return new ItemTransformMatrix().build();}protected void draw(ItemStack stack){}};
  DirectMeshItemRegistration directs=new DirectMeshItemRegistration(Collections.singletonMap(item,direct));
  EnumMap<TransformType,javax.vecmath.Matrix4f> transforms=new EnumMap<>(TransformType.class);for(TransformType type:TransformType.values())transforms.put(type,new ItemTransformMatrix().build());
  StaticMeshItemRegistration statics=new StaticMeshItemRegistration(item,callback,()->new AngelCoreItemModel(transforms,base.getParticleTexture()),0);
  IBakedModel previous=null;
  for(int reload=0;reload<2;reload++)for(int owner=0;owner<3;owner++){
   // Fresh native bake registry, not our replacement from an earlier resource reload.
   RegistrySimple<ModelResourceLocation,IBakedModel> registry=new RegistrySimple<>();
   IBakedModel original=new SimpleBakedModel(Collections.emptyList(),new EnumMap<>(net.minecraft.util.EnumFacing.class),false,false,base.getParticleTexture(),ItemCameraTransforms.DEFAULT,ItemOverrideList.NONE);
   registry.putObject(key,original);ModelBakeEvent event=new ModelBakeEvent(null,registry,null);
   if(owner==0)callbacks.bake(event);else if(owner==1)directs.bake(event);else statics.bake(event);
   IBakedModel selected=registry.getObject(key);
   check(selected!=previous&&selected instanceof NativeItemBaseModel,"native replacement is fresh and carries ownership");
   check(((NativeItemBaseModel)selected).getBaseModel()==original,"retained original complete model identity owner="+owner+" reload="+reload);
   ItemStack stack=new ItemStack(item,2);IBakedModel overridden=selected.getOverrides().handleItemState(selected,stack,null,null);
   check(((NativeItemBaseModel)overridden).getBaseModel()==original,"override copy preserves original model owner="+owner);
   IBakedModel posed=overridden.handlePerspective(TransformType.GROUND).getLeft();
   check(((NativeItemBaseModel)posed).getBaseModel()==original,"perspective retains original geometry owner="+owner);
   previous=selected;
  }
  OrdinaryMeshItemModel ordinary=new OrdinaryMeshItemModel(transforms,base.getParticleTexture()).withBaseModel(base);
  check(ordinary.getBaseModel()==base&&ordinary.hasEveryNativePerspective(),"ordinary static-family covariant copy retains exact base and perspectives");
  AngelCoreItemModel angel=new AngelCoreItemModel(transforms,base.getParticleTexture()).withBaseModel(base);
  check(angel.getBaseModel()==base&&angel.hasEveryNativePerspective(),"angel core covariant copy retains exact base and perspectives");
  System.out.println("PASS "+checks+" actual native bake/reload/base-geometry ownership checks. Direct event methods, not another event-bus proof.");
 }
}
