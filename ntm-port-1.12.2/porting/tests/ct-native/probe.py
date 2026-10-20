#!/usr/bin/env python3
"""CT geometry/mask regression: pinned original source and actual 1.7.10 bytecode.
Compiles selected production sources with real 1.12.2 Minecraft/Forge only.
No downloads, platform stubs, full Gradle build, or graphical context.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile
import zipfile

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
p = argparse.ArgumentParser(description=__doc__)
p.add_argument('--java-home', required=True, type=Path)
p.add_argument('--classpath', required=True)
p.add_argument('--original-root', required=True, type=Path)
p.add_argument('--legacy-client', required=True, type=Path)
p.add_argument('--legacy-srg', required=True, type=Path)
a = p.parse_args()
exe = '.exe' if os.name == 'nt' else ''
manifest = json.loads((HERE / 'original-manifest.json').read_text())
for rel, want in manifest['original_sources'].items():
    got = hashlib.sha256((a.original_root / rel).read_bytes()).hexdigest()
    assert got == want, 'Original source changed: ' + rel
    print('PINNED ORIGINAL', rel, got, flush=True)
for name, path in [('client', a.legacy_client), ('srg', a.legacy_srg)]:
    got = hashlib.sha256(path.read_bytes()).hexdigest()
    assert got == manifest['legacy_artifacts'][name], 'Original artifact changed: ' + name
    print('PINNED ORIGINAL ARTIFACT', name, got, flush=True)
# Confirm that the bytecode names used by the runtime oracle are official mappings,
# rather than relying on guessed class/method names.
with zipfile.ZipFile(a.legacy_srg) as z:
    srg = z.read('joined.srg').decode()
for entry in [
    'CL: blm net/minecraft/client/renderer/RenderBlocks',
    'CL: bmh net/minecraft/client/renderer/Tessellator',
    'CL: rf net/minecraft/util/IIcon',
    'CL: aji net/minecraft/block/Block',
    'FD: bmh/f net/minecraft/client/renderer/Tessellator/field_78405_h',
    'FD: bmh/g net/minecraft/client/renderer/Tessellator/field_78406_i',
    'FD: bmh/p net/minecraft/client/renderer/Tessellator/field_147569_p',
]:
    assert entry in srg, 'Missing pinned mapping: ' + entry

def body(source, signature):
    start = source.index(signature)
    opening = source.index('{', start)
    depth = 1
    end = opening + 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[opening + 1:end - 1]

def complete(source, signature):
    return signature + ' {' + body(source, signature) + '}\n'

ctdir = a.original_root / 'src/main/java/com/hbm/render/block/ct'
context = (ctdir / 'CTContext.java').read_text()
geometry = (ctdir / 'RenderBlocksCT.java').read_text()
icon = (ctdir / 'IconCT.java').read_text()
constants = (ctdir / 'CT.java').read_text()
constants = constants[constants.index('public static final int l'):constants.index('public static int renderID')]
access = context[context.index('public static int[][][] access'):context.index('//lexical coordinates from side')]
access += complete(context, 'private static int[][] lcfs(ForgeDirection up, ForgeDirection left)')
access += complete(context, 'private static int[] cfs(ForgeDirection... dirs)')
access = access.replace('ForgeDirection', 'EnumFacing')
for axis in 'XYZ':
    access = access.replace('.offset' + axis, '.get' + axis + 'Offset()')
corner = complete(context, 'public static int cornerType(boolean hor, boolean corner, boolean vert)')
indices = context[context.index('int itl ='):context.index('faces[i] =')]
iconbody = body(icon, 'public IconCT(IIcon parent, int type)').replace('CT.', 'OriginalCTOracle.')
region = '''static final class Icon {
 final TextureAtlasSprite parent; final int type; float minU,maxU,minV,maxV;
 Icon(TextureAtlasSprite parent,int type){''' + iconbody + '''}
 float getMinU(){return minU;}float getMaxU(){return maxU;}float getMinV(){return minV;}float getMaxV(){return maxV;}
}
'''
faces = ''
for method in ['renderFaceYNeg','renderFaceYPos','renderFaceZNeg','renderFaceZPos','renderFaceXNeg','renderFaceXPos']:
    source = body(geometry, 'public void ' + method + '(Block block, double x, double y, double z, IIcon icon)')
    source = re.sub(r'initSideInfo\(ForgeDirection\.\w+\.ordinal\(\)\);', '', source)
    source = re.sub(r'CTFace face = CTContext.faces\[ForgeDirection\.\w+\.ordinal\(\)\];', 'Face face = new Face(indices, full, connected);', source)
    faces += 'void ' + method + '(int[] indices,TextureAtlasSprite full,TextureAtlasSprite connected,double x,double y,double z){' + source + '}\n'
for signature in [
    'private void drawFace(double[] ftl, double[] ftr, double[] fbl, double[] fbr, IIcon itl, IIcon itr, IIcon ibl, IIcon ibr)',
    'private void drawSubFace(double[] ftl, VertInfo ntl, double[] ftr, VertInfo ntr, double[] fbl, VertInfo nbl, double[] fbr, VertInfo nbr, IIcon icon)',
    'private void drawVert(double[] coord, double u, double v, VertInfo info)',
    'private void drawVert(double x, double y, double z, double u, double v, VertInfo info)',
    'private double[] avgCoords(double[] first, double[] second)',
    'public static class VertInfo',
]:
    faces += complete(geometry, signature).replace('IIcon', 'Icon').replace('Tessellator.instance', 'recorder')
# A deliberately local recorder, never an old Minecraft/Forge API facade.
oracle = '''import java.util.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
final class OriginalCTOracle {
''' + constants + access + corner + region + '''
 static int[] fragments(int mask){boolean[] cons=new boolean[8];for(int n=0;n<8;n++)cons[n]=(mask&(1<<n))!=0;
''' + indices + '''return new int[]{itl,itr,ibl,ibr};}
 static final class Face {
 final Icon[] icons=new Icon[4];Face(int[] indices,TextureAtlasSprite full,TextureAtlasSprite connected){for(int i=0;i<4;i++)icons[i]=new Icon(indices[i]<4?full:connected,indices[i]);}
 Icon getTopLeft(){return icons[0];}Icon getTopRight(){return icons[1];}Icon getBottomLeft(){return icons[2];}Icon getBottomRight(){return icons[3];}
 }
 final Recorder recorder=new Recorder();boolean enableAO=false;VertInfo tl,tc,tr,cl,cc,cr,bl,bc,br;
 static final class Recorder {
 final List<double[]> vertices=new ArrayList<>();
 void addVertexWithUV(double x,double y,double z,double u,double v){vertices.add(new double[]{x,y,z,u,v});}
 void setColorOpaque_F(float r,float g,float b){}void setBrightness(int value){}
 }
''' + faces + '''
 static List<double[]> vertices(int side,int mask,TextureAtlasSprite full,TextureAtlasSprite connected){
 OriginalCTOracle o=new OriginalCTOracle();int[] indices=fragments(mask);
 switch(side){case 0:o.renderFaceYNeg(indices,full,connected,0,0,0);break;case 1:o.renderFaceYPos(indices,full,connected,0,0,0);break;case 2:o.renderFaceZNeg(indices,full,connected,0,0,0);break;case 3:o.renderFaceZPos(indices,full,connected,0,0,0);break;case 4:o.renderFaceXNeg(indices,full,connected,0,0,0);break;case 5:o.renderFaceXPos(indices,full,connected,0,0,0);break;default:throw new AssertionError(side);}
 return o.recorder.vertices;
 }
}
'''
item = body(geometry, 'public void renderBlockAsItem(Block block, int meta, float mult)')
assert 'GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);' in item
assert 'GL11.glTranslatef(-0.5F, -0.5F, -0.5F);' in item
assert re.findall(r'super\.(renderFace\w+)\(', item) == ['renderFaceYNeg','renderFaceYPos','renderFaceZNeg','renderFaceZPos','renderFaceXNeg','renderFaceXPos']
production = [
 'com/hbm/render/block/ct/CT.java',
 'com/hbm/render/block/ct/CTContext.java',
 'com/hbm/render/block/ct/IBlockCT.java',
 'com/hbm/render/block/nativeblock/ConveyorCuboidGeometry.java',
 'com/hbm/render/block/ct/RenderBlocksCT.java',
 'com/hbm/render/block/RenderBlockCT.java',
]
# Production geometry may have a small native-lighting helper, included by its real path.
for source in sorted((ROOT / 'src/main/java/com/hbm/render/block/ct').glob('*.java')):
    rel = source.relative_to(ROOT / 'src/main/java').as_posix()
    if rel not in production and source.name not in ('CTStitchReceiver.java', 'IconCT.java'):
        production.append(rel)
with tempfile.TemporaryDirectory(prefix='ct-native-proof-') as temporary:
    work = Path(temporary)
    files = []
    for name, content in [('OriginalCTOracle', oracle), ('CTNativeTest', (HERE / 'CTNativeTest.java.in').read_text())]:
        target = work / (name + '.java')
        target.write_text(content)
        files.append(str(target))
    for rel in production:
        source = ROOT / 'src/main/java' / rel
        print('PRODUCTION', rel, hashlib.sha256(source.read_bytes()).hexdigest(), flush=True)
        files.append(str(source))
    subprocess.run([str(a.java_home / 'bin' / ('javac' + exe)), '-encoding', 'UTF-8', '-source', '8', '-target', '8', '-cp', a.classpath, '-d', temporary] + files, check=True)
    subprocess.run([str(a.java_home / 'bin' / ('java' + exe)), '-Djava.awt.headless=true', '-Xmx768m', '-cp', temporary + os.pathsep + a.classpath, 'CTNativeTest', str(a.legacy_client)], cwd=temporary, check=True)
print('LIMITATION: Native full-corner interpolation is tested, not claimed bit-identical to the complete original 1.7.10 light solver. Native destruction retexturing bypasses CTLightQuad; old/new crack-overlay pixel parity remains open.', flush=True)
