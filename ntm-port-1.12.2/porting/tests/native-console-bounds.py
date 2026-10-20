#!/usr/bin/env python3
"""Execute the verbatim console render-bounds method against native TileEntity/AABB."""
import pathlib,subprocess,re,tempfile,os,argparse
p=argparse.ArgumentParser();p.add_argument("--classpath",required=True);p.add_argument("--java-home",type=pathlib.Path,required=True);a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];p=r/'src/main/java/com/hbm/tileentity/machine/rbmk/TileEntityRBMKConsole.java';s=p.read_text();method=re.search(r'public AxisAlignedBB getRenderBoundingBox\(\) \{.*?\n\t\}',s,re.S).group()
java='''import net.minecraft.util.math.*; public class ConsoleBoxTest extends net.minecraft.tileentity.TileEntity { METHOD
 public static void main(String[] args){int n=0;ConsoleBoxTest c=new ConsoleBoxTest();for(int x:new int[]{-29999998,-1,0,29999998})for(int y:new int[]{0,64,255})for(int z:new int[]{-29999998,-1,0,29999998}){c.setPos(new BlockPos(x,y,z));AxisAlignedBB a=c.getRenderBoundingBox();if(a.minX!=x-2||a.minY!=y||a.minZ!=z-2||a.maxX!=x+3||a.maxY!=y+4||a.maxZ!=z+3)throw new AssertionError();n++;}System.out.println("PASS "+n+" actual native console bounding method cases; not whole console compilation.");}}
'''.replace('METHOD',method)
cp=a.classpath;home=a.java_home/'bin'
with tempfile.TemporaryDirectory() as t:
 p=pathlib.Path(t)/'ConsoleBoxTest.java';p.write_text(java);subprocess.run([str(home/'javac'),'-cp',cp,'-d',t,str(p)],check=True);subprocess.run([str(home/'java'),'-cp',t+os.pathsep+cp,'ConsoleBoxTest'],check=True)
