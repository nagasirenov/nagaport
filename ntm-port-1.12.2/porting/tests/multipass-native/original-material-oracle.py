#!/usr/bin/env python3
"""Execute original multipass material methods against the real 1.7.10 API.
No platform stubs, production edits, network or full-mod build. NTM-only tile
collaborators provide fields, not gameplay implementations. See original-audit.md.
"""
import argparse, hashlib, json, os, re, subprocess, tempfile, zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent
FAMILIES=['BlockBedrockOreTE','BlockFissure','BlockOre','BlockSellafieldOre','BlockCableGauge','BlockCablePaintable','BlockOpenComputersCablePaintable','CraneRouter','FluidDuctGauge','FluidDuctPaintable','FluidDuctPaintableBlockExhaust','PneumoTubePaintableBlock']
METHODS=['registerBlockIcons','getIcon','colorMultiplier','shouldSideBeRendered','getPasses','shouldRenderItemMulti']
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def methods(s):
 out=[]
 for m in re.finditer(r'(?m)^\s*public [^\n]+\b(?:'+ '|'.join(METHODS)+r')\(',s):
  start=m.start(); a=s.index('{',start);i=a+1;d=1
  while d:
   d+=(s[i]=='{')-(s[i]=='}');i+=1
  out.append(s[start:i])
 return '\n'.join(out)
def map_api(s):
 for old,new in [('blockIcon','L'),('getTextureName()','N()'),('IIconRegister','rg'),('IBlockAccess','ahl'),('IIcon','rf'),('TileEntity tile','aor tile'),('TileEntity te','aor te'),('RefStrings.MODID','"hbm"'),('.registerIcon(','.a('),('.getTileEntity(','.o('),('.getBlockMetadata(','.e('),('Blocks.bedrock.getIcon(0, 0)','BEDROCK.a(0, 0)'),('RenderBlockMultipass.currentPass','currentPass'),('SolarSystem.Body.values().length','STONES.length'),('SolarSystem.Body body = SolarSystem.Body.values()[i]','String body = STONES[i]'),('body.getStoneTexture().toString()','body')]:s=s.replace(old,new)
 return s

def generate(original):
 blocks=original/'src/main/java/com/hbm/blocks'
 paths={p.stem:p for p in blocks.rglob('*.java')}
 generated=''
 for name in ['BlockSellafieldSlaked']+FAMILIES:
  parent='BlockSellafieldSlaked' if name=='BlockSellafieldOre' else 'OriginalBase'
  generated+='static class '+name+' extends '+parent+' {\n'+map_api(methods(paths[name].read_text()))+'\n}\n'
 return (HERE/'original-material-oracle.java.in').read_text().replace('/*SOURCE_EXTRACTED_CLASSES*/',generated)

def audit(original, legacy, srg, patches):
 source=original/'src/main/java'
 paths={p.stem:p for p in (source/'com/hbm/blocks').rglob('*.java')}
 actual_direct={name for name,path in paths.items() if re.search(r'implements [^{]*IBlockMultiPass',path.read_text())}
 assert actual_direct==set(FAMILIES),'Original direct implementer discovery drift'
 inherited={}
 for name,path in paths.items():
  m=re.search(r'public class \w+ extends (\w+)',path.read_text())
  if m:inherited[name]=m[1]
 accepted=set(FAMILIES)
 while True:
  more={c for c,b in inherited.items() if b in accepted}-accepted
  if not more:break
  accepted|=more
 mod=(source/'com/hbm/blocks/ModBlocks.java').read_text()
 owners=[]
 for m in re.finditer(r'(?m)^\s*(\w+)\s*=\s*new (\w+)\([^;]+;',mod):
  name,kind=m.group(1,2)
  if kind not in accepted:continue
  text=m.group(0).strip();tx=re.search(r'setBlockTextureName\(RefStrings.MODID \+ "([^"]+)"\)',text)
  regs=[]
  for line in mod.splitlines():
   if re.search(r'(?:GameRegistry\.registerBlock|register)\('+re.escape(name)+r'(?:,|\))',line):regs.append(line.strip())
  owners.append({'owner':name,'class':kind,'family':'BlockOre' if kind in {'BlockOreOutgas','BlockOreFluid','BlockCluster'} else kind,'texture':'hbm'+tx.group(1) if tx else None,'construction':text,'registration':regs,'optional':'OpenComputers' if name=='oc_cable_paintable' else None})
 original_sources={}
 for name in sorted(accepted|{'BlockSellafieldSlaked','ModBlocks','IBlockMultiPass','FluidDuctBase'}):
  p=paths[name];original_sources[p.relative_to(original).as_posix()]=sha(p)
 for rel in ['src/main/java/com/hbm/render/block/RenderBlockMultipass.java','src/main/java/com/hbm/dim/SolarSystem.java','src/main/java/com/hbm/dim/CelestialBody.java']:
  original_sources[rel]=sha(original/rel)
 materials=set(o['texture'] for o in owners if o['texture'])
 for name in FAMILIES+['BlockSellafieldSlaked']:
  for t in re.findall(r'(?:reg|iconRegister)\.registerIcon\(RefStrings.MODID \+ ":([^"]+)"\)',paths[name].read_text()):materials.add('hbm:'+t)
 materials|={'hbm:crane_in'}|{'hbm:ore_random_'+str(i) for i in range(1,11)}
 solar=(source/'com/hbm/dim/SolarSystem.java').read_text()
 materials|={'hbm:'+x for x in re.findall(r'withBlockTextures\(RefStrings.MODID \+ ":textures/blocks/([^".]+)\.png"',solar)}
 resources={}
 for material in sorted(materials):
  p=original/'src/main/resources/assets/hbm/textures/blocks'/ (material.split(':')[1]+'.png')
  assert p.is_file(),str(p)
  resources[p.relative_to(original).as_posix()]=sha(p)
 return {'schema':1,'direct_families':FAMILIES,'inherited_classes':sorted(accepted-set(FAMILIES)),'owners':owners,'original_sources':original_sources,'legacy_artifacts':{'client':sha(legacy),'srg':sha(srg),'forge_patches':sha(patches)},'resources':resources,'planet_base_order':['minecraft:stone','minecraft:stone','hbm:moon_rock','hbm:minmus_stone','hbm:duna_rock','hbm:moho_stone','hbm:dresbase','hbm:eve_stone_2','hbm:ike_stone','minecraft:stone','hbm:basalt']}

def verify_damage_bytecode(a):
 def disassemble(name):
  return subprocess.check_output([str(a.java_home/'bin/javap'),'-c','-p','-classpath',str(a.legacy_client),name],text=True)
 def section(source,signature):
  begin=source.index(signature)
  end=re.search(r'\n  (?:public|private|protected|static) ',source[begin+len(signature):])
  return source[begin:begin+len(signature)+end.start()] if end else source[begin:]
 glob=section(disassemble('bma'),'  public void a(bmh, yz, float);')
 assert glob.index('Method bmh.b:()V')<glob.index('Method bmh.c:()V')<glob.index('Method blm.a:(Laji;IIILrf;)V'),'Original damage noColor order'
 tess=disassemble('bmh')
 sections=[section(tess,sig) for sig in ['  public void a(int);','  public void c();','  public void a(int, int, int, int);','  public void a(double, double, double);','  public int a();']]
 start,disable,setter,vertex,draw=sections
 assert re.search(r'iconst_0\s+\d+: putfield[^\n]+Field l:Z',start)
 assert re.search(r'iconst_0\s+\d+: putfield[^\n]+Field r:Z',start)
 assert re.search(r'iconst_1\s+\d+: putfield[^\n]+Field r:Z',disable)
 assert re.search(r'Field r:Z\s+\d+: ifeq[^\n]+\s+\d+: return',setter)
 assert re.search(r'Field l:Z\s+\d+: ifeq',vertex)
 assert re.search(r'Field l:Z\s+\d+: ifeq',draw) and 'GL11.glColorPointer' in draw
 with zipfile.ZipFile(a.forge_patches) as z:
  globpatch=z.read('net/minecraft/client/renderer/RenderGlobal.java.patch').decode()
  tesspatch=z.read('net/minecraft/client/renderer/Tessellator.java.patch').decode()
 assert '+    public void drawBlockDamageTexture' in globpatch
 changed='\n'.join(line for line in (globpatch+'\n'+tesspatch).splitlines() if line[:1] in '+-' and not line.startswith(('+++','---')))
 for unchanged in ['func_78383_c','field_78410_t','field_78399_n','glColorPointer','GL_COLOR_ARRAY']:
  assert unchanged not in changed,'Forge patch changes noColor evidence: '+unchanged
 if a.damage_evidence:
  content='Official original client SHA256 '+sha(a.legacy_client)+'\nOfficial Forge1614 patch archive SHA256 '+sha(a.forge_patches)+'\n\n'
  content+='Mappings checked against pinned SRG: bma=RenderGlobal; bmh=Tessellator; bma.a(bmh,yz,float)=drawBlockDamageTexture; bmh.b()=startDrawingQuads; bmh.c()=disableColor; bmh.l=hasColor; bmh.r=isColorDisabled.\n\n'
  content+='The damage method starts drawing, disables color, then invokes the override-texture renderer. Color setters cannot enable hasColor when disabled. Vertex writes and GL_COLOR_ARRAY setup both depend on hasColor. Brightness remains independent.\n\n'
  content+=glob+'\n\n'+'\n\n'.join(sections)+'\n\nFORGE1614 RENDERGLOBAL PATCH\n'+globpatch+'\nFORGE1614 TESSELLATOR PATCH\n'+tesspatch
  a.damage_evidence.write_text(content)
 print('PASS pinned old RenderGlobal start/noColor/render ordering and Tessellator color-disabled bytecode; Forge1614 patches preserve relevant color gates',flush=True)


def main():
 p=argparse.ArgumentParser(description=__doc__)
 p.add_argument('--original-root',type=Path,required=True);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True)
 p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--legacy-srg',type=Path,required=True);p.add_argument('--forge-patches',type=Path,required=True)
 p.add_argument('--damage-evidence',type=Path);p.add_argument('--compare-native',action='store_true');p.add_argument('--freeze',action='store_true');p.add_argument('--output',type=Path);p.add_argument('--generated-java',type=Path)
 a=p.parse_args();result=audit(a.original_root,a.legacy_client,a.legacy_srg,a.forge_patches);manifest=HERE/'original-manifest.json'
 if a.freeze:
  assert not manifest.exists(),'Refuse to overwrite pinned evidence'
  manifest.write_text(json.dumps(result,indent=2)+'\n')
 assert result==json.loads(manifest.read_text()),'Original inputs or owner/resources drifted'
 with zipfile.ZipFile(a.legacy_srg) as z: srg=z.read('joined.srg').decode()
 for mapping in ['CL: aji net/minecraft/block/Block','CL: aor net/minecraft/tileentity/TileEntity','CL: ahl net/minecraft/world/IBlockAccess','CL: rf net/minecraft/util/IIcon','CL: rg net/minecraft/client/renderer/texture/IIconRegister','CL: bma net/minecraft/client/renderer/RenderGlobal','CL: bmh net/minecraft/client/renderer/Tessellator','MD: bma/a (Lbmh;Lyz;F)V net/minecraft/client/renderer/RenderGlobal/func_72717_a','MD: bmh/c ()V net/minecraft/client/renderer/Tessellator/func_78383_c','FD: bmh/l net/minecraft/client/renderer/Tessellator/field_78399_n','FD: bmh/r net/minecraft/client/renderer/Tessellator/field_78410_t','MD: aji/a (II)Lrf; net/minecraft/block/Block/func_149691_a','MD: aji/e (Lahl;IIII)Lrf; net/minecraft/block/Block/func_149673_e','MD: aji/d (Lahl;III)I net/minecraft/block/Block/func_149720_d']:
  assert mapping in srg,mapping
 verify_damage_bytecode(a)
 java=generate(a.original_root)
 if a.generated_java:a.generated_java.write_text(java)
 cp=str(a.legacy_client)+os.pathsep+a.classpath
 with tempfile.TemporaryDirectory(prefix='multipass-original-') as td:
  work=Path(td);j=work/'OriginalMultipassMaterials.java';j.write_text(java)
  # The official JAR signs the default package; source-extracted test classes
  # must share that package to reference obfuscated APIs. Strip only signature
  # metadata in this temporary copy, preserving all original class bytes.
  unsigned=work/'official-client-classbytes.jar'
  with zipfile.ZipFile(a.legacy_client) as src, zipfile.ZipFile(unsigned,'w') as dst:
   for entry in src.infolist():
    if not entry.filename.startswith('META-INF/'):
     dst.writestr(entry,src.read(entry.filename))
  cp=str(unsigned)+os.pathsep+a.classpath
  damage=work/'OriginalDamageNoColor.java';damage.write_text((HERE/'original-damage-no-color.java.in').read_text())
  files=[str(j),str(damage)]
  if a.compare_native:
   for rel,want in result['resources'].items():
    asset=HERE.parents[2]/rel
    assert asset.is_file() and sha(asset)==want,'Port texture differs from original: '+rel
   print('PASS all %d original PNGs byte-identical in port'%len(result['resources']),flush=True)
   native=HERE.parents[2]/'src/main/java'
   family_kinds=dict(zip(FAMILIES,['BEDROCK_ORE','FISSURE','ORE','SELLAFIELD','CABLE_GAUGE','CABLE','OC_CABLE','ROUTER','FLUID_GAUGE','FLUID','EXHAUST','PNEUMATIC']))
   fallback={'BlockBedrockOreTE':'minecraft:bedrock','BlockFissure':'minecraft:bedrock','BlockCableGauge':'hbm:deco_red_copper','BlockCablePaintable':'hbm:red_cable_base','BlockOpenComputersCablePaintable':'hbm:oc_cable_base','CraneRouter':'hbm:crane_in','FluidDuctGauge':'hbm:deco_steel','FluidDuctPaintable':'hbm:fluid_duct_paintable','FluidDuctPaintableBlockExhaust':'hbm:fluid_duct_paintable_block_exhaust','PneumoTubePaintableBlock':'hbm:pneumatic_tube_paintable'}
   expected={}
   for owner in result['owners']:
    fqcn=next(rel for rel in result['original_sources'] if rel.endswith('/'+owner['class']+'.java')).replace('src/main/java/','').replace('/','.').removesuffix('.java')
    expected[owner['owner']]=(fqcn,family_kinds[owner['family']],owner['texture'] or fallback[owner['family']])
   actual={}
   for name,fqcn,kind,texture in re.findall(r'add\("([^"]+)","([^"]+)",Kind\.(\w+),"([^"]+)"\)',(native/'com/hbm/main/MultipassModelHandler.java').read_text()):
    assert name not in actual,'Duplicate native profile '+name
    actual[name]=(fqcn,kind,texture if ':' in texture else 'hbm:'+texture)
   assert actual==expected,'Native owner/profile/material-alias table differs from original'
   print('PASS all %d original owner classes, family routes and material aliases exactly match native loader'%len(expected),flush=True)
   test=work/'OriginalNativeComparison.java';test.write_text((HERE/'original-native-comparison.java.in').read_text());files.append(str(test))
   for rel in ['com/hbm/blocks/MultipassContext.java','com/hbm/render/block/multipass/MultipassMaterials.java']:
    source=native/rel;print('NATIVE SOURCE',rel,sha(source),flush=True);files.append(str(source))
  subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',cp,'-d',td]+files,check=True)
  subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',td+os.pathsep+cp,'OriginalDamageNoColor'],cwd=td,check=True)
  process=subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',td+os.pathsep+cp,'OriginalMultipassMaterials'],cwd=td,check=True,text=True,stdout=subprocess.PIPE)
  if a.compare_native:
   rows=work/'materials.tsv';rows.write_text(process.stdout)
   subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',td+os.pathsep+cp,'OriginalNativeComparison',str(rows)],cwd=td,check=True)
  if a.output:a.output.write_text(process.stdout)
  else:print(process.stdout,end='')
 print('PASS pinned original source/API material oracle; %d owners, %d textures; no native model or old/new lighting parity claimed.'%(len(result['owners']),len(result['resources'])),flush=True)
if __name__=='__main__':main()
