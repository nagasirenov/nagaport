#!/usr/bin/env python3
"""Native vanilla variants, whole small creator and exact extracted ClientProxy branches."""
import pathlib,argparse,tempfile,subprocess,os,zipfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests'
subprocess.run(['python3',str(t/'check-native-vanilla-particles-source.py')],check=True)
names=['ExplosionSmall','MukeCloud','MukeCloudBF','NT','Giblet','Rift','RocketFlame','SpentCasing','Skeleton','Debris','Foam','SmokePlume','Spark','Letter','MukeFlash','RocketBF','RocketFusion','RocketMUD','RocketSCH','RocketUP','MukeWave','AtlasVertex','VertexFormats','Sprites','Definition']
paths=['com/hbm/particle/Particle'+n+'.java' for n in names]+['com/hbm/particle/SpentCasing.java','com/hbm/handler/CasingEjector.java','com/hbm/wiaj/WorldInAJar.java','com/hbm/lib/RefStrings.java','com/hbm/lib/ForgeDirection.java','com/hbm/main/ModSoundEvents.java','com/hbm/util/Vec3NT.java','com/hbm/util/Tuple.java','com/hbm/interfaces/Spaghetti.java','com/hbm/render/mesh/MeshModel.java','com/hbm/render/mesh/ObjMesh.java']
paths += ['com/hbm/particle/NativeVanillaParticles.java','com/hbm/particle/EntityFXRotating.java']
paths+=['com/hbm/particle/helper/'+n+'.java' for n in ['ExplosionSmallCreator','IParticleCreator','CasingCreator','SkeletonCreator','ExplosionCreator']]
paths+=['com/hbm/render/mesh/'+n+'.java' for n in ['ObjMeshModel','ObjMeshParser','ObjMeshRenderer','ObjMeshVboPayload','ReloadableObjMesh']]+['com/hbm/render/loader/HFRWavefrontObject.java','com/hbm/render/loader/HFRWavefrontObjectVBO.java']
collaborators={
'com/hbm/main/ClientProxy.java':'''package com.hbm.main;public class ClientProxy {public static int vanished=-1;public static void vanish(int id){vanished=id;}}''',
'com/hbm/main/MainRegistry.java':'''package com.hbm.main;public class MainRegistry {}''',
'com/hbm/main/ResourceManager.java':'''package com.hbm.main;public class ResourceManager {public static final net.minecraft.util.ResourceLocation casings_tex=new net.minecraft.util.ResourceLocation("hbm","textures/particle/casings.png");public static final com.hbm.render.mesh.MeshModel casings=new com.hbm.render.loader.HFRWavefrontObject(new net.minecraft.util.ResourceLocation("hbm","models/effect/casings.obj")).asVBO(),sphere_uv=new com.hbm.render.mesh.ObjMeshModel(new net.minecraft.util.ResourceLocation("hbm","models/sphere_uv.obj"));}''',
'com/hbm/util/BobMathUtil.java':'''package com.hbm.util;public class BobMathUtil {public static double safeClamp(double val,double min,double max){val=net.minecraft.util.math.MathHelper.clamp(val,min,max);if(val==Double.NaN)val=(min+max)/2D;return val;}public static double interp(double a,double b,double t){return a+(b-a)*t;}}''',
'com/hbm/entity/mob/EntityDummy.java':'''package com.hbm.entity.mob;public abstract class EntityDummy extends net.minecraft.entity.EntityLivingBase {protected EntityDummy(net.minecraft.world.World w){super(w);}}''',
'com/hbm/entity/mob/EntityUndeadSoldier.java':'''package com.hbm.entity.mob;public abstract class EntityUndeadSoldier extends net.minecraft.entity.EntityLivingBase {protected EntityUndeadSoldier(net.minecraft.world.World w){super(w);}}''',
'com/hbm/packet/toclient/AuxParticlePacketNT.java':'''package com.hbm.packet.toclient;public class AuxParticlePacketNT {public final net.minecraft.nbt.NBTTagCompound data;public final double x,y,z;public AuxParticlePacketNT(net.minecraft.nbt.NBTTagCompound n,double x,double y,double z){data=n.copy();this.x=x;this.y=y;this.z=z;}}''',
'com/hbm/handler/threading/PacketThreading.java':'''package com.hbm.handler.threading;public class PacketThreading {public static Object lastPacket;public static net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint lastPoint;public static void createAllAroundThreadedPacket(Object packet,net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint point){lastPacket=packet;lastPoint=point;}}'''
}
# Extract complete, unchanged top-level branches from the actual production caller.
import re,hashlib,json
source=(s/'com/hbm/main/ClientProxy.java').read_text()
selected=['waterSplash','cloudFX2','fireworks','vanillaburst','vanillaExt','vanilla','jetpack','bnuuy','jetpack_bj','jetpack_dns','sweat','vomit','radiation','schrabfog','fluidfill']
branches={}
for name in selected:
 marker='\t\tif("'+name+'".equals(type)) {'
 start=source.index(marker);brace=source.index('{',start);depth=1;i=brace+1
 while depth:
  if source[i]=='{':depth+=1
  elif source[i]=='}':depth-=1
  i+=1
 branches[name]=source[start:i]
# Every native helper use in effectNT must be covered, not a handpicked success subset.
method=source[source.index('public void effectNT('):source.index('\n\t@Override',source.index('public void effectNT('))]
assert sum(v.count('NativeVanillaParticles.') for v in branches.values()) == method.count('NativeVanillaParticles.'), 'uncovered native caller'
caller='import com.hbm.particle.*;import com.hbm.util.BobMathUtil;import net.minecraft.client.Minecraft;import net.minecraft.client.particle.Particle;import net.minecraft.client.renderer.texture.TextureManager;import net.minecraft.world.World;import net.minecraft.entity.*;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.block.Block;import net.minecraft.block.state.IBlockState;import net.minecraft.init.Blocks;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.util.EnumFacing;import net.minecraft.util.math.*;import java.util.*;import java.awt.Color; public class NativeVanillaCaller { public static void effect(World world, EntityPlayer player, NBTTagCompound data) { TextureManager man=Minecraft.getMinecraft().renderEngine;Random rand=world.rand;int particleSetting=Minecraft.getMinecraft().gameSettings.particleSetting;String type=data.getString("type");double x=data.getDouble("posX"), y=data.getDouble("posY"), z=data.getDouble("posZ");'+ '\n'.join(branches.values())+'}}'
with tempfile.TemporaryDirectory(prefix='vanilla-particles-native-') as d:
 out=pathlib.Path(d);files=[s/x for x in paths]
 (out/'NativeVanillaCaller.java').write_text(caller);files.append(out/'NativeVanillaCaller.java')
 files.append(t/'NativeVanillaParticlesTest.java')
 for path,body in collaborators.items():
  dest=out/'fixtures'/path;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(body);files.append(dest)
 files += [t/'ForgeEventNativeLauncher.java',t/'MaterialTextureAtlasSpriteNativeTest.java',s/'com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java']
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(out/name).write_bytes(z.read(name))
 assert (out/'liblwjgl64.so').exists()
 if (t/'NativeRemainingParticlesTest.java').exists():files.append(t/'NativeRemainingParticlesTest.java')
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'fixtures'),'-cp',a.classpath,'-d',d]+list(map(str,files)),check=True)
 print('PASS complete native vanilla helper/ExplosionSmallCreator plus 15 unchanged extracted ClientProxy branches compile',flush=True)
 if (t/'NativeRemainingParticlesTest.java').exists():subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+d,'-cp',d+os.pathsep+a.classpath,'ForgeEventNativeLauncher','NativeVanillaParticlesTest',str(r/'src/main/resources')],cwd=d,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
