#!/usr/bin/env python3
"""Compile actual direct renderers against native Forge/LWJGL; bounded NTM collaborators only.
No platform substitutes, installs or downloads. Resource draw spies do not certify assets/game visuals.
"""
import argparse, hashlib, os, pathlib, re, subprocess, tempfile, zipfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2]
names=['render/item/ItemRenderMissileGeneric.java','render/item/weapon/ItemRenderFireExt.java',
'render/item/nativeitem/direct/DirectMeshItemRenderer.java','render/item/nativeitem/direct/DirectMeshItemModel.java',
'render/item/nativeitem/direct/DirectMeshItemRegistration.java','render/item/nativeitem/direct/PlainItemPerspectives.java',
'render/util/NativeItemBaseModel.java','render/item/nativeitem/StaticMeshItemModel.java','render/item/nativeitem/StaticMeshItemRegistration.java',
'render/item/nativeitem/ItemTransformMatrix.java','render/item/nativeitem/AngelCoreFrameCorrection.java',
'render/item/nativeitem/staticfamily/OrdinaryMeshItemModel.java','render/item/nativeitem/MaterialTextureAtlasSprite.java',
'render/mesh/MeshModel.java','render/mesh/ObjMesh.java']
files=[root/'src/main/java/com/hbm'/n for n in names]
missile=files[0].read_text();fireext=files[1].read_text()
# The complete original consumer/material/scale table is intentionally unchanged.
baseline=subprocess.check_output(['git','show','f45ef1d63a0d8cd793b90d207d6ea7362793d442:./src/main/java/com/hbm/render/item/ItemRenderMissileGeneric.java'],cwd=root,text=True)
assert missile[missile.index('\tpublic static Consumer<TextureManager> generateStandard'):]==baseline[baseline.index('\tpublic static Consumer<TextureManager> generateStandard'):]
print('PASS exact original missile consumer/material/scale table source guard',flush=True)
with tempfile.TemporaryDirectory(prefix='native-direct-items-') as tmp:
 tmp=pathlib.Path(tmp)
 def fixture(name,body):
  path=tmp/(name+'.java');path.parent.mkdir(parents=True,exist_ok=True);path.write_text(body);files.append(path)
 items=sorted(set(re.findall(r'ModItems\.(\w+)',missile)))
 fixture('com/hbm/items/ModItems','package com.hbm.items; public class ModItems {'+''.join('public static final net.minecraft.item.Item '+n+'=new net.minecraft.item.Item();' for n in items)+'}')
 fixture('com/hbm/inventory/RecipesCommon','''package com.hbm.inventory; public class RecipesCommon { public static class ComparableStack {
 final net.minecraft.item.Item item; final int meta; int count;
 public ComparableStack(net.minecraft.item.Item i){item=i;meta=0;count=1;}
 public ComparableStack(net.minecraft.item.ItemStack s){item=s.getItem();meta=s.getMetadata();count=s.getCount();}
 public ComparableStack makeSingular(){count=1;return this;}
 public int hashCode(){return System.identityHashCode(item)*31+meta*13+count;}
 public boolean equals(Object o){if(!(o instanceof ComparableStack))return false;ComparableStack b=(ComparableStack)o;return item==b.item&&meta==b.meta&&count==b.count;}
 }}''')
 refs=sorted(set(re.findall(r'ResourceManager\.(\w+)',missile+fireext)))
 fixture('com/hbm/main/ResourceManager','''package com.hbm.main;
 import com.hbm.render.mesh.*; import java.util.*; import org.lwjgl.opengl.GL11;
 public class ResourceManager {
 public static class Draw implements MeshModel {
 public int calls; public javax.vecmath.Matrix4f matrix; public int shade; public boolean cull, lighting; public float alpha;
 public ObjMesh getMesh(){return ObjMesh.empty();} public List<String> getPartNames(){return Collections.emptyList();}
 public void renderAll(){calls++;java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,b);float[] f=new float[16];b.get(f);matrix=new javax.vecmath.Matrix4f(f);matrix.transpose();shade=GL11.glGetInteger(GL11.GL_SHADE_MODEL);cull=GL11.glIsEnabled(GL11.GL_CULL_FACE);lighting=GL11.glIsEnabled(GL11.GL_LIGHTING);alpha=GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);}
 public void renderPart(String n){throw new AssertionError(n);} public void renderOnly(String... n){throw new AssertionError();} public void renderAllExcept(String...n){throw new AssertionError();}
 }
 '''+''.join(('public static final net.minecraft.util.ResourceLocation '+n+'=new net.minecraft.util.ResourceLocation("test","'+n.lower()+'");') if n.endswith('_tex') else ('public static final Draw '+n+'=new Draw();') for n in refs)+'}')
 fixture('com/hbm/items/weapon/sedna/factory/XFactoryTool','package com.hbm.items.weapon.sedna.factory; public class XFactoryTool { public static final Object fext_foam=new Object(), fext_sand=new Object(); }')
 fixture('com/hbm/items/weapon/sedna/mags/IMagazine','package com.hbm.items.weapon.sedna.mags; public interface IMagazine { Object getType(net.minecraft.item.ItemStack stack, net.minecraft.entity.EntityLivingBase holder); }')
 fixture('com/hbm/items/weapon/sedna/ItemGunBaseNT','''package com.hbm.items.weapon.sedna;
 public class ItemGunBaseNT extends net.minecraft.item.Item {
 public final java.util.List<net.minecraft.item.ItemStack> seen=new java.util.ArrayList<>(); public Object type;
 public Config getConfig(net.minecraft.item.ItemStack s,int index){if(index!=0)throw new AssertionError();seen.add(s);return new Config();}
 public class Config {public Receiver[] getReceivers(net.minecraft.item.ItemStack s){seen.add(s);return new Receiver[]{new Receiver()};}}
 public class Receiver {public com.hbm.items.weapon.sedna.mags.IMagazine getMagazine(net.minecraft.item.ItemStack s){seen.add(s);return (stack,holder)->{if(holder!=null)throw new AssertionError();seen.add(stack);return type;};}}
 }''')
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(tmp/name).write_bytes(z.read(name))
 assert (tmp/'liblwjgl64.so').exists(),'Existing native Linux LWJGL JAR required; no downloads or installs.'
 files += [root/'porting/tests/DirectMeshItemNativeTest.java',root/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(tmp)]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,'DirectMeshItemNativeTest'],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
 for f in files[:len(names)]:print(hashlib.sha256(f.read_bytes()).hexdigest(),f.relative_to(root))
