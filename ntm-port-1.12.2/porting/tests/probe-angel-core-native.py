#!/usr/bin/env python3
"""Full production native item components against actual target API; independent matrix oracle.
No test Minecraft/Forge substitutions and no production source exclusions. Does not start GL.
"""
import argparse,pathlib,subprocess,tempfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2];src=root/'src/main/java/com/hbm'
files=list((src/'render/item/nativeitem').glob('*.java'))+[src/'render/item/ItemRenderAngelCore.java',src/'render/mesh/MeshModel.java',src/'render/mesh/ObjMesh.java',src/'render/mesh/ObjMeshParser.java',root/'porting/tests/AngelCoreNativeTest.java']
with tempfile.TemporaryDirectory(prefix='angel-core-native-') as tmp:
 subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-cp',a.classpath,'-d',tmp]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',tmp+':'+a.classpath,'AngelCoreNativeTest',str(root/'src/main/resources/assets/hbm/models/mobs/balefire_angel.obj'),str(root/'src/main/resources')],cwd=tmp,check=True)
