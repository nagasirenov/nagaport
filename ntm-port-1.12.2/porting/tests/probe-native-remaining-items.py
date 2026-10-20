#!/usr/bin/env python3
"""Compile complete native callbacks; invoke actual original OBJ/VBO/Java geometry under EGL.
NTM gameplay owners are explicit bounded collaborators, not fake platform APIs.
"""
import argparse,ast,hashlib,os,pathlib,re,subprocess,tempfile,zipfile,json
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2];src=root/'src/main/java/com/hbm'
source_scope=json.loads((root/'porting/tests/native-remaining-items-production-scope.json').read_text())
for path,digest in source_scope['original_sources'].items():
 assert hashlib.sha256((root.parent/'ntm-original-pristine'/path).read_bytes()).hexdigest()==digest,('pristine oracle drift',path)
# Retain the established genuine native support closure, without executing another runner.
prior=ast.parse((root/'porting/tests/probe-native-selective-items.py').read_text())
names=next(ast.literal_eval(n.value) for n in prior.body if isinstance(n,ast.Assign) and any(isinstance(t,ast.Name) and t.id=='names' for t in n.targets))
names+=['render/item/ItemRendererMeteorSword.java','render/item/ItemRendererHot.java','items/special/ItemHot.java','render/item/ItemRenderMissile.java','render/item/ItemRenderMissilePart.java','render/item/nativeitem/context/NativeOriginalItemRenderer.java','render/item/nativeitem/context/NativeToolItemPerspectives.java','render/item/nativeitem/context/NativeWeaponHandState.java','render/util/MissilePart.java','render/util/MissileMultipart.java','handler/MissileStruct.java','render/mesh/ObjMeshModel.java','render/mesh/ReloadableObjMesh.java','render/anim/HbmAnimations.java','render/anim/BusAnimation.java','render/anim/BusAnimationSequence.java','render/anim/BusAnimationKeyframe.java','util/Clock.java','interfaces/NotableComments.java','render/model/ModelB92.java','render/model/ModelBigSword.java','render/model/ModelSword.java']
weapons=['BigSword','RedstoneSword','GunAnim','Shim','Gavel','Chainsaw','Crucible']
names += ['render/item/weapon/ItemRender'+n+'.java' for n in weapons]
files=[src/n for n in dict.fromkeys(names)]
def method(text,signature):
 begin=text.index(signature);brace=text.index('{',begin);level=1;end=brace+1
 while level:
  level+=(text[end]=='{')-(text[end]=='}');end+=1
 return text[begin:end]
def native_nbt(s):
 return s.replace('stack.stackTagCompound = new NBTTagCompound();','stack.setTagCompound(new NBTTagCompound());').replace('stack.stackTagCompound','stack.getTagCompound()')
with tempfile.TemporaryDirectory(prefix='native-remaining-items-') as work:
 tmp=pathlib.Path(work);fixtures=[]
 def fixture(path,text):
  f=tmp/path;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(text);fixtures.append(f)
 part=(src/'render/util/MissilePart.java').read_text();rman=(src/'main/ResourceManager.java').read_text()
 selected='\n'.join(f.read_text() for f in files)
 refs=set(re.findall(r'ResourceManager\.(\w+)',selected))
 fields={};assets=set()
 for name in sorted(refs):
  m=re.search(r'public static final (MeshModel|ResourceLocation) '+name+r'\s*=\s*(.*?);',rman)
  if not m:raise AssertionError('unrecognized actual resource field '+name)
  kind,rhs=m.groups();fields[name]=(kind,rhs)
  assets.update(n.lower() for n in re.findall(r'"((?:models|textures)/[^\"]+)"',rhs))
 # Initializers are copied verbatim from the actual field owner; all parsers/VBOs remain production.
 fixture('com/hbm/main/ResourceManager.java','package com.hbm.main; import com.hbm.render.mesh.*; import com.hbm.render.loader.*; import com.hbm.lib.RefStrings; import net.minecraft.util.ResourceLocation; public class ResourceManager {\n'+ '\n'.join('public static '+kind+' '+n+' = '+rhs+';' for n,(kind,rhs) in fields.items())+'\n}')
 itemrefs=set(re.findall(r'ModItems\.(\w+)',selected)) | {'chainsaw','missile_custom'}; parttypes=dict(re.findall(r'MissilePart.registerPart\(ModItems\.(\w+), PartType\.(\w+)',part))
 declarations=[]
 for n in sorted(itemrefs):
  if n in parttypes: rhs='new com.hbm.items.weapon.ItemCustomMissilePart(com.hbm.items.weapon.ItemCustomMissilePart.PartType.'+parttypes[n]+')'
  elif n=='chainsaw':rhs='new com.hbm.items.tool.ItemToolAbilityFueled()'
  elif n=='missile_custom':rhs='new com.hbm.items.weapon.ItemCustomMissile()'
  else:rhs='new net.minecraft.item.Item()'
  declarations.append('public static net.minecraft.item.Item '+n+' = '+rhs+';')
 fixture('com/hbm/items/ModItems.java','package com.hbm.items; public class ModItems {'+'\n'.join(declarations)+'}')
 fixture('com/hbm/items/weapon/ItemCustomMissilePart.java','package com.hbm.items.weapon; public class ItemCustomMissilePart extends net.minecraft.item.Item { public enum PartType { WARHEAD,FUSELAGE,FINS,THRUSTER,CHIP } public final PartType type; public ItemCustomMissilePart(PartType t){type=t;} }')
 original=(src/'items/weapon/ItemCustomMissile.java').read_text()
 fixture('com/hbm/items/weapon/ItemCustomMissile.java','package com.hbm.items.weapon; import com.hbm.handler.MissileStruct; import net.minecraft.item.*; import net.minecraft.nbt.NBTTagCompound; public class ItemCustomMissile extends Item {'+native_nbt(method(original,'public static int readFromNBT'))+native_nbt(method(original,'public static MissileStruct getStruct'))+'}')
 original=(src/'items/weapon/GunB92.java').read_text()
 fixture('com/hbm/items/weapon/GunB92.java','package com.hbm.items.weapon; import net.minecraft.item.*; import net.minecraft.nbt.NBTTagCompound; public class GunB92 {'+''.join(native_nbt(method(original,s)) for s in ['private static int getAnim','public static float getRotationFromAnim','public static float getTransFromAnim'])+'}')
 fixture('com/hbm/items/tool/ItemToolAbilityFueled.java','package com.hbm.items.tool; public class ItemToolAbilityFueled extends net.minecraft.item.Item { public boolean canOperate(net.minecraft.item.ItemStack stack){return stack.hasTagCompound() && stack.getTagCompound().getBoolean("nativeProbeFuel");} }')
 fixture('com/hbm/render/entity/rocket/part/RenderRocketPart.java','package com.hbm.render.entity.rocket.part; public class RenderRocketPart {}')
 fixture('com/hbm/render/entity/rocket/part/RenderDropPod.java','package com.hbm.render.entity.rocket.part; public class RenderDropPod extends RenderRocketPart {}')
 original=(src/'render/util/MissilePronter.java').read_text()
 fixture('com/hbm/render/util/MissilePronter.java','package com.hbm.render.util; import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.texture.TextureManager; import com.hbm.items.weapon.ItemCustomMissilePart.PartType; public class MissilePronter {'+method(original,'public static void prontMissile')+'}')
 fixture('com/hbm/config/ClientConfig.java','package com.hbm.config; public class ClientConfig { public static final java.util.function.Supplier<Double> GUN_ANIMATION_SPEED=()->1D; }')
 fixture('com/hbm/inventory/recipes/CrucibleRecipes.java','package com.hbm.inventory.recipes; public class CrucibleRecipes { public static java.util.HashMap<Integer,CrucibleRecipe> indexMapping=new java.util.HashMap<>(); public static class CrucibleRecipe { public net.minecraft.item.ItemStack icon; public CrucibleRecipe(net.minecraft.item.ItemStack i){icon=i;} } }')
 # Portable source-pinned original bodies are recorded in the evidence manifest.
 client=(src/'main/ClientProxy.java').read_text()
 lifecycle=method(client,'private void initializeNativeMissileParts').replace('private void initializeNativeMissileParts','public void initialize')
 fixture('NativeMissileLifecycle.java','import com.hbm.render.util.MissilePart; public class NativeMissileLifecycle { private boolean nativeMissilePartsInitialized; '+lifecycle+' public void postLoad(){initialize();} }')
 original_root=root.parent/'ntm-original-pristine/src/main/java/com/hbm'
 for n in weapons+['Missile','MissilePart']:
  group='render/item/weapon/' if n in weapons else 'render/item/'
  actual=(src/(group+'ItemRender'+n+'.java')).read_text();old=(original_root/(group+'ItemRender'+n+'.java')).read_text()
  body=method(old,'public void renderItem');body=body[body.index('{'):]
  body=body.replace('switch(type)','switch(context.transform)').replace('case EQUIPPED_FIRST_PERSON:','case FIRST_PERSON_LEFT_HAND: case FIRST_PERSON_RIGHT_HAND:').replace('case EQUIPPED:','case THIRD_PERSON_LEFT_HAND: case THIRD_PERSON_RIGHT_HAND:').replace('case ENTITY:','case GROUND: case FIXED: case HEAD: case NONE:').replace('case INVENTORY:','case GUI:')
  body=body.replace('(Entity)data[1]','context.holder').replace('EntityPlayer player = Minecraft.getMinecraft().thePlayer;','net.minecraft.entity.EntityLivingBase player = context.holder;').replace('player.isBlocking()','NativeToolItemPerspectives.isBlocking(item,context)').replace('player.isSwingInProgress = false;','if(player != null && player == Minecraft.getMinecraft().player) player.isSwingInProgress = false;').replace('HbmAnimations.getRelevantTransformation("','HbmAnimations.getRelevantTransformation(item,context.holder,"').replace('MathHelper.clamp_double','MathHelper.clamp').replace('System.currentTimeMillis()','animationTimeMillis()')
  if n=='Crucible':
   b=body.index('float prevEq = ReflectionHelper');e=body.index('GL11.glScaled',b);body=body[:b]+'float[] equip=NativeWeaponHandState.snapEquip(item,context);float prevEq=equip[0],eq=equip[1];\n'+body[e:]
  for oldtex in ['ModelB92SM.png','ModelBigSwordTexture.png','ModelSwordRedstone.png']:body=body.replace(oldtex,oldtex.lower())
  imports='\n'.join(line for line in actual.splitlines() if line.startswith('import '))
  ctor='final com.hbm.render.util.MissilePart part;public OracleMissilePart(com.hbm.render.util.MissilePart part){super(part);this.part=part;}' if n=='MissilePart' else ''
  package='com.hbm.render.item.weapon.' if n in weapons else 'com.hbm.render.item.'
  fixture('Oracle'+n+'.java',imports+'\npublic class Oracle'+n+' extends '+package+'ItemRender'+n+' { '+ctor+' @Override protected long animationTimeMillis(){return 123456;} @Override protected void drawModel(ItemStack item,NativeItemContext context) '+body+'}')
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for n in z.namelist():
     if n.endswith('.so') and '/' not in n:(tmp/n).write_bytes(z.read(n))
 tests=[root/'porting/tests/RemainingItemNativeTest.java',root/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(tmp)]+list(map(str,files+fixtures+tests)),check=True)
 assets.update(['textures/misc/glint.png','textures/models/modelb92sm.png','textures/models/modelbigswordtexture.png','textures/models/modelswordredstone.png','textures/models/steelroof.png','textures/models/poletop.png','textures/models/polesatellitereceiver.png'])
 assets.update('textures/items/'+n+'_hot.png' for n in ['ingot_steel_dusted','ingot_chainsteel','ingot_meteorite','ingot_meteorite_forged','blade_meteorite'])
 archive=tmp/'original-materials.zip'
 with zipfile.ZipFile(archive,'w') as z:
  for n in sorted(assets):
   f=root/'src/main/resources/assets/hbm'/n;z.write(f,'assets/hbm/'+n)
 for resource in [root/'src/main/resources',archive]:
  print('Native resource pack:',resource,flush=True)
  subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,'RemainingItemNativeTest',str(resource)],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
 for f in files:print(hashlib.sha256(f.read_bytes()).hexdigest(),f.relative_to(root))
 for n in sorted(assets):
  f=root/'src/main/resources/assets/hbm'/n;print(hashlib.sha256(f.read_bytes()).hexdigest(),f.relative_to(root))
