#!/usr/bin/env python3
"""Native target API probe of extracted Component methods, not the full mod class."""
from pathlib import Path
import argparse, re, subprocess, tempfile
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
root=Path(__file__).resolve().parents[2]
s=(root/'src/main/java/com/hbm/world/gen/component/Component.java').read_text()
def balanced(start):
 i=s.index('{',start);j=i+1;d=1
 while d:d+=(s[j]=='{')-(s[j]=='}');j+=1
 return s[start:j]
names=['getAverageHeight','setAverageHeight','getCoordMode','setCoordMode','getXWithOffset','getZWithOffset','placeBlockAtCurrentPosition','getBlockAtCurrentPosition','placeFoundationUnderneath','placeBlocksOnTop','fillWithAir','fillWithBlocks','fillWithMetadataBlocks','fillWithRandomizedBlocks','fillWithRandomizedBlocksMeta','randomlyFillWithBlocks','placeDoor','placeLever','getButtonMeta','placeBed']
methods=[]
for m in re.finditer(r'(?:public|protected) (?:static )?(?:void|int|Block|boolean) (\w+)\(',s):
 if m[1] in names:methods.append(balanced(m.start()))
civilian=(root/'src/main/java/com/hbm/world/gen/component/CivilianFeatures.java').read_text()
for name in ['BrokenStairs','BrokenBlocks']:
 start=civilian.index('public static class '+name);i=civilian.index('{',start);j=i+1;d=1
 while d:d+=(civilian[j]=='{')-(civilian[j]=='}');j+=1
 methods.append(civilian[start:j])
java='''import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.gen.structure.*;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.nbt.NBTTagCompound;
import com.hbm.world.gen.LegacyStructureOrientation;
import com.hbm.world.gen.nbt.selector.*;
public class ComponentBlockProbe extends StructureComponent {
protected int hpos = -1;
%s
protected void writeStructureToNBT(NBTTagCompound n) {}
protected void readStructureFromNBT(NBTTagCompound n, TemplateManager t) {}
public boolean addComponentParts(World w, Random r, StructureBoundingBox b) { throw new UnsupportedOperationException(); }
static class RecordingWorld extends World {
 Map<BlockPos,IBlockState> states;
 List<BlockPos> writes;
 List<Integer> flags;
 private RecordingWorld() { super(null,null,null,null,false); }
 protected net.minecraft.world.chunk.IChunkProvider createChunkProvider() { throw new UnsupportedOperationException(); }
 protected boolean isChunkLoaded(int x,int z,boolean empty) { return true; }
 public IBlockState getBlockState(BlockPos p) { return states.getOrDefault(p,Blocks.AIR.getDefaultState()); }
 public boolean setBlockState(BlockPos p,IBlockState s,int f) { states.put(p,s); writes.add(p);flags.add(f);return true; }
 public boolean isAirBlock(BlockPos p) { return getBlockState(p).getBlock()==Blocks.AIR; }
 static RecordingWorld make() throws Exception {
  java.lang.reflect.Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);
  RecordingWorld w=(RecordingWorld)((sun.misc.Unsafe)f.get(null)).allocateInstance(RecordingWorld.class);
  w.states=new HashMap<>();w.writes=new ArrayList<>();w.flags=new ArrayList<>();return w;
 }
}
static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
static class Selector extends BlockSelector {
 List<BlockPos> calls=new ArrayList<>(); List<Boolean> walls=new ArrayList<>();
 public void selectBlocks(Random r,int x,int y,int z,boolean wall) { calls.add(new BlockPos(x,y,z));walls.add(wall);blockstate=Blocks.STONE.getStateFromMeta(r.nextInt(7)); }
}
public static void main(String[] args) throws Exception {
 Bootstrap.register();
 for(int mode=0;mode<4;mode++) {
  ComponentBlockProbe c=new ComponentBlockProbe(); c.boundingBox=new StructureBoundingBox(10,20,30,20,30,40);c.setCoordMode(mode);
  StructureBoundingBox box=new StructureBoundingBox(0,0,0,100,100,100);
  RecordingWorld w=RecordingWorld.make();
  for(int meta=0;meta<8;meta++) { c.placeBlockAtCurrentPosition(w,Blocks.OAK_STAIRS,meta,1,meta,2,box); BlockPos pos=new BlockPos(c.getXWithOffset(1,2),20+meta,c.getZWithOffset(1,2)); check(Blocks.OAK_STAIRS.getMetaFromState(w.getBlockState(pos))==meta,"no double rotation"); }
  int n=w.writes.size();c.placeBlockAtCurrentPosition(w,Blocks.STONE,0,200,0,0,box);check(w.writes.size()==n,"placement clipping");
  w=RecordingWorld.make();c.fillWithMetadataBlocks(w,box,0,0,0,2,2,2,Blocks.STONE,2,Blocks.STONE,5,false);check(w.writes.size()==27,"volume");check(Blocks.STONE.getMetaFromState(w.getBlockState(new BlockPos(c.getXWithOffset(1,1),21,c.getZWithOffset(1,1))))==5,"interior metadata");check(w.flags.stream().allMatch(f->f==2),"update flags");
  w=RecordingWorld.make();Selector selector=new Selector();Random rng=new Random(91),expected=new Random(91);c.fillWithRandomizedBlocks(w,box,0,0,0,2,2,2,false,rng,selector);
  int k=0;for(int x=0;x<=2;x++)for(int z=0;z<=2;z++)for(int y=0;y<=2;y++) {BlockPos pos=new BlockPos(c.getXWithOffset(x,z),20+y,c.getZWithOffset(x,z));check(selector.calls.get(k).equals(pos),"selector coordinate order");check(selector.walls.get(k)==(x==0||x==2||y==0||y==2||z==0||z==2),"boundary flag");check(Blocks.STONE.getMetaFromState(w.getBlockState(pos))==expected.nextInt(7),"seeded metadata");k++;}check(rng.nextLong()==expected.nextLong(),"RNG consumption");
  w=RecordingWorld.make();selector=new Selector();rng=new Random(9);expected=new Random(9);c.fillWithRandomizedBlocks(w,box,0,0,0,2,2,2,true,rng,selector);check(w.writes.isEmpty()&&selector.calls.isEmpty()&&rng.nextLong()==expected.nextLong(),"onlyReplace skips RNG");
  w=RecordingWorld.make();selector=new Selector();rng=new Random(17);expected=new Random(17);c.fillWithRandomizedBlocksMeta(w,box,0,0,0,1,1,1,rng,selector,0);for(BlockPos pos:w.writes)check(Blocks.STONE.getMetaFromState(w.getBlockState(pos))==expected.nextInt(7),"metadata selector native state");check(rng.nextLong()==expected.nextLong(),"metadata selector RNG");
  w=RecordingWorld.make();rng=new Random(212);expected=new Random(212);c.randomlyFillWithBlocks(w,box,rng,.4F,0,0,0,2,2,2,Blocks.STONE,3);int placements=0;for(int x=0;x<=2;x++)for(int z=0;z<=2;z++)for(int y=0;y<=2;y++)if(expected.nextFloat()<=.4F){BlockPos pos=new BlockPos(c.getXWithOffset(x,z),20+y,c.getZWithOffset(x,z));check(w.writes.get(placements++).equals(pos)&&Blocks.STONE.getMetaFromState(w.getBlockState(pos))==3,"random fill order and meta");}check(w.writes.size()==placements&&rng.nextLong()==expected.nextLong(),"random fill RNG");
  w=RecordingWorld.make();c.placeFoundationUnderneath(w,Blocks.STONE,3,0,0,0,0,0,box);check(w.writes.size()==16,"foundation legacy brake");check(w.writes.get(15).getY()==5,"foundation descent");
  w=RecordingWorld.make();c.fillWithAir(w,new StructureBoundingBox(0,21,0,100,30,100),0,0,0,2,2,2);check(w.writes.isEmpty(),"vertical rejection");
 }
 BrokenStairs stairs=new BrokenStairs();BrokenBlocks broken=new BrokenBlocks();Random br=new Random(451),be=new Random(451);
 for(int i=0;i<20000;i++){int meta=i&7;stairs.setMetadata(meta);stairs.selectBlocks(br,0,0,0,false);float f=be.nextFloat();Block b=f<.7?Blocks.OAK_STAIRS:f<.97?Blocks.WOODEN_SLAB:Blocks.AIR;check(stairs.getBlockState().getBlock()==b,"broken stairs distribution");check(b.getMetaFromState(stairs.getBlockState())==(b==Blocks.OAK_STAIRS?meta:0),"stairs-only metadata");broken.selectBlocks(br,0,0,0,false);f=be.nextFloat();b=f<.6?Blocks.PLANKS:f<.8?Blocks.OAK_STAIRS:Blocks.WOODEN_SLAB;int bm=f>=.6&&f<.8?be.nextInt(4):0;check(broken.getBlockState().getBlock()==b&&b.getMetaFromState(broken.getBlockState())==bm,"broken blocks distribution");}check(br.nextLong()==be.nextLong(),"broken selectors RNG");
 StoneBrickSelector stone=new StoneBrickSelector();Random actual=new Random(123),expected=new Random(123);
 for(int i=0;i<20000;i++){stone.selectBlocks(actual,0,0,0,false);float f=expected.nextFloat();check(stone.getBlockState().getBlock()==Blocks.STONEBRICK,"stone block");check(Blocks.STONEBRICK.getMetaFromState(stone.getBlockState())==(f<.2F?2:f<.5F?1:0),"stone metadata distribution");}check(actual.nextLong()==expected.nextLong(),"stone RNG consumption");
 BrickSelector brick=new BrickSelector();brick.selectBlocks(actual,0,0,0,false);check(brick.getBlockState()==Blocks.BRICK_BLOCK.getDefaultState(),"brick state");
 BiomeTopSelector top=new BiomeTopSelector();top.nextBiome=Biomes.DESERT;top.selectBlocks(actual,0,0,0,false);check(top.getBlockState()==Biomes.DESERT.topBlock,"biome top state");
 BiomeFillerSelector filler=new BiomeFillerSelector();filler.nextBiome=Biomes.DESERT;filler.selectBlocks(actual,0,0,0,false);check(filler.getBlockState()==Biomes.DESERT.fillerBlock,"biome filler state");
 System.out.println("PASS: extracted Component helpers compile against native target APIs; recording World subclass verifies coordinates, metadata, flags, clipping, order, RNG, boundary flags and foundation brake across four orientations; production NBT selectors and extracted civilian selectors, 20000 seeded rolls each. Not full Component/mod compilation or live-world integration.");
}
}'''%'\n'.join(methods)
with tempfile.TemporaryDirectory(prefix='hbm-component-probe-') as out:
 path=Path(out)/'ComponentBlockProbe.java';path.write_text(java)
 sources=[root/'src/main/java/com/hbm/world/gen/LegacyStructureOrientation.java']+list((root/'src/main/java/com/hbm/world/gen/nbt/selector').glob('*.java'))
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',a.classpath,'-d',out,str(path)]+list(map(str,sources)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',out+':'+a.classpath,'ComponentBlockProbe'],check=True,cwd=out)
