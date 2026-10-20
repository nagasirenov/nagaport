#!/usr/bin/env python3
"""Complete remaining native particle classes and exact helper callers, explicit NTM boundaries."""
import pathlib,argparse,tempfile,subprocess,os,zipfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests'
names=['MukeCloud','MukeCloudBF','NT','Giblet','Rift','RocketFlame','SpentCasing','Skeleton','Debris','Foam','SmokePlume','Spark','Letter','MukeFlash','RocketBF','RocketFusion','RocketMUD','RocketSCH','RocketUP','MukeWave','AtlasVertex','VertexFormats','Sprites','Definition']
paths=['com/hbm/particle/Particle'+n+'.java' for n in names]+['com/hbm/particle/SpentCasing.java','com/hbm/handler/CasingEjector.java','com/hbm/wiaj/WorldInAJar.java','com/hbm/lib/RefStrings.java','com/hbm/lib/ForgeDirection.java','com/hbm/main/ModSoundEvents.java','com/hbm/util/Vec3NT.java','com/hbm/util/Tuple.java','com/hbm/interfaces/Spaghetti.java','com/hbm/render/mesh/MeshModel.java','com/hbm/render/mesh/ObjMesh.java']
paths+=['com/hbm/particle/helper/'+n+'.java' for n in ['IParticleCreator','CasingCreator','SkeletonCreator','ExplosionCreator']]
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
with tempfile.TemporaryDirectory(prefix='remaining-particles-native-') as d:
 out=pathlib.Path(d);files=[s/x for x in paths]
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
 print('PASS complete remaining particle family plus exact creators/ejector/native block access compile',flush=True)
 if (t/'NativeRemainingParticlesTest.java').exists():subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+d,'-cp',d+os.pathsep+a.classpath,'ForgeEventNativeLauncher','NativeRemainingParticlesTest',str(r/'src/main/resources')],cwd=d,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
