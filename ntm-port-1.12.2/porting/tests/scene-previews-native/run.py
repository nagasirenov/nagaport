#!/usr/bin/env python3
"""Complete scene callback compile and real native dispatcher/buffer/offscreen tests.
NTM-only collaborator boundaries are explicitly listed in fixtures below; no platform substitutes.
"""
import argparse, os, pathlib, subprocess, tempfile, zipfile, hashlib, re
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--compile-only',action='store_true');a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[3];src=root/'src/main/java';here=pathlib.Path(__file__).resolve().parent
names=['com/hbm/interfaces/Spaghetti.java','com/hbm/wiaj/GuiWorldInAJar.java','com/hbm/inventory/gui/GUIScreenSlicePrinter.java','com/hbm/render/util/RenderOverhead.java','com/hbm/render/util/NativeSceneBlockDraw.java','com/hbm/wiaj/WorldInAJar.java','com/hbm/wiaj/JarScript.java','com/hbm/wiaj/JarScene.java','com/hbm/wiaj/actions/IJarAction.java','com/hbm/wiaj/actors/ISpecialActor.java','com/hbm/wiaj/actors/ActorFancyPanel.java','com/hbm/wiaj/cannery/CanneryBase.java','com/hbm/entity/boss/INtmBossDisplay.java','com/hbm/util/Clock.java','com/hbm/lib/RefStrings.java','com/hbm/lib/ForgeDirection.java','com/hbm/util/fauxpointtwelve/BlockPos.java','com/hbm/util/fauxpointtwelve/Rotation.java','com/hbm/render/util/NativeGuiItemDraw.java','com/hbm/render/util/NativeItemBaseModel.java','com/hbm/render/util/NativeItemQuadRenderer.java','com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java']
fixtures={
'com/hbm/util/BobMathUtil.java':'''package com.hbm.util; public class BobMathUtil { public static double interp(double x,double y,float interp) { return x + (y - x) * interp; } }''',
'com/hbm/util/i18n/I18nUtil.java':'''package com.hbm.util.i18n; import java.util.*; public class I18nUtil { public static String resolveKey(String s,Object...args){return s;} public static List<String> autoBreak(Object font,String text,int width){return Arrays.asList(text);} }''',
'com/hbm/blocks/machine/BlockPWR.java':'''package com.hbm.blocks.machine; public class BlockPWR extends net.minecraft.block.Block { public BlockPWR(){super(net.minecraft.block.material.Material.ROCK);} public boolean hasTileEntity(net.minecraft.block.state.IBlockState s){return true;} public net.minecraft.tileentity.TileEntity createTileEntity(net.minecraft.world.World w,net.minecraft.block.state.IBlockState s){return new TileEntityBlockPWR();} public static class TileEntityBlockPWR extends net.minecraft.tileentity.TileEntity { public net.minecraft.block.Block block; } }''',
'com/hbm/inventory/gui/GUIScreenWikiRender.java':'''package com.hbm.inventory.gui; import java.io.File; import java.util.*; public class GUIScreenWikiRender { public static final List<String> calls=new ArrayList<>(); public static void saveScreenshot(File d,String sub,String n,int x,int y,int w,int h,int c){calls.add(d.getName()+":"+sub+":"+n+":"+x+":"+y+":"+w+":"+h+":"+c);} }'''
}
with tempfile.TemporaryDirectory(prefix='native-scene-previews-') as tmp:
 out=pathlib.Path(tmp);files=[src/n for n in names]+[root/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java']
 if not a.compile_only:
  files.append(here/'ScenePreviewsNativeTest.java')
  pristine=root.parent/'ntm-original-pristine/src/main/java'
  old=(pristine/'com/hbm/render/util/RenderOverhead.java').read_text()
  def segment(start,end):return old[old.index(start):old.index(end)]
  def vertices(part,receiver):return re.findall(re.escape(receiver)+r'\.addVertex\(([^;]+)\);',part)
  thermal=vertices(segment('public static void renderThermalSight','public static final HashMap'),'tess')
  marker=vertices(segment('public static void renderMarkers','public static class Marker'),'tess')
  tag=vertices(segment('public static void drawTag(float','public static void renderThermalSight'),'tessellator')
  assert len(thermal)==24 and len(marker)==24 and len(tag)==4
  def array(es):return 'new double[][] {'+','.join('{'+e+'}' for e in es)+'}'
  def body(file):
   text=(pristine/file).read_text();start=text.index('private void setupRotation() {');at=text.index('{',start);depth=1;end=at+1
   while depth:
    depth += (text[end]=='{')-(text[end]=='}');end+=1
   print('PRISTINE MATRIX ORACLE',hashlib.sha256(text.encode()).hexdigest(),file,flush=True)
   return text[at+1:end-1]
  oracle='import org.lwjgl.opengl.GL11; import net.minecraft.util.math.AxisAlignedBB; import com.hbm.wiaj.JarScript; import com.hbm.lib.ForgeDirection; public class OriginalSceneGeometry {'
  oracle+='public static double[][] thermal(AxisAlignedBB bb,double x,double y,double z){return '+array(thermal)+';}'
  oracle+='public static double[][] marker(int pX,int pY,int pZ,double minX,double minY,double minZ,double maxX,double maxY,double maxZ,double x,double y,double z){return '+array(marker)+';}'
  oracle+='public static double[][] tag(int center,int heightOffset){return '+array(tag)+';}'
  oracle+='public static void jar(int width,int height,JarScript jarScript){'+body('com/hbm/wiaj/GuiWorldInAJar.java')+'}'
  oracle+='public static void slice(int width,int height,int sizeX,int sizeY,int sizeZ,ForgeDirection dir){'+body('com/hbm/inventory/gui/GUIScreenSlicePrinter.java')+'}}'
  f=out/'OriginalSceneGeometry.java';f.write_text(oracle);files.append(f)
  print('PRISTINE GEOMETRY ORACLE',hashlib.sha256(old.encode()).hexdigest(),'72 thermal/marker and 12 tag coordinates, expressions unchanged',flush=True)
 for n,s in fixtures.items():
  f=out/n;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(s);files.append(f);print('NTM COLLABORATOR FIXTURE',n,flush=True)
 for n in names:print('WHOLE INPUT',hashlib.sha256((src/n).read_bytes()).hexdigest(),n,flush=True)
 # No repository build/classes: all listed production roots are compiled afresh.
 cp=os.pathsep.join(s for s in a.classpath.split(os.pathsep) if pathlib.Path(s).is_file())
 for jar in cp.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for n in z.namelist():
     if n.endswith('.so') and '/' not in n:(out/n).write_bytes(z.read(n))
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-cp',cp,'-d',tmp]+list(map(str,files)),check=True)
 if not a.compile_only:subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+tmp,'-cp',tmp+os.pathsep+cp,'ScenePreviewsNativeTest',str(root/'src/main/resources')],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
 print('PASS complete native scene callback compile; explicitly bounded NTM collaborators, no Minecraft/Forge replacements.',flush=True)
