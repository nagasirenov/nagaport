#!/usr/bin/env python3
import csv,hashlib,json,re,subprocess
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];REPO=ROOT.parent
BASE='a040cd6b46901a209b8e5db808b980dccc4981c5'
def base(path):return subprocess.check_output(['git','-C',str(REPO),'show',BASE+':ntm-port-1.12.2/'+path])
rows=json.loads((HERE/'sites.json').read_text());assert len(rows)==9
relative='src/main/java/com/hbm/blocks/ModBlocks.java';old=base(relative);new=(ROOT/relative).read_bytes();inverse=new
for row in rows:
 assert old.count(row['before'].encode())==new.count(row['after'].encode())==1
 inverse=inverse.replace(row['after'].encode(),row['before'].encode())
 assert '.setStepSound('+row['sound']+')' in row['before']
 assert row['sound_after']=='(('+row['owner']+') '+row['receiver']+').withSoundType('+row['sound']+')'
 assert re.match(r'^new '+row['owner']+r'\(',row['receiver']) and '{' not in row['receiver']
 name=re.search(r'(\w+) = ',row['before'])[1]
 assert len(re.findall(r'^\s*GameRegistry.registerBlock\('+name+r', '+name+r'\.getTranslationKey\(\)\);',new.decode(),re.M))==1
assert inverse==old,'All nine whole statements invert and preserve previous27/other source byte-for-byte'
for name in ['BlockOutgas','BlockStepEffect']:
 path=f'src/main/java/com/hbm/blocks/generic/{name}.java';s=(ROOT/path).read_text()
 addition='''\n\n    /** Audited construction builder; native protected setter, unchanged receiver identity. */
    public '''+name+''' withSoundType(net.minecraft.block.SoundType sound) {
        setSoundType(sound);
        return this;
    }
'''
 assert s.count(addition)==1 and s.replace(addition,'').encode()==base(path)
 for forbidden in ['setTranslationKey','setCreativeTab','setSoundType']:
  assert not re.search(r'\b(?:public|protected|private)\s+\w+\s+'+forbidden+r'\s*\(',s)
 assert 'extends Block {' in s
for path in ['src/main/java/com/hbm/blocks/PlainCubeTextures.java','src/main/java/com/hbm/main/PlainCubeModelHandler.java','src/main/java/com/hbm/render/model/PlainCubeModel.java','src/main/java/com/hbm/blocks/generic/BlockSmolder.java']:
 assert (ROOT/path).read_bytes()==base(path)
# Preserve the old evidence, not a freshly rebased historical guard.
for f in (HERE.parent/'plain-cube-textures').iterdir():
 assert f.read_bytes()==base(str(f.relative_to(ROOT))),f
before=list(csv.DictReader((HERE/'before.tsv').open(),delimiter='\t'));after=list(csv.DictReader((HERE/'after.tsv').open(),delimiter='\t'))
for row in rows:
 original=row['before'].strip();changed=row['after'].strip()
 b=[r for r in before if r['full_invocation']+'.' in original+'.' or r['full_invocation'] in original]
 # Line membership established independently by javac source positions.
 texture=next(r for r in before if r['method']=='setBlockTextureName' and r['full_invocation'] in original)
 sound=next(r for r in before if r['method']=='setStepSound' and r['line']==texture['line'])
 native=next(r for r in after if r['method']=='bind' and r['line']==texture['line'])
 builder=next(r for r in after if r['method']=='withSoundType' and r['line']==texture['line'])
 assert sound['receiver_source']==row['receiver'] and sound['receiver_type']=='net.minecraft.block.Block' and sound['receiver_kind']=='DECLARED'
 assert sound['arguments_source']==row['sound'] and sound['arguments_type']=='net.minecraft.block.SoundType'
 assert texture['receiver_kind']=='ERROR','Retain honest original poisoned texture attribution'
 assert builder['receiver_source']=='(('+row['owner']+') '+sound['receiver_source']+')'
 assert builder['receiver_type']==builder['method_owner']==builder['return_type']=='com.hbm.blocks.generic.'+row['owner']
 assert builder['method_element_kind']=='METHOD' and builder['method_signature']=='withSoundType(net.minecraft.block.SoundType)'
 assert builder['arguments_source']==sound['arguments_source'] and builder['arguments_type']==sound['arguments_type']
 assert native['method_owner']=='com.hbm.blocks.PlainCubeTextures' and native['arguments_type']=='net.minecraft.block.Block || java.lang.String'
 assert native['arguments_kind']=='DECLARED || DECLARED' and native['return_type']=='net.minecraft.block.Block'
 expected=texture['receiver_source'].replace(sound['full_invocation'],builder['full_invocation'])
 assert native['arguments_source']==expected+' || '+texture['arguments_source'],'Complete receiver preservation after exact native sound replacement'
 # Each actual preceding invocation must return the same audited receiver domain.
 preceding=[r for r in before if r['line']==sound['line'] and r['method'] in ['setTranslationKey','setCreativeTab','noFortune'] and r['full_invocation'] in sound['receiver_source']]
 assert len(preceding)==row['receiver'].count('.setTranslationKey(')+row['receiver'].count('.setCreativeTab(')+row['receiver'].count('.noFortune(')
 first=min(preceding,key=lambda r:len(r['full_invocation']))
 assert first['receiver_source'].startswith('new '+row['owner']+'(') and first['receiver_type']=='com.hbm.blocks.generic.'+row['owner']
 for call in preceding:
  if call['method']=='noFortune':assert call['method_owner']==call['return_type']=='com.hbm.blocks.generic.BlockStepEffect'
  else:assert call['method_owner']==call['return_type']=='net.minecraft.block.Block'
  assert call['method_element_kind']=='METHOD' and call['receiver_kind']=='DECLARED'
for relative,digest in json.loads((HERE/'source-hashes.json').read_text()).items():assert hashlib.sha256((ROOT/relative).read_bytes()).hexdigest()==digest,relative
print('PASS nine exact fresh-owner/cast closures, complete sound receiver/argument attribution, native builder destination/return, all preceding setter executable owners/returns, resolved final Block texture receivers; complete three-file inversion; previous27 production/model/evidence unchanged.')
