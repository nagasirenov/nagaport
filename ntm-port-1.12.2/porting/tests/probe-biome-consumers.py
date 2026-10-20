#!/usr/bin/env python3
"""Extract actual production biome predicates/lookup expressions into a native fixture.
NOT whole consumer compilation. No replacement Minecraft/Forge classes.
"""
import argparse, importlib.util, os, re, subprocess, tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
root=Path(__file__).resolve().parents[2]
spec=importlib.util.spec_from_file_location('audit',Path(__file__).with_name('check-biome-consumers.py'));audit=importlib.util.module_from_spec(spec);spec.loader.exec_module(audit);audit.verify()
def source(rel):return (root/'src/main/java/com/hbm'/rel).read_text(encoding='utf-8')
def block(s,signature):
 start=s.index(signature); opening=s.index('{',start);i=opening+1;depth=1
 while depth:depth+=(s[i]=='{')-(s[i]=='}');i+=1
 return s[start:i]
ntm=source('world/gen/NTMWorldGenerator.java');cave=source('world/feature/BiomeCave.java');features=source('world/gen/MapGenNTMFeatures.java');arctic=source('world/dungeon/ArcticVault.java');jar=source('wiaj/WorldInAJar.java');hbm=source('lib/HbmWorld.java');mine=source('render/tileentity/RenderLandmine.java')
methods='\n'.join([block(ntm,'public static boolean isInvalidBiome('),block(ntm,'public static boolean isFlatBiome('),block(cave,'private static EnumBiomeType getTypeFromBiome('),block(jar,'public Biome getBiome('),block(source('blocks/BlockEnums.java'),'public static enum EnumBiomeType')])
predicates=re.findall(r'canSpawn = biome -> ([^;]+);',ntm)
assert len(predicates)==32, len(predicates)
# Compile every actual spawning predicate, including duplicates and their original order.
methods+='\nstatic Predicate<Biome>[] predicates = new Predicate[]{'+','.join('(Predicate<Biome>)(biome -> '+e+')' for e in predicates)+'};'
exclusions=re.search(r'biomelist = (Arrays.asList\(new Biome\[\].*?\));',features).group(1)
methods+='\nstatic List<Biome> exclusions = '+exclusions+';'
oil=re.search(r'sandOilBubble.canSpawn = biome -> (.*?);',hbm).group(1)
methods+='\nstatic boolean oil(Biome biome) {return '+oil+';}'
raw_desert=re.search(r'else if\((biome.getDefaultTemperature\(\).*?)\) bindTexture',mine).group(1)
methods+='\nstatic boolean desertTexture(Biome biome) {return '+raw_desert+';}'
for name,s,pattern,params in [
 ('arcticLookup',arctic,r'Biome biome = (.*?);','World world, int x, int z'),
 ('caveLookup',cave,r'Biome biome = (.*?);','World world, int x, int z'),
 ('featureLookup',features,r'Biome biome = (.*?);','World world, int i, int j')]:
 expression=re.search(pattern,s).group(1);methods+='\nstatic Biome '+name+'('+params+') { return '+expression+'; }'
expression=re.search(r'Biome biomegenbase = (.*?);',features).group(1)
methods+='\nBiome structureLookup(int k, int l) { return '+expression+'; }\nWorld world;'
expression=re.search(r'if\((biome.getTemperature\(new BlockPos\(x, y, z\)\) < 0.2)',arctic).group(1)
methods+='\nstatic boolean cold(Biome biome,int x,int y,int z) {return '+expression+';}'
expression=re.search(r'Biome biome = (.*?);',mine).group(1)
methods+='\nstatic Biome mineLookup(net.minecraft.tileentity.TileEntity tileEntity) {return '+expression+';}'
java='''import java.util.*;
import java.util.function.Predicate;
import net.minecraft.init.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.*;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraft.world.storage.*;
import net.minecraft.world.chunk.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
public class BiomeConsumerNativeProbe {
'''+methods+'''
 static int nextBiome;
 static class TestBiome extends Biome {
  TestBiome(float t,float r,float h) {this(t,r,h,0.1F);}
  TestBiome(float t,float r,float h,float base) {super(new BiomeProperties("probe").setTemperature(t).setRainfall(r).setHeightVariation(h).setBaseHeight(base)); setRegistryName("minecraft", "hbm_native_probe_biome_"+(nextBiome++)); net.minecraftforge.fml.common.registry.ForgeRegistries.BIOMES.register(this);}
 }
 static class TrackingBiomes extends BiomeProviderSingle {
  BlockPos last; TrackingBiomes(){super(Biomes.PLAINS);}
  public Biome getBiome(BlockPos pos){last=pos;return super.getBiome(pos);}
 }
 static class Provider extends WorldProviderSurface {
  protected void init() { this.biomeProvider = new TrackingBiomes(); }
 }
 static class TestWorld extends World {
  Chunk chunk; boolean loaded=true;
  TestWorld() {
   super(new SaveHandlerMP(),new WorldInfo(new WorldSettings(1,GameType.SURVIVAL,false,false,WorldType.DEFAULT),"probe"),new Provider(),new Profiler(),false);
   provider.setWorld(this); chunk=new Chunk(this,-1,-1);
   Arrays.fill(chunk.getBiomeArray(),(byte)Biome.getIdForBiome(Biomes.DESERT));
   chunkProvider=createChunkProvider();
  }
  protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return loaded;}
  protected IChunkProvider createChunkProvider(){return new IChunkProvider(){
   public Chunk getLoadedChunk(int x,int z){return chunk;}
   public Chunk provideChunk(int x,int z){return chunk;}
   public boolean tick(){return false;}
   public String makeString(){return "probe";}
   public boolean isChunkGeneratedAt(int x,int z){return true;}
  };}
 }
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args) {
  Bootstrap.register();
  // Initializers reference Biomes: construct fixture only after native bootstrap.
  BiomeConsumerNativeProbe fixture=new BiomeConsumerNativeProbe();
  int cases=0;
  for(float t:new float[]{0.49F,0.5F,0.99F,1F,1.49F,1.5F})
   for(float r:new float[]{0F,0.1F,0.249F,0.25F,0.251F})
    for(float h:new float[]{0.049F,0.05F,0.2F,0.201F,0.3F}) {
     TestBiome biome=new TestBiome(t,r,h);
     BiomeDictionary.addTypes(biome,Type.SPARSE);
     check(getTypeFromBiome(biome)==(t>=1 && r<0.25 ? EnumBiomeType.DESERT:t>=0.5 && r>0.25 ? EnumBiomeType.WOODLAND:null),"cave raw threshold");
     check(isFlatBiome(biome)==(h<=0.2F),"flat threshold");
     check(desertTexture(biome)==(t>=1.5F&&r<=0.1F),"texture threshold");
     check(oil(biome)==(!biome.canRain()&&t>=1.5F),"oil predicate");
     for(Predicate<Biome> predicate:predicates)predicate.test(biome);
     cases++;
    }
  for(float base:new float[]{-1.501F,-1.5F,-1.499F}) {
   TestBiome sea=new TestBiome(0.5F,0.5F,0.1F,base);BiomeDictionary.addTypes(sea,Type.OCEAN);
   check(predicates[7].test(sea)==(base>=-1.5F),"oil-rig base-height boundary");
  }
  for(Biome biome:new Biome[]{Biomes.OCEAN,Biomes.RIVER,Biomes.FROZEN_OCEAN,Biomes.FROZEN_RIVER,Biomes.DEEP_OCEAN}) {
   check(exclusions.contains(biome),"exact exclusion");check(isInvalidBiome(biome),"dictionary invalid");
  }
  TestBiome otherOcean=new TestBiome(0.5F,0.5F,0.1F);BiomeDictionary.addTypes(otherOcean,Type.OCEAN);
  check(isInvalidBiome(otherOcean)&&!exclusions.contains(otherOcean),"identity exclusion must not broaden");
  check(getTypeFromBiome(Biomes.OCEAN)==null,"ocean woodland exclusion");
  check(!Biomes.ICE_PLAINS.canRain()&&!Biomes.DESERT.canRain()&&Biomes.PLAINS.canRain(),"snow/rain disabled");
  TestBiome high=new TestBiome(0.5F,0.5F,0.1F);
  check(high.getDefaultTemperature()==0.5F&&high.getTemperature(new BlockPos(0,255,0))<0.5F,"raw versus altitude");
  TestBiome nearCold=new TestBiome(0.2F,0.5F,0.1F);
  check(!cold(nearCold,0,0,0)&&cold(nearCold,0,255,0),"arctic intentionally altitude adjusted");
  TestWorld world=new TestWorld();fixture.world=world;
  check(arcticLookup(world,-8,-8)==Biomes.PLAINS,"arctic provider");
  check(fixture.structureLookup(-1,-1)==Biomes.PLAINS,"structure provider negative chunk center");
  check(((TrackingBiomes)world.getBiomeProvider()).last.equals(new BlockPos(-8,0,-8)),"negative center coordinates");
  check(caveLookup(world,-8,-8)==Biomes.DESERT&&featureLookup(world,-8,-8)==Biomes.DESERT,"loaded chunk route");
  net.minecraft.tileentity.TileEntity tile=new net.minecraft.tileentity.TileEntity() {};
  tile.setWorld(world);tile.setPos(new BlockPos(-8,240,-8));
  check(mineLookup(tile)==Biomes.DESERT,"native TileEntity biome lookup");
  world.loaded=false;
  check(caveLookup(world,-8,-8)==Biomes.PLAINS,"unloaded chunk fallback");
  check(fixture.getBiome(new BlockPos(-8,240,-8))==Biomes.PLAINS,"jar constant biome");
  System.out.println("PASS: extracted production methods/expressions against native Forge 2768: "+cases+" climate/height cases, all spawning lambdas, identity/dictionary exclusions, rain/snow, altitude, actual World+Chunk versus BiomeProvider routes. No whole-class/game claim.");
 }
}
'''
# Avoid accessing Biomes before bootstrap: initialized by an explicit static helper after registration.
java=java.replace('static List<Biome> exclusions = '+exclusions+';', 'static List<Biome> exclusions;')
java=java.replace('Bootstrap.register();','Bootstrap.register(); exclusions = '+exclusions+';')
with tempfile.TemporaryDirectory(prefix='hbm-biome-native-') as out:
 file=Path(out)/'BiomeConsumerNativeProbe.java';file.write_text(java,encoding='utf-8')
 subprocess.run([str(a.java_home/'bin'/('javac.exe' if os.name=='nt' else 'javac')),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',out,str(file)],check=True)
 subprocess.run([str(a.java_home/'bin'/('java.exe' if os.name=='nt' else 'java')),'-cp',out+os.pathsep+a.classpath,'BiomeConsumerNativeProbe'],check=True,cwd=out)
