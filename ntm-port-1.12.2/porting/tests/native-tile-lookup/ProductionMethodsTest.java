import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.util.math.*;
import net.minecraft.init.*;
import net.minecraft.tileentity.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
public class ProductionMethodsTest {
 public static void main(String[] args)throws Exception {
  Bootstrap.register();WorldServer w=TileLookupNativeTest.nativeWorld(WorldServer.class);ChunkProviderServer cp=new ChunkProviderServer(w,null,null);TileLookupNativeTest.field(w,World.class,"chunkProvider",cp);
  Chunk c=TileLookupNativeTest.install(w,cp,-1,-17);BlockPos p=new BlockPos(-1,64,-17);ProductionTileLookup owner=new ProductionTileLookup();owner.setWorld(w);owner.setPos(p);
  TileLookupNativeTest.check(!owner.isUseableByPlayer(null),"production RBMK identity gate rejects missing tile before player access");
  c.getTileEntityMap().put(p,owner);FakePlayer player=(FakePlayer)TileLookupNativeTest.UNSAFE.allocateInstance(FakePlayer.class);player.posX=p.getX()+.5;player.posY=p.getY()+.5;player.posZ=p.getZ()+.5;
  TileLookupNativeTest.check(owner.isUseableByPlayer(player),"production RBMK center allowed");player.posX+=8;player.posZ+=8;TileLookupNativeTest.check(owner.isUseableByPlayer(player),"production exact 128 squared-distance threshold");player.posX+=.01;TileLookupNativeTest.check(!owner.isUseableByPlayer(player),"production beyond original threshold denied");
  c.getTileEntityMap().clear();TileLookupNativeTest.check(owner.getComparatorInputOverride(w,-1,64,-17,0)==0,"production crate absent inventory remains zero");
  TileEntityChest chest=new TileEntityChest();chest.setWorld(w);chest.setPos(p);c.getTileEntityMap().put(p,chest);TileLookupNativeTest.check(owner.getComparatorInputOverride(w,-1,64,-17,0)==0,"production empty inventory zero");
  for(int i=0;i<chest.getSizeInventory();i++)chest.setInventorySlotContents(i,new ItemStack(Items.DIAMOND,64));TileLookupNativeTest.check(owner.getComparatorInputOverride(w,-1,64,-17,0)==15,"production full inventory native comparator max");
  System.out.println("PASS: "+TileLookupNativeTest.checks+" complete extracted production RBMK access and crate comparator checks; not complete owner classes");
 }
}
