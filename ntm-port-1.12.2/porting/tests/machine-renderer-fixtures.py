"""Explicit NTM collaborator boundaries; Minecraft/Forge/LWJGL are never replaced."""
import pathlib,re

def add(root,tmp,files):
 def fixture(name,body):
  p=tmp/'fixtures'/(name+'.java');p.parent.mkdir(parents=True,exist_ok=True);p.write_text(body);files.append(p)
 consumers='RenderPress RenderEPress RenderSolderingStation RenderOreSlopper RenderMassStorage RenderDecoBlockAlt RenderArcWelder RenderPistonInserter RenderVacuumCircuit RenderAssemblyFactory RenderAssemblyMachine RenderPrecAss RenderSkeletonHolder RenderPedestalTile'.split()
 sources=[root/'src/main/java/com/hbm/render/tileentity'/f'{n}.java' for n in consumers];text=''.join(p.read_text() for p in sources);files.extend(sources)
 for name in ['render/item/nativeitem/context/NativeSelectiveItemRenderer.java','render/tileentity/IItemRendererProvider.java','render/item/ItemRenderBase.java','render/model/ModelGun.java','render/model/ModelStatue.java','lib/RefStrings.java','render/mesh/MeshModel.java','render/mesh/ObjMesh.java', 'render/item/nativeitem/ItemTransformMatrix.java', 'render/item/nativeitem/StaticMeshItemModel.java', 'render/item/nativeitem/AngelCoreItemModel.java', 'render/item/nativeitem/direct/DirectMeshItemModel.java', 'render/item/nativeitem/direct/DirectMeshItemRegistration.java', 'render/item/nativeitem/direct/DirectMeshItemRenderer.java', 'render/item/nativeitem/StaticMeshItemRegistration.java', 'render/item/nativeitem/AngelCoreFrameCorrection.java', 'render/item/nativeitem/staticfamily/OrdinaryMeshItemModel.java', 'render/item/nativeitem/staticfamily/StaticItemPerspectives.java', 'render/item/nativeitem/callback/CallbackItemModel.java', 'render/item/nativeitem/callback/CallbackItemRegistration.java', 'render/item/nativeitem/direct/PlainItemPerspectives.java', 'render/item/nativeitem/context/NativeItemContext.java', 'render/item/nativeitem/context/NativeItemFields.java', 'render/item/nativeitem/context/NativeItemFailureScope.java', 'render/item/nativeitem/context/ContextItemRenderer.java', 'render/item/nativeitem/context/ContextItemModel.java', 'render/item/nativeitem/context/ContextRenderItem.java']:
  files.append(root/'src/main/java/com/hbm'/name)
 fixture('com/hbm/blocks/BlockDummyable','package com.hbm.blocks; public class BlockDummyable { public static final int offset=10; }')
 fixture('com/hbm/blocks/ModBlocks','package com.hbm.blocks; public class ModBlocks {'+''.join('public static net.minecraft.block.Block '+n+';' for n in sorted(set(re.findall(r'ModBlocks\.(\w+)',text))))+'}')
 fixture('com/hbm/items/ModItems','package com.hbm.items; public class ModItems {'+''.join('public static net.minecraft.item.Item '+n+'=net.minecraft.init.Items.DIAMOND;' for n in sorted(set(re.findall(r'ModItems\.(\w+)',text))))+'}')
 refs=sorted(set(re.findall(r'ResourceManager\.(\w+)',text)))
 fixture('com/hbm/main/ResourceManager','''package com.hbm.main;
 public class ResourceManager {
 public static class Draw implements com.hbm.render.mesh.MeshModel {
  public final java.util.List<String> calls=new java.util.ArrayList<>();public final java.util.List<float[]> matrices=new java.util.ArrayList<>();
  private void capture(String name){calls.add(name);java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(16);org.lwjgl.opengl.GL11.glGetFloat(org.lwjgl.opengl.GL11.GL_MODELVIEW_MATRIX,b);float[] a=new float[16];b.get(a);matrices.add(a);}
  public com.hbm.render.mesh.ObjMesh getMesh(){return com.hbm.render.mesh.ObjMesh.empty();}public java.util.List<String> getPartNames(){return java.util.Collections.emptyList();}
  public void renderAll(){capture("all");}public void renderPart(String n){capture(n);}public void renderOnly(String...n){for(String s:n)capture(s);}public void renderAllExcept(String...n){capture("except");}
 }
 '''+''.join('public static final '+('net.minecraft.util.ResourceLocation '+n+'=new net.minecraft.util.ResourceLocation("test","'+n+'");' if n.endswith('_tex') else 'Draw '+n+'=new Draw();') for n in refs)+'}')
 base='extends net.minecraft.tileentity.TileEntity { public int meta; @Override public int getBlockMetadata(){return meta;} '
 def tile(name,fields):fixture('com/hbm/tileentity/machine/'+name,'package com.hbm.tileentity.machine; public class '+name+' '+base+fields+'}')
 for name in ['TileEntityMachinePress','TileEntityMachineEPress']:tile(name,'public int lastPress,renderPress,maxPress=100; public net.minecraft.item.ItemStack syncStack;')
 for name in ['TileEntityMachineSolderingStation','TileEntityMachineArcWelder','TileEntityMachineVacuumCircuit']:tile(name,'public net.minecraft.item.ItemStack display;')
 tile('TileEntityMachineOreSlopper','public enum Animation {LIFTING,IDLE};public Animation animation=Animation.IDLE; public double prevSlider,slider,prevBucket,bucket,prevBlades,blades,prevFan,fan;')
 fixture('com/hbm/tileentity/machine/storage/TileEntityMassStorage','package com.hbm.tileentity.machine.storage; public class TileEntityMassStorage '+base+'public net.minecraft.item.ItemStack type;public int stock,capacity=100;public int getStockpile(){return stock;}public int getCapacity(){return capacity;}}')
 for name,inner,fields in [('blocks/machine/PistonInserter','TileEntityPistonInserter','public int lastExtend,renderExtend,maxExtend=100;public net.minecraft.item.ItemStack slot;'),('blocks/generic/BlockPedestal','TileEntityPedestal','public net.minecraft.item.ItemStack item;'),('blocks/generic/BlockSkeletonHolder','TileEntitySkeletonHolder','public net.minecraft.item.ItemStack item;')]:
  package,outer=name.rsplit('/',1);fixture('com/hbm/'+name,'package com.hbm.'+package.replace('/','.')+';public class '+outer+' { public static class '+inner+' '+base+fields+'}}')
 for suffix in 'FGW':fixture('com/hbm/tileentity/deco/TileEntityDecoBlockAlt'+suffix,'package com.hbm.tileentity.deco;public class TileEntityDecoBlockAlt'+suffix+' '+base+'}')
 fixture('com/hbm/inventory/recipes/loader/GenericRecipe','package com.hbm.inventory.recipes.loader;public class GenericRecipe { public net.minecraft.item.ItemStack icon;public net.minecraft.item.ItemStack getIcon(){return icon.copy();} }')
 for name in ['AssemblyMachineRecipes','PrecAssRecipes']:fixture('com/hbm/inventory/recipes/'+name,'package com.hbm.inventory.recipes; public class '+name+' {public static final '+name+' INSTANCE=new '+name+'();public final java.util.Map<String,com.hbm.inventory.recipes.loader.GenericRecipe> recipeNameMap=new java.util.HashMap<>();}')
 module='public static class Module {public String recipe;} public static class Arm {public double[] positions=new double[5];public double[] getPositions(float f){return positions.clone();}} '
 tile('TileEntityMachineAssemblyMachine',module+'public boolean frame;public double prevRing,ring;public Arm[] arms={new Arm(),new Arm()};public Module assemblerModule=new Module();')
 tile('TileEntityMachinePrecAss',module+'public boolean frame;public double prevRing,ring;public double[] prevArmAngles=new double[3],armAngles=new double[3],prevStrikers=new double[4],strikers=new double[4];public Module assemblerModule=new Module();')
 tile('TileEntityMachineAssemblyFactory',module+'public static class Animation {public Arm striker=new Arm(),saw=new Arm();public double slider;public double getSlider(float f){return slider;}} public boolean frame;public Animation[] animations={new Animation(),new Animation()};public Module[] assemblerModule={new Module(),new Module(),new Module(),new Module()};')
 fixture('com/hbm/util/BobMathUtil','package com.hbm.util;public class BobMathUtil { public static double interp(double old,double current,double partial){return old+(current-old)*partial;} }')
 fixture('com/hbm/main/MainRegistry','package com.hbm.main;public class MainRegistry {public static Proxy proxy=new Proxy();public static class Proxy {public net.minecraft.entity.player.EntityPlayer player; public net.minecraft.entity.player.EntityPlayer me(){return player;}} }')
 fixture('com/hbm/render/tileentity/RenderArcFurnace','package com.hbm.render.tileentity;public class RenderArcFurnace {public static void fullbright(boolean enable){net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit,enable?240:0,enable?240:0);} }')
 return consumers
