"""Generate bounded, recording-only geometry oracles directly from supplied pristine source."""
from pathlib import Path
import re

def generate(root):
 p=root.parent/'ntm-original-pristine/src/main/java/com/hbm/render/block'
 chain=(p/'RenderChain.java').read_text()
 chain=chain[chain.index('\t\tif(l == 0)'):chain.index('\n\t\treturn true;')]
 chain=chain.replace('iicon','sprite')
 # A test recorder, not a production platform adapter.
 chain=chain.replace('tessellator.addVertexWithUV(', 'record.add(')
 chain=re.sub(r'record.add\((.*?)\);',r'record.add(new double[] {\1});',chain)
 fence=(p/'RenderFence.java').read_text();fence=fence[fence.index('\t\tboolean xNeg'):fence.index('\n\t\tfence.setBlockBoundsBasedOnState')]
 fence=re.sub(r'fence.canConnectFenceTo\(world, x - 1, y, z\)', '(mask & 1) != 0', fence)
 fence=re.sub(r'fence.canConnectFenceTo\(world, x \+ 1, y, z\)', '(mask & 2) != 0', fence)
 fence=re.sub(r'fence.canConnectFenceTo\(world, x, y, z - 1\)', '(mask & 4) != 0', fence)
 fence=re.sub(r'fence.canConnectFenceTo\(world, x, y, z \+ 1\)', '(mask & 8) != 0', fence)
 fence=fence.replace('fence.postIcon','post').replace('renderer.renderStandardBlock(fence, x, y, z)','renderer.record(meta == 0 ? panel : post)')
 return '''import java.util.*;import net.minecraft.client.renderer.texture.TextureAtlasSprite;
final class StructuralOriginalOracle {
 static List<double[]> chain(int l,TextureAtlasSprite sprite) {
  List<double[]> record=new ArrayList<>();double x=0,y=0,z=0,scale=0,wallOffset=.05D;
  double d0=sprite.getMinU(),d1=sprite.getMinV(),d2=sprite.getMaxU(),d3=sprite.getMaxV();
'''+chain+'''\nreturn record;}
 static class Box {double[] bounds;TextureAtlasSprite material;boolean mirror;Box(double[] b,TextureAtlasSprite s,boolean m){bounds=b;material=s;mirror=m;}}
 static class Recorder {List<Box> boxes=new ArrayList<>();double[] bounds;TextureAtlasSprite override;boolean field_152631_f;
 void setRenderBounds(double... b){bounds=b;}void setOverrideBlockTexture(TextureAtlasSprite s){override=s;}void clearOverrideBlockTexture(){override=null;}
 void record(TextureAtlasSprite s){boxes.add(new Box(bounds,override==null?s:override,field_152631_f));}}
 static List<Box> fence(int meta,int mask,TextureAtlasSprite panel,TextureAtlasSprite post){Recorder renderer=new Recorder();
'''+fence+'''\nreturn renderer.boxes;}}
'''
