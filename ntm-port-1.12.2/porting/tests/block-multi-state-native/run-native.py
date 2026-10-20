#!/usr/bin/env python3
"""Bounded genuine BlockMulti state owner proof; never runs a full build or downloads."""
import argparse, hashlib, json, os, re, subprocess, tempfile, zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
JAVA=ROOT/'src/main/java'
BASELINE_SHA='a0d90655208c34a59ae81c63c6fce1ce53914e05984b391f3bb9a0ef2187dfad'
COMPOSITION_SHA='c5672f1eaeae3b050fbf4faac03ca9b2ef386dfbdf1464369726fb3e808bf1da'
NAMES=['ore_meteor','concrete_colored_ext','lightstone','vinyl_tile','cm_block','icf_laser_component']
PRODUCTION=['com/hbm/'+p+'.java' for p in [
 'blocks/BlockBase','blocks/BlockMulti','blocks/IBlockMulti','blocks/BlockEnumMulti','blocks/BlockEnums',
 'blocks/generic/BlockGeneric','blocks/generic/BlockDecoToaster','world/gen/nbt/INBTBlockTransformable',
 'blocks/generic/BlockMeteorOre','blocks/generic/BlockConcreteColoredExt','blocks/generic/BlockLightstone',
 'blocks/machine/BlockCM','blocks/machine/BlockICFLaserComponent','items/block/ItemBlockBase',
 'blocks/IPersistentInfoProvider','blocks/ITooltipProvider','tileentity/IPersistentNBT',
 'util/i18n/I18nUtil','util/i18n/ITranslate','lib/RefStrings','render/icon/IIcon','render/icon/IIconRegister',
 'render/icon/LegacyIconRegister','render/icon/LegacyTextureAtlasSprite','render/item/NativeItemTextureRegistry',
 'render/model/LegacyBaseBakedModel','items/ItemEnumMulti','interfaces/IOrderedEnum','util/EnumUtil']]
IMPORTS='''import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
'''
PROPERTY='''\t/**
\t * Stored block metadata is a four-bit value, not necessarily a subtype index:
\t * some descendants also encode orientation or the upper half of a plant.
\t * Keep all bits here and leave item/drop rectification to the owning callback.
\t * Descendants with their own native state container override all three methods.
\t */
\tpublic static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);

'''
METHODS='''\t@Override
\tprotected BlockStateContainer createBlockState() {
\t\treturn new BlockStateContainer(this, METADATA);
\t}

\t@Override
\tpublic IBlockState getStateFromMeta(int meta) {
\t\treturn getDefaultState().withProperty(METADATA, meta & 15);
\t}

\t@Override
\tpublic int getMetaFromState(IBlockState state) {
\t\treturn state.getValue(METADATA);
\t}

'''
def sha(b): return hashlib.sha256(b).hexdigest()
def method(text,signature):
 start=text.index(signature); opening=text.index('{',start); end=opening+1; depth=1
 while depth:
  if text[end]=='{': depth+=1
  if text[end]=='}': depth-=1
  end+=1
 return text[start:end]
def descendants():
 records={}
 for path in JAVA.rglob('*.java'):
  text=path.read_text(); match=re.search(r'\b(?:public\s+)?(?:abstract\s+)?class\s+(\w+)\s+extends\s+([\w.]+)',text)
  if match: records[match.group(1)]=(match.group(2).split('.')[-1],str(path.relative_to(JAVA)))
 selected={'BlockMulti'}
 while True:
  found={name for name,(parent,_) in records.items() if parent in selected}
  if found<=selected: break
  selected|=found
 return {n:records[n] for n in sorted(selected)}
def composed_source(path,data,compose):
 if not compose: return data
 receipt_path=HERE/'composition-scheduled-updates.json'
 assert sha(receipt_path.read_bytes())==COMPOSITION_SHA,'composition receipt changed'
 receipt=json.loads(receipt_path.read_text())
 assert receipt['scheduler_total_calls_verified']==37 and receipt['scheduler_total_files_verified']==21
 assert set(receipt['files'])=={'com/hbm/blocks/generic/BlockAbsorber.java','com/hbm/blocks/generic/BlockNTMSand.java'}
 if path not in receipt['files']: return data
 f=receipt['files'][path]
 assert sha(data)==f['after_sha256'],'only exact scheduled-call composition permitted: '+path
 text=data.decode(); offset=0;positions=[]
 for c in f['calls']:
  start=c['start']+offset;positions.append((start,start+len(c['after']),c));offset+=len(c['after'])-len(c['before'])
 for start,end,c in reversed(positions):
  assert text[start:end]==c['after'],'composed call differs: '+path
  text=text[:start]+c['before']+text[end:]
 assert sha(text.encode())==f['before_sha256'],'composed inverse changed surrounding source: '+path
 return text.encode()

def check_source(compose=False):
 assert sha((HERE/'source-baseline.zip').read_bytes())==BASELINE_SHA,'frozen source baseline changed'
 with zipfile.ZipFile(HERE/'source-baseline.zip') as archive:
  baseline={n:archive.read(n) for n in archive.namelist()}
 current=(JAVA/'com/hbm/blocks/BlockMulti.java').read_text()
 inverse=current
 for addition in (IMPORTS,PROPERTY,METHODS):
  assert inverse.count(addition)==1,'bounded addition changed'
  inverse=inverse.replace(addition,'',1)
 assert inverse.count('public int damageDropped(IBlockState state)')==1
 inverse=inverse.replace('public int damageDropped(IBlockState state)','public int damageDropped(net.minecraft.block.state.IBlockState state)',1)
 assert inverse.encode()==baseline['cp83/com/hbm/blocks/BlockMulti.java'],'whole-file inverse differs from CP83'
 for name,data in baseline.items():
  if name.startswith('cp83/') and name!='cp83/com/hbm/blocks/BlockMulti.java':
   assert composed_source(name[5:],(JAVA/name[5:]).read_bytes(),compose)==data,'untouched closure/route changed: '+name
 pristine=baseline['pristine/com/hbm/blocks/BlockMulti.java'].decode()
 assert method(current,'public int damageDropped(int meta)')==method(pristine,'public int damageDropped(int meta)'),'original drop body changed'
 assert method((JAVA/'com/hbm/blocks/IBlockMulti.java').read_text(),'public default int rectify(int meta)')==method(baseline['pristine/com/hbm/blocks/IBlockMulti.java'].decode(),'public default int rectify(int meta)')
 closure=descendants(); expected=json.loads((HERE/'source-manifest.json').read_text())
 assert closure=={k:tuple(v) for k,v in expected['closure'].items()},'descendant closure changed'
 assert len(closure)==33
 overrides={}
 for name,(_,path) in closure.items():
  text=(JAVA/path).read_text(); found=[m for m in ('createBlockState','getStateFromMeta','getMetaFromState') if re.search(r'\b(?:public|protected)\s+\w+\s+'+m+r'\s*\(',text)]
  if found:
   assert len(found)==3,'partial state owner: '+name
   overrides[name]=found
 assert sorted(overrides)==expected['complete_state_owners']
 assert 'setDefaultState' not in current,'base must not alter subclass-created state container defaults'
 for path,hash_ in expected['compiled_production_sha256'].items(): assert sha((JAVA/path).read_bytes())==hash_,'compiled source changed: '+path
 mod=(JAVA/'com/hbm/blocks/ModBlocks.java').read_text()
 statements=[]
 for name in NAMES:
  matches=re.findall(r'^\s*'+name+r' = .*;$',mod,re.M)
  assert len(matches)==1 and len(re.findall(r'\b'+name+r'\s*=',mod))==1
  statements.append(matches[0].strip())
 assert statements==expected['construction_statements'],'constructor statements changed'
 if compose: print('PASS strict scheduled-update composition: exact four calls in Absorber/NTMSand reconstruct frozen CP83 complete dependency files; no general drift accepted.',flush=True)
 print('PASS frozen CP83 whole-file inverse; 33-class closure; seven complete state roots including new BlockMulti; original drop/rectify unchanged; all six exact constructor statements.',flush=True)
 return baseline,statements

def generate(directory,baseline,statements):
 def write(name,text):
  path=directory/name;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(text);return path
 files=[]
 files.append(write('MultiConstructionSlice.java','''import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import com.hbm.blocks.*;
import com.hbm.blocks.BlockEnums.*;
import com.hbm.blocks.generic.*;
import com.hbm.blocks.machine.*;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
/** Six exact construction statements, not a shortened or substitute ModBlocks owner. */
public final class MultiConstructionSlice {
 public static Block '''+', '.join(NAMES)+''';
 public static void initialize() {
'''+ '\n'.join(statements)+'''
 }
 public static Block[] blocks() { return new Block[]{'''+', '.join(NAMES)+'''}; }
}
'''))
 drop=method(baseline['pristine/com/hbm/blocks/BlockMulti.java'].decode(),'public int damageDropped(int meta)')
 rectify=method(baseline['pristine/com/hbm/blocks/IBlockMulti.java'].decode(),'public default int rectify(int meta)')
 # Java default is interface-only; move only this declaration modifier for an executable pristine-body oracle.
 files.append(write('OriginalMultiOracle.java','public final class OriginalMultiOracle { private final int count; public OriginalMultiOracle(int count){this.count=count;} public int getSubCount(){return count;}\n'+drop+'\n'+rectify.replace('public default int','public int',1)+'\n}\n'))
 files.append(write('com/hbm/main/MainRegistry.java','''package com.hbm.main;
/** Static bootstrap/tab boundary. Translation backend must not be invoked by this mechanics test. */
public final class MainRegistry {
 public static final net.minecraft.creativetab.CreativeTabs blockTab=net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS;
 public static final net.minecraft.creativetab.CreativeTabs machineTab=net.minecraft.creativetab.CreativeTabs.REDSTONE;
 public static final Proxy proxy=new Proxy();
 public static final class Proxy { public com.hbm.util.i18n.ITranslate getI18n(){throw new AssertionError("Translation backend outside state proof");} }
}
'''))
 files.append(write('com/hbm/util/CompatExternal.java','''package com.hbm.util;
/** External multiblock persistence lookup is outside this non-tile proof and must not execute. */
public final class CompatExternal {
 public static net.minecraft.tileentity.TileEntity getCoreFromPos(net.minecraft.world.World world,int x,int y,int z) {
  throw new AssertionError("External core lookup outside state proof");
 }
}
'''))
 return files

def main():
 global ROOT,JAVA
 parser=argparse.ArgumentParser(description=__doc__)
 parser.add_argument('--java-home',type=Path,required=True)
 parser.add_argument('--classpath-file',type=Path,required=True)
 parser.add_argument('--source-root',type=Path,help='Actual port root to compile; evidence baseline stays fixed beside this runner')
 parser.add_argument('--compose-scheduled-updates',action='store_true',help='Accept only the pinned four-call scheduler composition in two closure dependencies')
 parser.add_argument('--negative-control',action='store_true',help='Compile the pinned CP83 metadata-zero BlockMulti and require the actual native test to fail')
 a=parser.parse_args()
 if a.source_root: ROOT=a.source_root.resolve();JAVA=ROOT/'src/main/java'
 baseline,statements=check_source(a.compose_scheduled_updates); cp=a.classpath_file.read_text().strip()
 with tempfile.TemporaryDirectory(prefix='block-multi-state-native-') as tmp:
  directory=Path(tmp)
  sources=[JAVA/p for p in PRODUCTION]+generate(directory,baseline,statements)+[HERE/'BlockMultiStateNativeTest.java',HERE.parent/'ForgeEventNativeLauncher.java']
  if a.negative_control:
   old=directory/'original-owner/com/hbm/blocks/BlockMulti.java';old.parent.mkdir(parents=True,exist_ok=True);old.write_bytes(baseline['cp83/com/hbm/blocks/BlockMulti.java'])
   sources=[old if p==JAVA/'com/hbm/blocks/BlockMulti.java' else p for p in sources]
  for path in sources: print('COMPILE',path.name,sha(path.read_bytes()),flush=True)
  subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(directory/'empty-sourcepath'),'-cp',cp,'-d',tmp]+list(map(str,sources)),check=True)
  result=subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',tmp+os.pathsep+cp,'ForgeEventNativeLauncher','BlockMultiStateNativeTest'],cwd=tmp,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,universal_newlines=True)
  print(result.stdout,end='')
  if a.negative_control:
   assert result.returncode!=0 and 'genuine sixteen-state container' in result.stdout,'old metadata-zero owner did not fail expected native invariant'
   print('PASS negative control: unchanged complete pinned CP83 owner fails genuine sixteen-state container at runtime.')
  else: result.check_returncode()
if __name__=='__main__': main()
