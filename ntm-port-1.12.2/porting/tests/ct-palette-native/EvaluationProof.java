import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
public class EvaluationProof extends NativeProof {
 static java.util.Random random; static StringBuilder order; static int fail,invalid; static boolean nil; static RecordingWorld w;
 static void step(int n){random.nextInt();order.append(n);if(fail==n)throw new IllegalStateException("failure"+n);}
 static World receiver(){step(0);return nil?null:w;}
 static int arg(int n){step(n);return n==2?(invalid==1?256:64):n==1?(invalid==2?30000000:0):0;}
 static Block block(){step(4);return ConcreteConstructionSlice.deco_steel;}
 static void original(){legacy(receiver(),arg(1),arg(2),arg(3),block(),0,3);}
 static void nativeCall(){com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(receiver(),arg(1),arg(2),arg(3),block());}
 static World holder;static RecordingWorld other;static int mutate(){holder=other;return 0;}
 static void originalMutation(){legacy(holder,mutate(),64,0,ConcreteConstructionSlice.deco_lead,0,3);}
 static void nativeMutation(){com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(holder,mutate(),64,0,ConcreteConstructionSlice.deco_lead);}
 static int conversions;
 static class ConversionWitness extends Block { ConversionWitness(){super(net.minecraft.block.material.Material.ROCK);} public net.minecraft.block.state.IBlockState getStateFromMeta(int meta){conversions++;check(meta==0,"fixed zero state conversion");return getDefaultState();}}
 static void run(){for(int invalidCase=0;invalidCase<3;invalidCase++)for(int nullCase=0;nullCase<2;nullCase++){conversions=0;boolean threw=false;try{com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(nullCase==0?new RecordingWorld():null,invalidCase==1?30000000:0,invalidCase==2?256:64,0,new ConversionWitness());}catch(NullPointerException e){threw=true;}check(threw==(nullCase==1)&&conversions==((nullCase==0&&invalidCase==0)?1:0),"native conversion occurs once only after null/bounds checks");}for(int n=0;n<2;n++){RecordingWorld target=new RecordingWorld();other=new RecordingWorld();holder=n==0?target:null;String oe="";try{originalMutation();}catch(RuntimeException e){oe=e.getClass().getName();}java.util.List<String> expected=new java.util.ArrayList<>(target.trace);target=new RecordingWorld();other=new RecordingWorld();holder=n==0?target:null;String ne="";try{nativeMutation();}catch(RuntimeException e){ne=e.getClass().getName();}check(oe.equals(ne)&&expected.equals(target.trace)&&other.trace.isEmpty(),"receiver captured before coordinate mutation");}for(invalid=0;invalid<3;invalid++)for(int nullCase=0;nullCase<2;nullCase++)for(fail=-1;fail<=4;fail++){nil=nullCase!=0;random=new java.util.Random(918273);w=new RecordingWorld();order=new StringBuilder();String oe="";try{original();}catch(RuntimeException e){oe=e.getClass().getName();}long rng=random.nextLong();String eval=order.toString();java.util.List<String> expected=new java.util.ArrayList<>(w.trace);random=new java.util.Random(918273);w=new RecordingWorld();order=new StringBuilder();String ne="";try{nativeCall();}catch(RuntimeException e){ne=e.getClass().getName();}check(oe.equals(ne)&&eval.equals(order.toString())&&expected.equals(w.trace)&&rng==random.nextLong(),"exact receiver/coordinate/field evaluation and bounds/null timing "+invalid+"/"+nil+"/"+fail);}}
}
