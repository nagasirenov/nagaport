#!/usr/bin/env python3
"""Real native selected-class compilation and EGL draw using original mesh, material and native font."""
import argparse,hashlib,os,pathlib,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2]
names=['render/item/ItemRenderTransformer.java','render/item/ItemRenderTemplate.java','render/util/NativeMachineItemRenderer.java','render/util/NativeGuiItemDraw.java','render/util/NativeItemQuadRenderer.java','render/util/NativeIconExtrusion.java','render/util/RenderItemStack.java','render/item/nativeitem/staticfamily/SingleMeshEntityItemRenderer.java','render/item/block/NativeDecoItemRenderer.java','render/item/block/ItemRenderDecoBlock.java','render/item/block/ItemRenderPoleTop.java','render/item/block/ItemRenderSatelliteReceiver.java','render/model/ModelSteelRoof.java','render/model/ModelPoleTop.java','render/model/ModelSatelliteReceiver.java','render/item/nativeitem/context/NativeSelectiveItemRenderer.java','render/item/nativeitem/StaticMeshItemModel.java','lib/RefStrings.java','render/item/ItemRenderDetonatorLaser.java','render/item/nativeitem/ItemTransformMatrix.java','render/item/nativeitem/MaterialTextureAtlasSprite.java','render/item/nativeitem/AngelCoreFrameCorrection.java','render/item/nativeitem/direct/PlainItemPerspectives.java','render/item/nativeitem/staticfamily/OrdinaryMeshItemModel.java','render/util/NativeItemBaseModel.java','render/item/nativeitem/context/ContextItemRenderer.java','render/item/nativeitem/context/ContextItemModel.java','render/item/nativeitem/context/ContextRenderItem.java','render/item/nativeitem/context/NativeItemContext.java','render/item/nativeitem/context/NativeItemFailureScope.java','render/item/nativeitem/context/NativeItemFields.java','render/loader/HFRWavefrontObject.java','render/loader/HFRWavefrontObjectVBO.java','render/mesh/MeshModel.java','render/mesh/ObjMesh.java','render/mesh/ObjMeshParser.java','render/mesh/ObjMeshRenderer.java','render/mesh/ObjMeshVboPayload.java']
files=[root/'src/main/java/com/hbm'/n for n in names]
with tempfile.TemporaryDirectory(prefix='native-detonator-item-') as tmp:
 tmp=pathlib.Path(tmp)
 fixture=tmp/'com/hbm/main/ResourceManager.java';fixture.parent.mkdir(parents=True);fixture.write_text('''package com.hbm.main; public class ResourceManager {
 public static com.hbm.render.mesh.MeshModel detonator_laser;
 public static final net.minecraft.util.ResourceLocation detonator_laser_tex = new net.minecraft.util.ResourceLocation("hbm", "textures/models/weapons/detonator_laser.png");
 }''')
 itemFixture=tmp/'com/hbm/items/ModItems.java';itemFixture.parent.mkdir(parents=True);itemFixture.write_text('package com.hbm.items; public class ModItems { public static net.minecraft.item.Item crucible_template=new net.minecraft.item.Item(); }')
 recipeFixture=tmp/'com/hbm/inventory/recipes/CrucibleRecipes.java';recipeFixture.parent.mkdir(parents=True);recipeFixture.write_text('package com.hbm.inventory.recipes; public class CrucibleRecipes { public static java.util.HashMap<Integer,CrucibleRecipe> indexMapping=new java.util.HashMap<>(); public static class CrucibleRecipe { public net.minecraft.item.ItemStack icon; public CrucibleRecipe(net.minecraft.item.ItemStack i){icon=i;} } }')
 files += [itemFixture, recipeFixture]
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(tmp/name).write_bytes(z.read(name))
 assert(tmp/'liblwjgl64.so').exists()
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(tmp)]+list(map(str,files+[fixture,root/'porting/tests/SelectiveItemNativeTest.java',root/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java'])),check=True)
 archive=tmp/'original-materials.zip'
 with zipfile.ZipFile(archive,'w') as z:
  for name in ['textures/misc/glint.png','models/weapons/detonator_laser.obj','textures/models/weapons/detonator_laser.png','textures/models/steelroof.png','textures/models/poletop.png','textures/models/polesatellitereceiver.png']:
   f=root/'src/main/resources/assets/hbm'/name;z.write(f,'assets/hbm/'+name)
 for resource in [root/'src/main/resources',archive]:
  print('Native resource pack:',resource,flush=True)
  subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,'SelectiveItemNativeTest',str(resource)],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
 for f in files:
  if not str(f).startswith(str(root)):continue
  print(hashlib.sha256(f.read_bytes()).hexdigest(),f.relative_to(root))
 for n in ['models/weapons/detonator_laser.obj','textures/models/weapons/detonator_laser.png']:
  f=root/'src/main/resources/assets/hbm'/n;print(hashlib.sha256(f.read_bytes()).hexdigest(),f.relative_to(root))
