"""Extract pristine routing/placement methods. Oracle Vec retains original mutable semantics."""
from pathlib import Path
import re,hashlib,json
HERE=Path(__file__).resolve().parent
NAMES=['RailNarrowStraight','RailNarrowCurve','RailStandardStraight','RailStandardStraightShort','RailStandardCurveBase','RailStandardCurveWide7','RailStandardCurveWide9','RailStandardRamp','RailStandardBuffer','BlockRailWaypointSystem','RailStandardSwitch','RailStandardSwitchFlipped']
METHODS=['getDimensions','getOffset','getSnappingPos','getTravelLocation','snapAndMove','getGauge','checkRequirement','fillSpace','canCross','getClosestPointOnLink','getPositionFromNode']
def extract(s,name):
 out=[]
 for m in re.finditer(r'(?:public|protected) (?:static )?(?:[^\n{]*?\s)?'+name+r'\([^)]*\)\s*\{',s):
  i=m.end();d=1
  while d:d+=(s[i]=='{')-(s[i]=='}');i+=1
  out.append(s[m.start():i])
 return out

def calls(s,name,fn):
 start=0
 while True:
  at=s.find(name+'(',start)
  if at<0:return s
  e=at+len(name)+1;d=1;a=e;args=[]
  while d:
   c=s[e]
   if c=='(':d+=1
   elif c==')':d-=1
   elif c==',' and d==1:args.append(s[a:e].strip());a=e+1
   e+=1
  args.append(s[a:e-1].strip());r=fn(args);s=s[:at]+r+s[e:];start=at+len(r)

def generate(original):
 blocks=[];hashes={}
 for helper in ['src/main/java/com/hbm/entity/train/EntityRailCarBase.java','src/main/java/com/hbm/util/BobMathUtil.java']:
  hashes[helper]=hashlib.sha256((Path(original)/helper).read_bytes()).hexdigest()
 for name in NAMES:
  p=Path(original)/('src/main/java/com/hbm/blocks/rail/'+name+'.java');s=p.read_text();hashes[str(p.relative_to(original))]=hashlib.sha256(p.read_bytes()).hexdigest()
  methods='\n'.join(m for n in METHODS for m in extract(s,n))
  # Only platform boundaries change. The original arithmetic, branches, data and mutation stay untouched.
  methods=methods.replace('Vec3','V').replace('MathHelper.clamp_double','MathHelper.clamp').replace('MathHelper.wrapAngleTo180_double','MathHelper.wrapDegrees')
  methods=methods.replace('EntityRailCarBase.generateYaw','yaw').replace('TileEntityRailSwitch','com.hbm.blocks.rail.RailStandardSwitch.TileEntityRailSwitch')
  methods=calls(methods,'world.getBlockMetadata',lambda a:'meta(world, '+', '.join(a)+')')
  pos=lambda a:'new BlockPos('+', '.join(a)+')'
  methods=calls(methods,'world.getTileEntity',lambda a:'world.getTileEntity('+pos(a)+')')
  methods=calls(methods,'world.getBlock',lambda a:'world.getBlockState('+pos(a)+').getBlock()')
  methods=calls(methods,'.isReplaceable',lambda a:'.isReplaceable('+a[0]+', '+pos(a[1:])+')')
  methods=calls(methods,'world.setBlock',lambda a:'world.setBlockState('+pos(a[:3])+', owner.getStateFromMeta('+a[4]+'), '+a[5]+')')
  methods=methods.replace('getDimensions(), this, dir','getDimensions(), owner, dir').replace('new int[] {1, -1, 2, 2, 1, 0}, this, dir','new int[] {1, -1, 2, 2, 1, 0}, owner, dir')
  # Callback contexts retain original public data and native vector oracle is entirely separate.
  methods=methods.replace('new BlockPos(', 'new net.minecraft.util.math.BlockPos(')
  methods=re.sub(r'\.pos\(new net.minecraft.util.math.BlockPos\(', '.pos(new com.hbm.util.fauxpointtwelve.BlockPos(',methods)
  methods=methods.replace('info.pos = new net.minecraft.util.math.BlockPos(', 'info.pos = new com.hbm.util.fauxpointtwelve.BlockPos(')
  parent='RailStandardCurveBase' if name in ['RailStandardCurveWide7','RailStandardCurveWide9'] else 'BlockRailWaypointSystem' if name.startswith('RailStandardSwitch') else 'Base'
  extra=''
  if name=='RailStandardCurveBase':extra='protected int width=4;'
  if name=='RailStandardCurveWide7':extra='RailStandardCurveWide7(){width=6;}'
  if name=='RailStandardCurveWide9':extra='RailStandardCurveWide9(){width=8;}'
  if name=='BlockRailWaypointSystem':extra='public List<RailDef> railDefs=new ArrayList<>();public class RailDef {String name;public List<V> nodes=new ArrayList<>();public RailDef(String name){this.name=name;}}'
  if name.startswith('RailStandardSwitch'):
   c=extract(s,name)[0];body=c[c.index('{')+1:c.rfind('}')];body=re.sub(r'\s*super\([^;]+;','',body);extra=name+'(){'+body.replace('Vec3','V')+'}'
  blocks.append('public static class '+name+' extends '+parent+'{'+extra+methods+'}')
 yaw=extract((Path(original)/'src/main/java/com/hbm/entity/train/EntityRailCarBase.java').read_text(),'generateYaw')[0].replace('generateYaw','yaw').replace('Vec3','V').replace('MathHelper.wrapAngleTo180_double','MathHelper.wrapDegrees')
 template=(HERE/'routing-oracle.java.in').read_text();return template.replace('/* YAW */',yaw).replace('/* OWNERS */','\n'.join(blocks)),hashes
