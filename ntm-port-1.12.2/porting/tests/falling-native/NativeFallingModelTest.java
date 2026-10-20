import java.util.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.*;
import com.hbm.blocks.generic.BlockFallingTint;
public class NativeFallingModelTest {
 static int checks; static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static class Sprite extends TextureAtlasSprite {Sprite(String n){super(n);setIconWidth(16);setIconHeight(16);initSprite(256,256,32,48,false);}}
 static void routes()throws Exception {
  Class<?> cl=Class.forName("com.hbm.main.FallingTintModelHandler$Loader");java.lang.reflect.Constructor<?> c=cl.getDeclaredConstructor();c.setAccessible(true);net.minecraftforge.client.model.ICustomModelLoader loader=(net.minecraftforge.client.model.ICustomModelLoader)c.newInstance();
  for(String path:new String[]{"world","item"}){ResourceLocation route=new ResourceLocation("hbm:native_falling_tint/"+path);check(loader.accepts(route),"route");net.minecraftforge.client.model.IModel model=loader.loadModel(route);check(model.getTextures().equals(Collections.singletonList(new ResourceLocation("hbm:blocks/laythe_silt"))),"original single atlas material");IBakedModel baked=model.bake(net.minecraftforge.common.model.TRSRTransformation.identity(),net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK,r->new Sprite(r.toString()));check(baked.getQuads(null,null,0).isEmpty(),"native culled cube");for(EnumFacing face:EnumFacing.values()){List<BakedQuad> qs=baked.getQuads(null,face,0);check(qs.size()==1&&qs.get(0).getFace()==face&&qs.get(0).getTintIndex()==0,"six native tinted cube faces");}check(baked.isAmbientOcclusion()&&!baked.isBuiltInRenderer(),"standard AO lighting");loader.onResourceManagerReload(null);}
  check(!loader.accepts(new ResourceLocation("other:native_falling_tint/world")),"namespace isolation");
 }
 static void registry() throws Exception {
  net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
  net.minecraftforge.fml.common.ModMetadata metadata=new net.minecraftforge.fml.common.ModMetadata();metadata.modId="hbm";metadata.name="Native pedestal probe";metadata.version="1";
  net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(metadata);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);

  java.lang.reflect.Field mapping=net.minecraft.item.Item.class.getDeclaredField("BLOCK_TO_ITEM");mapping.setAccessible(true);
  Map<net.minecraft.block.Block,net.minecraft.item.Item> items=(Map<net.minecraft.block.Block,net.minecraft.item.Item>)mapping.get(null);
  BlockFallingTint block=new BlockFallingTint(net.minecraft.block.material.Material.SAND);block.setRegistryName("hbm","tile.laythe_silt");net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(block);
  net.minecraft.item.ItemBlock item=new net.minecraft.item.ItemBlock(block);item.setRegistryName(block.getRegistryName());net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(item);items.put(block,item);
  BlockFallingTint foreign=new BlockFallingTint(net.minecraft.block.material.Material.SAND);foreign.setRegistryName("other","tile.laythe_silt");net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(foreign);
  // Model registration uses the real client singleton/resource manager, without a game loop or GL context.
  java.io.File d=new java.io.File(".");
  net.minecraft.client.main.GameConfiguration config=new net.minecraft.client.main.GameConfiguration(
    new net.minecraft.client.main.GameConfiguration.UserInformation(new net.minecraft.util.Session("PedestalProbe","00000000-0000-0000-0000-000000000000","","legacy"),new com.mojang.authlib.properties.PropertyMap(),new com.mojang.authlib.properties.PropertyMap(),java.net.Proxy.NO_PROXY),
    new net.minecraft.client.main.GameConfiguration.DisplayInformation(1,1,false,false),new net.minecraft.client.main.GameConfiguration.FolderInformation(d,d,d,null),
    new net.minecraft.client.main.GameConfiguration.GameInformation(false,"1.12.2","native pedestal proof"),new net.minecraft.client.main.GameConfiguration.ServerInformation(null,0));
  net.minecraft.client.Minecraft minecraft=new net.minecraft.client.Minecraft(config);minecraft.gameSettings=new net.minecraft.client.settings.GameSettings();
  java.lang.reflect.Field rm=net.minecraft.client.Minecraft.class.getDeclaredField("resourceManager");rm.setAccessible(true);rm.set(minecraft,new net.minecraft.client.resources.SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer()));

  java.lang.reflect.Field bc=net.minecraft.client.Minecraft.class.getDeclaredField("blockColors");bc.setAccessible(true);bc.set(minecraft,net.minecraft.client.renderer.color.BlockColors.init());
  java.lang.reflect.Field ic=net.minecraft.client.Minecraft.class.getDeclaredField("itemColors");ic.setAccessible(true);ic.set(minecraft,net.minecraft.client.renderer.color.ItemColors.init(minecraft.getBlockColors()));
  net.minecraftforge.fml.common.eventhandler.EventBus bus=new net.minecraftforge.fml.common.eventhandler.EventBus();bus.register(com.hbm.main.FallingTintModelHandler.class);bus.post(new net.minecraftforge.client.event.ModelRegistryEvent());
  java.lang.reflect.Field custom=net.minecraftforge.client.model.ModelLoader.class.getDeclaredField("customModels");custom.setAccessible(true);Map<?,?> models=(Map<?,?>)custom.get(null);check(models.size()==16,"actual registry item route");
  java.lang.reflect.Field sm=net.minecraftforge.client.model.ModelLoader.class.getDeclaredField("customStateMappers");sm.setAccessible(true);Map<?,?> states=(Map<?,?>)sm.get(null);check(states.containsKey(block.delegate)&&!states.containsKey(foreign.delegate),"actual state mapper exact owner isolation");
  net.minecraft.client.renderer.block.statemap.IStateMapper mapper=(net.minecraft.client.renderer.block.statemap.IStateMapper)states.get(block.delegate);check(mapper.putStateModelLocations(block).get(block.getDefaultState()).equals(new ModelResourceLocation("hbm:native_falling_tint/world","normal")),"actual blockstate route");
  for(int meta=0;meta<16;meta++)check(minecraft.getBlockColors().colorMultiplier(block.getStateFromMeta(meta),null,null,0)==BlockFallingTint.color(meta),"actual registered metadata tint");
  net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.hbm.entity.item.EntityFallingBlockNT.class,com.hbm.render.entity.item.RenderFallingBlockNT::new);
  Map<Class<? extends net.minecraft.entity.Entity>,net.minecraft.client.renderer.entity.Render<? extends net.minecraft.entity.Entity>> renderers=new HashMap<>();net.minecraftforge.fml.client.registry.RenderingRegistry.loadEntityRenderers(null,renderers);
  check(renderers.get(com.hbm.entity.item.EntityFallingBlockNT.class) instanceof com.hbm.render.entity.item.RenderFallingBlockNT,"native renderer factory route");
 }
 public static void main(String[] args)throws Exception {net.minecraft.init.Bootstrap.register();routes();registry();System.out.println("PASS native falling model "+checks+" assertions; native model registry/state/item/color callbacks, cube routes, renderer factory");}
}
