import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.YellowBarrel;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
/** Reconstructed bounded test; whole production owner and ancestors, not an engine facade. */
public class YellowStateProof extends NativeProof {
 static class ScheduledWorld extends RecordingWorld {
  int schedules; Block scheduled; BlockPos position; int delay;
  public void scheduleUpdate(BlockPos p,Block b,int ticks){schedules++;scheduled=b;position=p;delay=ticks;}
 }
 static void run(){
  Bootstrap.register();
  net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
  net.minecraftforge.fml.common.ModMetadata metadata=new net.minecraftforge.fml.common.ModMetadata();metadata.modId="hbm";metadata.name="YellowPlacementProbe";metadata.version="1";
  net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(metadata);
  net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);
  // Actual constructor/material; UI and registry initialization are bounded.
  YellowBarrel barrel=new YellowBarrel(Material.IRON);barrel.setRegistryName("hbm","yellow_barrel");
  ModBlocks.yellow_barrel=barrel;net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(barrel);
  check(barrel.getBlockState().getValidStates().size()==16,"all sixteen actual native owner states");
  check(barrel.getDefaultState()==barrel.getStateFromMeta(0),"actual owner default equals metadata zero");
  BlockPos p=new BlockPos(7,64,-9);ScheduledWorld w=new ScheduledWorld();
  for(int m=0;m<16;m++){
   IBlockState state=barrel.getStateFromMeta(m);check(state.getBlock()==barrel&&barrel.getMetaFromState(state)==m,"native metadata roundtrip "+m);
   AxisAlignedBB box=barrel.getBoundingBox(state,w,p);check(box.equals(new AxisAlignedBB(.125,0,.125,.875,1,.875)),"actual barrel bounds "+m);
   check(box.equals(barrel.getCollisionBoundingBox(state,w,p)),"collision matches bounds "+m);
  }
  com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(w,7,64,-9,barrel);
  check(w.trace.size()==1&&w.trace.get(0).equals(event(7,64,-9,barrel,0,3)),"real registered owner metadata zero flags three");
  // Recording placement does not dispatch hooks: call actual hooks explicitly.
  barrel.onBlockAdded(w,p,barrel.getStateFromMeta(0));check(w.schedules==1&&w.scheduled==barrel&&w.position.equals(p)&&w.delay==20,"native onBlockAdded schedules actual owner");
  barrel.updateTick(w,p,barrel.getStateFromMeta(0),new java.util.Random(1));
  check(w.schedules==2&&w.delay==20,"native updateTick reschedules");
  check(com.hbm.handler.radiation.ChunkRadiationManager.proxy.calls.equals(java.util.Arrays.asList("7,64,-9:5.0")),"actual yellow owner radiation dispatch");
  System.out.println("PASS actual YellowBarrel hierarchy, sixteen native states, metadata-zero helper placement, bounds and explicit lifecycle hooks.");
 }
}
