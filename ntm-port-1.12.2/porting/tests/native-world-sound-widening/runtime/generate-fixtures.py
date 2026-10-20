#!/usr/bin/env python3
"""Generate paired invocation fixtures from identical receiver/argument expressions.
Only World type, actual dispatch target and callback adapter differ. No engine shim.
"""
from pathlib import Path
import json,sys
out=Path(sys.argv[1]);out.mkdir(parents=True,exist_ok=True)
shapes=[
 ('migration-IIIFF',['ints[0]','ints[1]','ints[2]','name','floats[0]','floats[1]']),
 ('migration-DIDFF',['doubles[0]','ints[1]','doubles[2]','name','floats[0]','floats[1]']),
 ('robustness-LFIIL',['longs[0]','floats[2]','ints[2]','name','ints[0]','longs[1]']),
 ('robustness-FFFFF',['floats[0]','floats[1]','floats[2]','name','floats[3]','floats[4]']),
 ('robustness-LLLLL',['longs[0]','longs[1]','longs[2]','name','longs[3]','longs[4]']),
 ('existing-DDDFF',['doubles[0]','doubles[1]','doubles[2]','name','floats[0]','floats[1]']),
]
receivers=['c.outer().member.nested().world','c.outer().member.nested().getWorld()']
labels=['x','y','z','name','volume','pitch']
manifest=[]
template='''import java.lang.reflect.*;
import java.util.*;
import sun.misc.Unsafe;
/** Generated paired fixture; actual original/native engine types, no World shim. */
public final class @CLASS@ {
 static final Unsafe U;
 static {try {Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(Unsafe)f.get(null);}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 static final class Context {
  @WORLD@ world;
  Context member=this;
  int[] ints;long[] longs;float[] floats;double[] doubles;String name,fail;
  int nullMode;
  List<String> trace=new ArrayList<>();
  void mark(String s){trace.add(s);if(s.equals(fail))throw new IllegalStateException("scripted:"+s);}
  Context outer(){mark("receiver.outer");return nullMode==2?null:this;}
  Context nested(){mark("receiver.nested");return nullMode==3?null:this;}
  @WORLD@ getWorld(){mark("receiver.getWorld");return world;}
  Context arg(String label){mark(label);return this;}
 }
 @METHODS@
 public static Object[] run(int shape,int receiver,int nullMode,String fail,String name,int[] ints,long[] longs,float[] floats,double[] doubles)throws Exception {
  Context c=new Context();c.ints=ints;c.longs=longs;c.floats=floats;c.doubles=doubles;c.name=name;c.fail=fail;c.nullMode=nullMode;
  List<Object[]> callbacks=new ArrayList<>();
  @SETUP@
  Throwable failure=null;
  try {switch(shape*2+receiver){@CASES@default:throw new AssertionError("bad shape");}}catch(Throwable e){failure=e;}
  String exception=failure==null?null:failure.getClass().getName()+(failure instanceof IllegalStateException?":"+failure.getMessage():"");
  return new Object[]{c.trace,callbacks,exception};
 }
 public static String origin(){return @WORLD@.class.getProtectionDomain().getCodeSource().getLocation().toString();}
}
'''
for legacy in [True,False]:
 cls='LegacyNumericFixture' if legacy else 'NativeNumericFixture'
 world='ahb' if legacy else 'net.minecraft.world.World'
 methods=[];cases=[]
 for si,(label,values) in enumerate(shapes):
  argexpr=[f'c.arg("{n}").member.{v}' for n,v in zip(labels,values)]
  for ri,receiver in enumerate(receivers):
   invocation=receiver+'.a('+','.join(argexpr)+')' if legacy else 'com.hbm.sound.WorldSoundEffects.play('+','.join([receiver]+argexpr)+')'
   methods.append(f' static void call{si}_{ri}(Context c) {{ {invocation}; }}')
   cases.append(f'case {si*2+ri}:call{si}_{ri}(c);break;')
   if legacy:manifest.append({'shape':si,'label':label,'receiverKind':ri,'receiver':receiver,'arguments':argexpr})
 if legacy:
  setup='''ahb real=(ahb)U.allocateInstance(bjf.class);
  if(real.getClass().getMethod("a",double.class,double.class,double.class,String.class,float.class,float.class).getDeclaringClass()!=ahb.class)throw new AssertionError("not genuine inherited World coordinate method");
  ahh observer=(ahh)Proxy.newProxyInstance(ahh.class.getClassLoader(),new Class[]{ahh.class},(p,m,a)->{
   if(m.getName().equals("a")&&a!=null&&a.length==6&&a[0] instanceof String){callbacks.add(a.clone());return null;}
   throw new AssertionError("unexpected original callback:"+m);
  });
  Field f=ahb.class.getDeclaredField("u");f.setAccessible(true);f.set(real,new ArrayList<>(Arrays.asList(observer)));
  c.world=nullMode==1?null:real;'''
 else:
  setup='''net.minecraft.world.World real=(net.minecraft.world.World)U.allocateInstance(net.minecraft.client.multiplayer.WorldClient.class);
  net.minecraft.world.IWorldEventListener observer=(net.minecraft.world.IWorldEventListener)Proxy.newProxyInstance(NativeNumericFixture.class.getClassLoader(),new Class[]{net.minecraft.world.IWorldEventListener.class},(p,m,a)->{
   if(m.getName().equals("playSoundToAllNearExcept")){
    net.minecraft.util.SoundEvent sound=(net.minecraft.util.SoundEvent)a[1];
    if(a[0]!=null||sound!=com.hbm.main.ModSoundEvents.worldEffect(new net.minecraft.util.ResourceLocation(name))||a[2]!=com.hbm.main.ModSoundEvents.worldEffectCategory(new net.minecraft.util.ResourceLocation(name)))throw new AssertionError("native callback metadata");
    callbacks.add(new Object[]{sound.getSoundName().toString(),a[3],a[4],a[5],a[6],a[7]});return null;
   }
   throw new AssertionError("unexpected native callback:"+m);
  });
  Field f=net.minecraft.world.World.class.getDeclaredField("eventListeners");f.setAccessible(true);f.set(real,new ArrayList<>(Arrays.asList(observer)));
  c.world=nullMode==1?null:real;'''
 source=template.replace('@CLASS@',cls).replace('@WORLD@',world).replace('@METHODS@','\n'.join(methods)).replace('@CASES@',''.join(cases)).replace('@SETUP@',setup)
 (out/(cls+'.java')).write_text(source)
(out/'identical-expressions.json').write_text(json.dumps(manifest,indent=2)+'\n')
