#!/usr/bin/env python3
import pathlib,subprocess,tempfile,sys,os
h=pathlib.Path(__file__).resolve().parent;j=pathlib.Path(sys.argv[1]);cpfile=pathlib.Path(sys.argv[2]);tools=str(j/'lib/tools.jar')
prefix='import net.minecraft.world.World; import net.minecraft.world.chunk.Chunk; import net.minecraft.entity.Entity; import net.minecraft.client.entity.EntityPlayerSP; import net.minecraft.tileentity.TileEntity; import net.minecraft.world.gen.MapGenBase; '
cases=[
 ('class T {Object read(Entity e){return e.worldObj;}}',1),
 ('class T {Object read(EntityPlayerSP e){return e.worldObj;}}',1),
 ('class T {Object read(Chunk c){return c.worldObj;}}',1),
 ('class T extends TileEntity {Object read(World world){return worldObj;}}',1),
 ('class T extends TileEntity {void unrelated(World worldObj){} Object read(){return worldObj;}}',1),
 ('class T extends TileEntity {class Inner {Object read(){return worldObj;}}}',1),
 ('class T extends MapGenBase {Object read(){return this.worldObj;}}',1),
 ('class T {Object worldObj;Object read(T e){return e.worldObj;}}',0),
 ('class T extends net.minecraft.entity.item.EntityItem {World world; T(){super((World)null);} Object read(){return this.worldObj;}}',0),
 ('class T {Object read(Object c){return c.worldObj;}}',0),
 ('class T {Object read(Missing c){return c.worldObj;}}',0),
 ('class T<C extends Chunk> {Object read(C c){return c.worldObj;}}',0),
 ('class T {Object read(){return Entity.worldObj;}}',0),
 ('class T {Object read(net.minecraft.world.WorldProvider c){return c.worldObj;}}',0),
 ('class T extends TileEntity implements InterfaceShadow {Object read(){return this.worldObj;}} interface InterfaceShadow {World world=null;}',0),
]
rejected=[
 'class T {void write(Chunk c,World w){c.worldObj=w;}}',
 'class T {void write(Chunk c,World w){(c.worldObj)=w;}}',
 'class T extends TileEntity {Object read(World worldObj){return worldObj;}}',
 'class T extends TileEntity {class Inner {World world;Object read(){return worldObj;}}}',
 'class T extends TileEntity {static Object read(){return worldObj;}}',
 'class T extends Chunk {T(){super(null,0,0);} public World getWorld(){return null;}Object read(){return this.worldObj;}}',
]
with tempfile.TemporaryDirectory(prefix='world-read-controls-') as td:
 d=pathlib.Path(td);out=d/'classes';out.mkdir();subprocess.run([str(j/'bin/javac'),'-cp',tools,'-d',str(out),str(h/'RemainingWorldScanner.java')],check=True)
 def scan(sources):
  paths=[];targets=[]
  for i,src in enumerate(sources):
   f=d/'src/main/java'/('Control'+str(i)+'.java');f.parent.mkdir(parents=True,exist_ok=True);f.write_text(prefix+__import__('re').sub(r'\bT\b','Control'+str(i),src));paths.append(str(f));targets.append('src/main/java/'+f.name+':1')
  inp=d/'inputs';inp.write_text('\n'.join(paths));target=d/'targets';target.write_text('\n'.join(targets));return subprocess.run([str(j/'bin/java'),'-cp',str(out)+os.pathsep+tools,'RemainingWorldScanner',str(inp),str(cpfile),str(target)],text=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
 result=scan([x for x,n in cases]);assert result.returncode==0,result.stderr
 rows=result.stdout.splitlines()
 for i,(src,n) in enumerate(cases):assert len([r for r in rows if r.startswith('src/main/java/Control'+str(i)+'.java\t')])==n,(i,rows,result.stderr)
 for src in rejected:
  result=scan([src]);assert result.returncode!=0 and 'AssertionError' in result.stderr,(src,result.stdout,result.stderr)
 print('PASS 21 compiler-attributed direct/outer/local/interface/hidden/unknown/static/read-write and virtual-override selection controls')
