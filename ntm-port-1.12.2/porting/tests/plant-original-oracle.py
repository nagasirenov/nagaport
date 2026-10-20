"""Record exact supplied pristine coral/vine method bodies. No production shims."""
import re

def generate(root):
 result='import java.util.*;import net.minecraft.client.renderer.texture.TextureAtlasSprite;import net.minecraft.util.EnumFacing;\nfinal class PlantOriginalOracle {\n'
 for name,method in [('Coral','coral'),('HangingVine','vine')]:
  s=(root.parent/('ntm-original-pristine/src/main/java/com/hbm/render/block/Render'+name+'.java')).read_text()
  start=s.index('\t\tdouble minU =');end=s.index('\n\t}',start);body=s[start:end]
  body=re.sub(r'tess.addVertexWithUV\((.*?)\);',r'record.add(new double[]{\1});',body)
  result+='static List<double[]> '+method+'(TextureAtlasSprite icon,EnumFacing dir){double x=0,y=0,z=0,height=1;List<double[]> record=new ArrayList<>();\n'+body+'\nreturn record;}\n'
 return result+'}\n'
