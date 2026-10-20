#!/usr/bin/env python3
import argparse,hashlib,importlib.util,json,re,subprocess,tempfile,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];JAVA=M/'src/main/java'
NAMES=['deco_steel','deco_tungsten','deco_red_copper','deco_lead','deco_rusty_steel']
PRODUCTION=[JAVA/p for p in ['com/hbm/blocks/BlockBase.java','com/hbm/blocks/generic/BlockGeneric.java','com/hbm/lib/RefStrings.java','com/hbm/render/icon/IIcon.java','com/hbm/render/icon/IIconRegister.java','com/hbm/blocks/generic/BlockDecoCT.java','com/hbm/render/block/ct/CT.java','com/hbm/render/block/ct/CTContext.java','com/hbm/render/block/ct/IBlockCT.java']]
def check_source():
 manifest=json.loads((H/'owner-manifest.json').read_text())
 for path,digest in manifest['files'].items():assert hashlib.sha256((M/path).read_bytes()).hexdigest()==digest,'Owner prerequisite changed: '+path
 registry=(JAVA/'com/hbm/blocks/ModBlocks.java').read_text()
 for name,statement in manifest['constructors'].items():
  found=re.findall(r'^\s*'+name+r' = .*;$',registry,re.M);assert len(found)==1 and found[0].strip()==statement
  assert len(re.findall(r'\b'+name+r'\s*=(?!=)',registry))==1
  registrations=re.findall(r'^\s*register\('+name+r',.*;$',registry,re.M);assert len(registrations)==1 and registrations[0].strip()==manifest['registrations'][name]
 print('PASS exact CT owner/native model source hashes and five exact constructor assignments; no new visual policy acceptance.')
def generate_slices(directory):
 t=Path(directory);spec=importlib.util.spec_from_file_location('previous_owners',H.parent/'structure-palette-native/run-owners.py');previous=importlib.util.module_from_spec(spec);spec.loader.exec_module(previous);generated=previous.generate_slices(t)
 # Existing Generic constructors remain only for the unchanged Library helper fixture; CT blocks below are actual selected owners.
 manifest=json.loads((H/'owner-manifest.json').read_text());p=t/'ConcreteConstructionSlice.java';s=p.read_text().replace('import net.minecraft.block.Block;','import net.minecraft.block.Block; import com.hbm.blocks.generic.BlockDecoCT; import com.hbm.blocks.ModBlocks;')
 s=s.replace('public static void initialize() {','public static Block '+', '.join(NAMES)+';\npublic static void initialize() {\n'+'\n'.join(manifest['constructors'][n] for n in NAMES)+'\nModBlocks.deco_steel=deco_steel; ModBlocks.deco_rusty_steel=deco_rusty_steel; for(Block registered : new Block[]{'+', '.join(NAMES)+'}) registered.setRegistryName("hbm",registered.getTranslationKey());')
 s=s.replace('reinforced_sand, brick_light};','reinforced_sand, brick_light, '+', '.join(NAMES)+'};');p.write_text(s)
 table=t/'com/hbm/blocks/ModBlocks.java';table.parent.mkdir(parents=True,exist_ok=True);table.write_text('package com.hbm.blocks; /** Explicit static identity collaborator only; not the complete content registry. */ public class ModBlocks { public static net.minecraft.block.Block deco_steel, deco_rusty_steel; }\n');generated.append(table)
 return generated
def main():
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-client',type=Path,required=True);a=p.parse_args();check_source();assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce';cp=a.classpath_file.read_text().strip()
 with tempfile.TemporaryDirectory(prefix='ct-palette-owners-') as td:
  t=Path(td);generated=generate_slices(t);subprocess.run(list(map(str,[a.java_home/'bin/javac','-encoding','UTF-8','-sourcepath',t,'-cp',cp,'-d',t,H/'CTStateTest.java']+generated+PRODUCTION)),check=True);subprocess.run(list(map(str,[a.java_home/'bin/java','-cp',str(t)+':'+cp,'CTStateTest',a.legacy_client])),check=True,cwd=t)
if __name__=='__main__':main()
