#!/usr/bin/env python3
import argparse,json,subprocess,tempfile,os
from pathlib import Path
E=Path(__file__).resolve().parent;ROOT=E.parents[2]
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--native-library-dir',type=Path,required=True);a=p.parse_args()
m=json.loads((E/'manifest.json').read_text());source=(ROOT/m['file']).read_text();save=source.split('public static class GuiStructureSave extends GuiScreen {',1)[1].split('public static class GuiStructureLoad extends GuiScreen {',1)[0];load=source.split('public static class GuiStructureLoad extends GuiScreen {',1)[1]
def between(s,start,end):return s.split(start,1)[1].split(end,1)[0]
def fixture(owner,s):
 init='textName = '+between(s,'textName = ','\n\t\t\tperformAction = ' if owner=='Save' else '\n\t\t\tstructureDirectory.mkdir();')
 key=between(s,'super.keyTyped(typedChar, keyCode);','\n\t\t}' if owner=='Save' else '\n\t\t\tif(!nameFilter.equals')
 mouse=between(s,'super.mouseClicked(mouseX, mouseY, mouseButton);','\n\t\t\tif(performAction' if owner=='Save' else '\n\t\t\tfileList.func_148179_a')
 fields='private GuiTextField textName;'+('private GuiTextField textSizeX,textSizeY,textSizeZ;' if owner=='Save' else 'public String nameFilter;')
 selected='public void selectFile(File file) {'+between(s,'public void selectFile(File file) {','\n\t\t}')+'}' if owner=='Load' else ''
 return 'public static class '+owner+' extends GuiScreen { public Tile tile=new Tile();'+fields+'public void initialize(){'+init+'} public void dispatchKey(char typedChar,int keyCode){'+key+'} public void dispatchMouse(int mouseX,int mouseY,int mouseButton){'+mouse+'}'+selected+'}'
code='import java.io.File;import net.minecraft.client.gui.*;public class StructureDispatchFixture { public static class Tile { public String name="demo";public int sizeX=12,sizeY=34,sizeZ=56;}'+fixture('Save',save)+fixture('Load',load)+'}'
with tempfile.TemporaryDirectory(prefix='structure-text-fields-') as tmp:
 d=Path(tmp);target=d/'StructureDispatchFixture.java';target.write_text(code);cp=a.classpath_file.read_text().strip();javac=[str(a.java_home/'bin/javac'),'-cp',cp,'-d',str(d),str(target),str(E/'StructureFieldProbe.java')]
 subprocess.run(javac,check=True);cmd=[str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(a.native_library_dir),'-cp',str(d)+os.pathsep+cp,'StructureFieldProbe'];subprocess.run(cmd,check=True)
 # Detect an actual compiled duplicate-ID regression, not only a checksum mutation.
 mutated=code.replace('new GuiTextField(1,','new GuiTextField(0,',1);assert mutated!=code;target.write_text(mutated);subprocess.run(javac,check=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE);run=subprocess.run(cmd,text=True,capture_output=True);assert run.returncode!=0 and 'save ID' in run.stderr
 print('REJECTED actual compiled duplicate-ID mutant')
