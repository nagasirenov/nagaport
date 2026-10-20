import com.hbm.blocks.network.*;
import com.hbm.main.ConveyorBlockModelHandler;
import com.hbm.render.item.block.*;
import com.hbm.render.item.nativeitem.staticfamily.*;
import com.hbm.render.util.NativeMachineItemRenderer;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.opengl.GL11;

/** Real target registry, model events/loader, RenderItem, RenderEntityItem and GL draws.
 * The wand Item is a declared NTM gameplay boundary, not a replacement platform class. */
public class NativeConveyorWandTest {
 static int checks;static Item wand;static Block[] blocks;static Spy draw;static BlockItemRegistration registration;static EventBus bus;static ModelLoader loader;
 static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 static void equal(float[] a,float[] b,String message){for(int i=0;i<16;i++)if(Math.abs(a[i]-b[i])>0.00008F)throw new AssertionError(message+" at "+i+" "+Arrays.toString(a)+" != "+Arrays.toString(b));checks++;}
 static final class Spy extends ItemRenderBlock {
  final List<float[]> matrices=new ArrayList<>();final List<ItemStack> stacks=new ArrayList<>();final List<IBakedModel> selected=new ArrayList<>();
  Spy(Block... blocks){super(blocks);}
  @Override public void renderByItem(ItemStack stack){matrices.add(NativeMachineItemTest.matrix());stacks.add(stack);selected.add(inventoryModel(stack.getItemDamage()));super.renderByItem(stack);}
  void clear(){matrices.clear();stacks.clear();selected.clear();}
 }
 static void reset(){NativeMachineDrawTest.reset();draw.clear();}
 static ModelResourceLocation key(){return new ModelResourceLocation(wand.getRegistryName(),"inventory");}
 static IBakedModel bake(Block block)throws Exception {
  IModel model=ModelLoaderRegistry.getModel(ConveyorBlockModelHandler.inventoryModelLocation(block,0));
  return model.bake(model.getDefaultState(),DefaultVertexFormats.ITEM,location->{TextureAtlasSprite sprite=new TextureAtlasSprite(location.toString()){};sprite.setIconWidth(16);sprite.setIconHeight(16);sprite.initSprite(16,16,0,0,false);sprite.setFramesTextureData(Collections.singletonList(new int[][]{new int[256]}));return sprite;});
 }
 static void models()throws Exception {
  blocks=new Block[]{new BlockConveyor(),new BlockConveyorExpress(),new BlockConveyorDouble(),new BlockConveyorTriple()};
  String[] names={"conveyor","conveyor_express","conveyor_double","conveyor_triple"};
  for(int i=0;i<4;i++){blocks[i].setRegistryName("hbm","tile."+names[i]);ForgeRegistries.BLOCKS.register(blocks[i]);ItemBlock item=new ItemBlock(blocks[i]);item.setRegistryName(blocks[i].getRegistryName());ForgeRegistries.ITEMS.register(item);check(Item.getItemFromBlock(blocks[i])==item,"actual native ItemBlock registry identity "+i);}
  wand=new Item().setHasSubtypes(true).setRegistryName("hbm","item.conveyor_wand");ForgeRegistries.ITEMS.register(wand);draw=new Spy(blocks);registration=new BlockItemRegistration(wand,draw);
  net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);
  loader=new ModelLoader(Minecraft.getMinecraft().getResourceManager(),Minecraft.getMinecraft().getTextureMapBlocks(),new BlockModelShapes(NativeMachineDrawTest.renderer.getItemModelMesher().getModelManager()));
  bus=new EventBus();bus.register(ConveyorBlockModelHandler.class);bus.register(registration);bus.post(new ModelRegistryEvent());
  NativeMachineDrawTest.models.putObject(key(),NativeMachineDrawTest.icon);
  for(Block block:blocks)NativeMachineDrawTest.models.putObject(ConveyorBlockModelHandler.inventoryModelLocation(block,0),bake(block));
  bus.post(new ModelBakeEvent(NativeMachineDrawTest.renderer.getItemModelMesher().getModelManager(),NativeMachineDrawTest.models,null));
  check(NativeMachineDrawTest.models.getObject(key()) instanceof BlockItemModel,"actual ModelBakeEvent installs typed native model");check(wand.getTileEntityItemStackRenderer()==draw,"actual bake installs full native TEISR");
  Field definitions=ModelLoader.class.getDeclaredField("customMeshDefinitions");definitions.setAccessible(true);
  @SuppressWarnings("unchecked") Map<net.minecraftforge.registries.IRegistryDelegate<Item>,ItemMeshDefinition> meshes=(Map<net.minecraftforge.registries.IRegistryDelegate<Item>,ItemMeshDefinition>)definitions.get(null);
  ItemMeshDefinition mesh=meshes.get(wand.delegate);check(mesh!=null,"actual ModelRegistryEvent registers native custom mesh definition");
  ModelLoader.onRegisterItems(NativeMachineDrawTest.renderer.getItemModelMesher());NativeMachineDrawTest.renderer.getItemModelMesher().rebuildCache();
  for(int i=0;i<4;i++){check(mesh.getModelLocation(new ItemStack(wand,1,i)).equals(key()),"all damage variants reach one native dispatcher "+i);IBakedModel model=draw.inventoryModel(i);check(model.getQuads(null,null,0).size()==6,"real baked conveyor inventory six faces "+i);for(BakedQuad q:model.getQuads(null,null,0))check(q.getTintIndex()==-1,"original white conveyor ignores wand tint "+i);}
  check(((BlockItemModel)NativeMachineDrawTest.models.getObject(key())).getBaseModel()==NativeMachineDrawTest.icon,"original full base retained per bake");
 }
 static void guiAndHeld() {
  for(int damage=0;damage<4;damage++){
   ItemStack stack=new ItemStack(wand,37,damage);stack.addEnchantment(Enchantments.UNBREAKING,1);NBTTagCompound before=stack.writeToNBT(new NBTTagCompound());
   reset();NativeMachineDrawTest.renderer.zLevel=0;NativeMachineDrawTest.renderer.renderItemAndEffectIntoGUI(stack,20,20);
   check(draw.matrices.size()==1,"full native GUI invokes exactly one block helper "+damage);float[] actual=draw.matrices.get(0);
   GL11.glLoadIdentity();GL11.glTranslatef(18,23,47);GL11.glScalef(10,10,10);GL11.glTranslatef(1,.5F,1);GL11.glScalef(1,1,-1);GL11.glRotatef(210,1,0,0);GL11.glRotatef(45,0,1,0);GL11.glRotatef(-90,0,1,0);GL11.glTranslatef(-.5F,-.5F,-.5F);
   equal(NativeMachineItemTest.matrix(),actual,"GUI actual native vs original all-true helper "+damage);
   check(NativeMachineDrawTest.textures.glints==0,"custom GUI does not invent enchant glint");check(!Arrays.equals(NativeMachineDrawTest.pixels(),new byte[64*64*4]),"complete conveyor GUI emits pixels");
   for(TransformType type:new TransformType[]{TransformType.FIRST_PERSON_RIGHT_HAND,TransformType.THIRD_PERSON_RIGHT_HAND,TransformType.HEAD,TransformType.NONE}){
    reset();if(type==TransformType.FIRST_PERSON_RIGHT_HAND)GL11.glRotatef(-45,0,1,0);if(type==TransformType.THIRD_PERSON_RIGHT_HAND){GL11.glRotatef(-90,1,0,0);GL11.glRotatef(180,0,1,0);GL11.glTranslatef(.0625F,.125F,-.625F);}
    NativeMachineDrawTest.renderer.renderItem(stack,type);check(draw.matrices.size()==1,"native held/new context reaches actual selected mesh "+type);
    actual=draw.matrices.get(0);GL11.glLoadIdentity();
    if(type==TransformType.FIRST_PERSON_RIGHT_HAND)GL11.glScalef(.4F,.4F,.4F);
    if(type==TransformType.THIRD_PERSON_RIGHT_HAND){GL11.glTranslatef(-.0625F,.4375F,.0625F);GL11.glTranslatef(0,.1875F,-.3125F);GL11.glRotatef(20,1,0,0);GL11.glRotatef(45,0,1,0);GL11.glScalef(-.375F,-.375F,.375F);}
    GL11.glTranslatef(-.5F,-.5F,-.5F);equal(NativeMachineItemTest.matrix(),actual,"native context vs original helper cancellation or centered approved context "+type);
    check(draw.stacks.get(0)==stack,"no held or GUI copied/mutated stack");
   }
   check(before.equals(stack.writeToNBT(new NBTTagCompound())),"all native contexts preserve damage/count/NBT");
  }
 }
 static List<float[]> oldEntity(EntityItem entity,float partial,boolean frame,boolean decoration) {
  List<float[]> expected=new ArrayList<>();GL11.glLoadIdentity();float bob=decoration?0:net.minecraft.util.math.MathHelper.sin((entity.getAge()+partial)/10F+entity.hoverStart)*.1F+.1F;
  GL11.glTranslatef(3,4+bob,5);GL11.glRotatef(((entity.getAge()+partial)/20F+entity.hoverStart)*57.295776F,0,1,0);
  if(frame){GL11.glScalef(1.25F,1.25F,1.25F);GL11.glTranslatef(0,.05F,0);GL11.glRotatef(-90,0,1,0);}GL11.glScalef(.5F,.5F,.5F);
  int count=entity.getItem().getCount();int copies=decoration?1:count>40?5:count>20?4:count>5?3:count>1?2:1;Random random=new Random(187);
  for(int i=0;i<copies;i++){GL11.glPushMatrix();if(i>0)GL11.glTranslatef((random.nextFloat()*2-1)*.2F/.5F,(random.nextFloat()*2-1)*.2F/.5F,(random.nextFloat()*2-1)*.2F/.5F);GL11.glTranslatef(-.5F,-.5F,-.5F);expected.add(NativeMachineItemTest.matrix());GL11.glPopMatrix();}return expected;
 }
 static void entities() {
  SingleMeshEntityItemRenderer renderer=new SingleMeshEntityItemRenderer(Minecraft.getMinecraft().getRenderManager(),NativeMachineDrawTest.renderer,Collections.emptySet(),registration.droppedRenderers());
  for(int count:new int[]{1,2,5,6,16,17,20,21,32,33,40,41,48,49,64})for(int damage=0;damage<4;damage++) {
   ItemStack stack=new ItemStack(wand,count,damage);stack.addEnchantment(Enchantments.UNBREAKING,1);EntityItem entity=new EntityItem(null,0,0,0,stack);entity.hoverStart=.73F;NBTTagCompound nbt=stack.writeToNBT(new NBTTagCompound());
   for(int mode=0;mode<3;mode++){
    reset();if(mode==0)renderer.doRender(entity,3,4,5,19,.375F);else new NativeMachineItemRenderer().renderFrame(entity,3,4,5,19,.375F,mode==1?NativeMachineItemRenderer.Display.ENTITY_FRAME:NativeMachineItemRenderer.Display.DECORATION_FRAME);
    List<float[]> actual=new ArrayList<>(draw.matrices),expected=oldEntity(entity,.375F,mode!=0,mode==2);check(actual.size()==expected.size(),"actual native original count threshold "+count+" mode "+mode);
    for(int i=0;i<expected.size();i++){equal(expected.get(i),actual.get(i),"actual dropped/frame old bob/rotation/scale/seed187 spread "+count+" mode "+mode+" copy "+i);check(draw.stacks.get(i)==stack,"copy draws preserve original stack identity");}
    check(NativeMachineDrawTest.textures.glints==0,"block helper never adds generic glint");check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==1,"entity/frame matrix balanced");
   }
   check(nbt.equals(stack.writeToNBT(new NBTTagCompound())),"count damage and NBT unchanged through every dropped/frame copy");
  }
 }
 static void unaffected() {
  Item nativeItem=Items.DIAMOND,oneMesh=Items.EMERALD;NativeMachineDrawTest.model(nativeItem,NativeMachineDrawTest.icon);NativeMachineDrawTest.model(oneMesh,NativeMachineDrawTest.icon);
  RenderManager manager=Minecraft.getMinecraft().getRenderManager();RenderEntityItem vanilla=new RenderEntityItem(manager,NativeMachineDrawTest.renderer);
  SingleMeshEntityItemRenderer previous=new SingleMeshEntityItemRenderer(manager,NativeMachineDrawTest.renderer,Collections.singleton(oneMesh));
  SingleMeshEntityItemRenderer extended=new SingleMeshEntityItemRenderer(manager,NativeMachineDrawTest.renderer,Collections.singleton(oneMesh),registration.droppedRenderers());
  for(Item item:new Item[]{nativeItem,oneMesh})for(int count:new int[]{1,2,6,17,21,33,41,49,64}){
   EntityItem entity=new EntityItem(null,0,0,0,new ItemStack(item,count));entity.hoverStart=.37F;
   reset();NativeMachineDrawTest.renderer.matrices.clear();(item==nativeItem?vanilla:previous).doRender(entity,32,32,0,0,.3F);List<float[]> expected=new ArrayList<>(NativeMachineDrawTest.renderer.matrices);byte[] pixels=NativeMachineDrawTest.pixels();
   reset();NativeMachineDrawTest.renderer.matrices.clear();extended.doRender(entity,32,32,0,0,.3F);List<float[]> actual=NativeMachineDrawTest.renderer.matrices;check(expected.size()==actual.size(),"native unrelated/old one-mesh count unchanged");for(int i=0;i<actual.size();i++)equal(expected.get(i),actual.get(i),"native unrelated/old one-mesh draw matrix unchanged");check(Arrays.equals(pixels,NativeMachineDrawTest.pixels()),"actual unaffected path framebuffer equals original native path");check(draw.matrices.isEmpty(),"unrelated/native one-mesh never enters wand delegate");
  }
 }
 static void vanillaFrames()throws Exception {
  RenderItemFrame frameRenderer=new RenderItemFrame(Minecraft.getMinecraft().getRenderManager(),NativeMachineDrawTest.renderer);
  Method render=RenderItemFrame.class.getDeclaredMethod("renderItem",EntityItemFrame.class);render.setAccessible(true);
  Method set=EntityItemFrame.class.getDeclaredMethod("setDisplayedItemWithUpdate",ItemStack.class,boolean.class);set.setAccessible(true);
  Method rotate=EntityItemFrame.class.getDeclaredMethod("setRotation",int.class,boolean.class);rotate.setAccessible(true);
  net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(registration);
  float[][] offsets={{0,0},{-.16F,-.16F},{0,-.32F},{.16F,-.16F}};
  try { for(int k=0;k<4;k++)for(int damage=0;damage<4;damage++) {
   EntityItemFrame frame=new EntityItemFrame(null);frame.setSilent(true);set.invoke(frame,new ItemStack(wand,1,damage),false);rotate.invoke(frame,k*2,false);
   ItemStack source=frame.getDisplayedItem();NBTTagCompound before=source.writeToNBT(new NBTTagCompound());
   reset();render.invoke(frameRenderer,frame);check(draw.matrices.size()==1,"actual native RenderItemFrame dispatch draws one selected conveyor");float[] actual=draw.matrices.get(0);
   GL11.glLoadIdentity();GL11.glTranslatef(0,-.18F,.015625F);GL11.glRotatef(180,0,1,0);GL11.glRotatef(-90*k,0,0,1);GL11.glTranslatef(offsets[k][0],offsets[k][1],0);GL11.glTranslatef(0,.1F,0);GL11.glScalef(1.25F,1.25F,1.25F);GL11.glTranslatef(0,.05F,0);GL11.glRotatef(-90,0,1,0);GL11.glScalef(.5F,.5F,.5F);GL11.glTranslatef(-.5F,-.5F,-.5F);
   equal(NativeMachineItemTest.matrix(),actual,"full native item-frame event/FIXED path vs old four-angle block helpers "+k);
   check(before.equals(source.writeToNBT(new NBTTagCompound()))&&draw.stacks.get(0)==source,"frame does not mutate/copy source for rendering");check(NativeMachineDrawTest.textures.glints==0,"actual native frame custom branch has no generic glint");
  }} finally {net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(registration);}
 }
 static void ownershipEvent() {
  RenderManager manager=Minecraft.getMinecraft().getRenderManager();Render<?> old=manager.entityRenderMap.get(EntityItem.class);List<String> warnings=new ArrayList<>();
  Map<Item,SingleMeshEntityItemRenderer.BlockItemDraw> map=new IdentityHashMap<>(registration.droppedRenderers());
  StaticItemEntityOwnership owner=new StaticItemEntityOwnership(Collections.singleton(Items.EMERALD),map,warnings::add);map.clear();EventBus events=new EventBus();events.register(owner);
  try {
   events.post(new net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent(net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END,0));check(manager.entityRenderMap.get(EntityItem.class)==old,"optional block seam still waits first render START");
   events.post(new net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent(net.minecraftforge.fml.common.gameevent.TickEvent.Phase.START,0));Render<?> installed=manager.entityRenderMap.get(EntityItem.class);check(installed instanceof SingleMeshEntityItemRenderer,"actual transformed RenderTick event installs sole existing ownership seam");
   reset();((SingleMeshEntityItemRenderer)installed).doRender(new EntityItem(null,0,0,0,new ItemStack(wand,6)),3,4,5,0,0);check(draw.matrices.size()==3,"installed renderer retained identity map snapshot and actual old block count");check(warnings.isEmpty(),"exact native owner accepted without warning");
   events.post(new net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent(net.minecraftforge.fml.common.gameevent.TickEvent.Phase.START,0));check(manager.entityRenderMap.get(EntityItem.class)==installed,"repeat render event never replaces owned renderer");
  } finally {events.unregister(owner);manager.entityRenderMap.put(EntityItem.class,(Render)old);}
 }
 static void bakeFailure() {
  ModelResourceLocation key=key();IBakedModel live=NativeMachineDrawTest.models.getObject(key);Object teisr=wand.getTileEntityItemStackRenderer();
  net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> missing=new net.minecraft.util.registry.RegistrySimple<>();missing.putObject(key,NativeMachineDrawTest.icon);
  try{registration.bake(new ModelBakeEvent(NativeMachineDrawTest.renderer.getItemModelMesher().getModelManager(),missing,null));throw new AssertionError("missing block event model accepted");}catch(IllegalStateException expected){checks++;}
  check(missing.getObject(key)==NativeMachineDrawTest.icon&&NativeMachineDrawTest.models.getObject(key)==live&&wand.getTileEntityItemStackRenderer()==teisr,"failed reload validates event registry, not stale live models; no partial replacement");
 }
 static void reload()throws Exception {
  IBakedModel previous=draw.inventoryModel(0);IBakedModel original=NativeMachineDrawTest.icon;
  ModelLoaderRegistry.clearModelCache(Minecraft.getMinecraft().getResourceManager());
  NativeMachineDrawTest.models.putObject(key(),original);for(Block block:blocks)NativeMachineDrawTest.models.putObject(ConveyorBlockModelHandler.inventoryModelLocation(block,0),bake(block));
  bus.post(new ModelBakeEvent(NativeMachineDrawTest.renderer.getItemModelMesher().getModelManager(),NativeMachineDrawTest.models,null));NativeMachineDrawTest.renderer.getItemModelMesher().rebuildCache();check(draw.inventoryModel(0)!=previous,"reload resolves new native geometry rather than cached model");check(((BlockItemModel)NativeMachineDrawTest.models.getObject(key())).getBaseModel()==original,"reload captures new base without wrapper nesting");
  NativeMachineDrawTest.Renderer replacement=new NativeMachineDrawTest.Renderer(NativeMachineDrawTest.textures,NativeMachineDrawTest.renderer.getItemModelMesher().getModelManager(),Minecraft.getMinecraft().getItemColors());replacement.getItemModelMesher().register(wand,stack->key());replacement.getItemModelMesher().rebuildCache();NativeMachineDrawTest.field(Minecraft.getMinecraft(),"renderItem",replacement);
  try{reset();new NativeMachineItemRenderer().renderFrame(new EntityItem(null,0,0,0,new ItemStack(wand)),3,4,5,0,.2F,NativeMachineItemRenderer.Display.DECORATION_FRAME);check(draw.matrices.size()==1,"existing native root uses replaced renderer/model manager at runtime");}finally{NativeMachineDrawTest.field(Minecraft.getMinecraft(),"renderItem",NativeMachineDrawTest.renderer);}
 }
 static void baseAssets(String folder,String archive)throws Exception {
  net.minecraft.client.resources.SimpleReloadableResourceManager resources=(net.minecraft.client.resources.SimpleReloadableResourceManager)Minecraft.getMinecraft().getResourceManager();
  net.minecraft.client.resources.IResourcePack[] packs={new net.minecraft.client.resources.FolderResourcePack(new java.io.File(folder)),new net.minecraft.client.resources.FileResourcePack(new java.io.File(archive))};
  byte[] directoryPixels=null;
  for(net.minecraft.client.resources.IResourcePack pack:packs) {
   ResourceLocation json=new ResourceLocation("hbm","models/item/item.conveyor_wand.json"),png=new ResourceLocation("hbm","textures/items/wand_s.png");
   check(pack.resourceExists(json)&&pack.resourceExists(png),"exact original registry-prefix JSON and PNG resolve in "+pack.getPackName());resources.reloadResourcePack(pack);ModelLoaderRegistry.clearModelCache(resources);
   TextureAtlasSprite sprite=new TextureAtlasSprite("hbm:items/wand_s"){};
   try(net.minecraft.client.resources.IResource resource=resources.getResource(png)){sprite.loadSprite(PngSizeInfo.makeFromResource(resource),false);}
   try(net.minecraft.client.resources.IResource resource=resources.getResource(png)){sprite.loadSpriteFrames(resource,1);}
   sprite.initSprite(sprite.getIconWidth(),sprite.getIconHeight(),0,0,false);check(sprite.getFrameCount()==1&&sprite.getIconWidth()==16&&sprite.getIconHeight()==16,"actual native decoder reads original 16x16 wand_s pixels");
   Method locate=ModelBakery.class.getDeclaredMethod("getItemLocation",String.class);locate.setAccessible(true);ResourceLocation source=(ResourceLocation)locate.invoke(loader,wand.getRegistryName().toString());check(source.equals(new ResourceLocation("hbm:item/item.conveyor_wand")),"genuine native ModelLoader keeps item. registry prefix for JSON route");
   IModel unbaked=ModelLoaderRegistry.getModel(source);check(unbaked!=ModelLoaderRegistry.getMissingModel(),"actual native inventory route resolves generated wand base");
   IBakedModel base=unbaked.bake(unbaked.getDefaultState(),DefaultVertexFormats.ITEM,location->{check(location.equals(new ResourceLocation("hbm:items/wand_s")),"every native generated base texture uses original wand_s");return sprite;});
   check(!base.isBuiltInRenderer()&&!base.isGui3d()&&base.getQuads(null,null,0).size()>=6,"real generated base contains full extruded PNG geometry");check(base.getParticleTexture()==sprite,"generated base particle is original wand_s");
   DynamicTexture texture=new DynamicTexture(sprite.getIconWidth(),sprite.getIconHeight());System.arraycopy(sprite.getFrameTextureData(0)[0],0,texture.getTextureData(),0,texture.getTextureData().length);texture.updateDynamicTexture();NativeMachineDrawTest.textures.loadTexture(TextureMap.LOCATION_BLOCKS_TEXTURE,texture);
   ItemStack stack=new ItemStack(wand,37,3);stack.addEnchantment(Enchantments.UNBREAKING,1);NBTTagCompound before=stack.writeToNBT(new NBTTagCompound());
   NativeMachineDrawTest.models.putObject(key(),base);NativeMachineDrawTest.renderer.getItemModelMesher().rebuildCache();reset();com.hbm.render.util.NativeGuiItemDraw.withoutEffect(NativeMachineDrawTest.renderer,stack,20,20);byte[] expected=NativeMachineDrawTest.pixels();check(!Arrays.equals(expected,new byte[expected.length]),"actual original PNG base draw emits pixels");
   bus.post(new ModelBakeEvent(NativeMachineDrawTest.renderer.getItemModelMesher().getModelManager(),NativeMachineDrawTest.models,null));NativeMachineDrawTest.renderer.getItemModelMesher().rebuildCache();check(((BlockItemModel)NativeMachineDrawTest.models.getObject(key())).getBaseModel()==base,"bake captures actual generated base rather than a placeholder");
   reset();com.hbm.render.util.NativeGuiItemDraw.withoutEffect(NativeMachineDrawTest.renderer,stack,20,20);byte[] actual=NativeMachineDrawTest.pixels();check(draw.matrices.isEmpty()&&NativeMachineDrawTest.textures.glints==0,"explicit no-effect bypass uses retained real base and no custom/glint pass");check(Arrays.equals(expected,actual),"retained-base fallback framebuffer equals actual generated original base");check(before.equals(stack.writeToNBT(new NBTTagCompound())),"base fallback preserves stack damage/count/NBT");
   if(directoryPixels==null)directoryPixels=actual;else check(Arrays.equals(directoryPixels,actual),"directory and actual JAR PNG/base fallback pixels agree");
  }
  ((net.minecraft.client.resources.FileResourcePack)packs[1]).close();
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());ModMetadata metadata=new ModMetadata();metadata.modId="wandprobe";metadata.name="Wand proof";metadata.version="1";Loader.instance().setupTestHarness(new DummyModContainer(metadata));
  Constructor<?> context=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();context.setAccessible(true);
  try(AutoCloseable gl=(AutoCloseable)context.newInstance()){
   Method init=MaterialTextureAtlasSpriteNativeTest.class.getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);NativeMachineDrawTest.setup();models();guiAndHeld();entities();unaffected();vanillaFrames();ownershipEvent();bakeFailure();reload();baseAssets(args[0],args[1]);
   GlStateManager.disableRescaleNormal();GlStateManager.enableRescaleNormal();check(GL11.glIsEnabled(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL),"following native rescale-enable cache remains coherent");GlStateManager.disableRescaleNormal();check(!GL11.glIsEnabled(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL),"following native rescale-disable remains coherent");check(GL11.glGetError()==0,"complete native wand draws have no GL errors");NativeMachineDrawTest.framebuffer.deleteFramebuffer();
  }
  System.out.println("PASS "+checks+" genuine native conveyor-wand model events, inventory mesh, GUI/held/head/none, dropped/frame copies and matrices, unaffected renderer, reload and stack checks; rendering only, no wand gameplay claim.");
 }
}
