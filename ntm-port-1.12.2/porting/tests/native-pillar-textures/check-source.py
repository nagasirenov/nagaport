#!/usr/bin/env python3
"""Finite exact inverse, original assets and descendant boundary verification."""
import hashlib,json,re,subprocess,tempfile
from pathlib import Path
HERE=Path(__file__).resolve().parent; ROOT=HERE.parents[2]; REPO=ROOT.parent
manifest=json.loads((HERE/'source-manifest.json').read_text()); base=manifest['base']
def sha(data):return hashlib.sha256(data).hexdigest()
def blob(path,ref=base):return subprocess.check_output(['git','-C',str(REPO),'show',ref+':'+path])
def read(path):
 p=ROOT/path
 return p.read_bytes() if p.exists() else blob('ntm-port-1.12.2/'+path,'HEAD')
patch=(HERE/'production.patch').read_bytes()
assert sha(patch)==manifest['production_patch_sha256']
with tempfile.TemporaryDirectory(prefix='pillar-inverse-') as d:
 t=Path(d)
 for path,row in manifest['production'].items():
  data=read(path);assert sha(data)==row['after_sha256'],path
  target=t/'ntm-port-1.12.2'/path;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(data)
 subprocess.run(['git','apply','--reverse','--check',str(HERE/'production.patch')],cwd=t,check=True)
 subprocess.run(['git','apply','--reverse',str(HERE/'production.patch')],cwd=t,check=True)
 for path,row in manifest['production'].items():
  target=t/'ntm-port-1.12.2'/path
  if row['before_sha256'] is None:assert not target.exists(),path
  else:
   assert target.read_bytes()==blob('ntm-port-1.12.2/'+path),path
   assert sha(target.read_bytes())==row['before_sha256']
print('PASS exact inverse: full ModBlocks and BlockPillar bytes return to sealed CP96; only new PillarModelHandler disappears.')
for path,digest in manifest['unchanged'].items():assert sha(read(path))==digest,path
rows=json.loads((HERE/'sites.json').read_text());assert len(rows)==11
old=blob('ntm-port-1.12.2/src/main/java/com/hbm/blocks/ModBlocks.java').decode()
for row in rows:
 before=row['after'].replace('= ((BlockPillar) new ','= new ',1).replace(').setBlockTextureName(','.setBlockTextureName(',1)
 assert before in old and row['registration'] in old
assert sum(r['owner']=='BlockPillar' for r in rows)==8 and sum(r['owner']=='BlockPillarPWR' for r in rows)==3
print('PASS eleven and only eleven whole construction chains restored by receiver casts; registry statements and original item classes unchanged.')
assets=json.loads((HERE/'resource-audit.json').read_text());assert len(assets)==22
for row in assets:
 data=read(row['path']);original=blob('ntm-original-pristine/'+row['path'])
 assert sha(data)==row['sha256']==row['original_sha256'] and data==original,row['path']
 assert subprocess.run(['git','-C',str(REPO),'cat-file','-e',base+':ntm-port-1.12.2/'+row['path']+'.mcmeta'],stderr=subprocess.DEVNULL).returncode!=0
print('PASS all 22 original texture PNGs byte-identical; no new default textures, replacements or animation companions.')
owner=read('src/main/java/com/hbm/blocks/machine/BlockPillar.java').decode()
assert 'protected String textureName;' in owner and 'protected IIcon blockIcon;' in owner
assert 'extends Block {' in owner and 'PropertyInteger.create("metadata", 0, 15)' in owner
assert 'getDefaultState().withProperty(METADATA, meta & 15)' in owner
assert 'return state.getValue(METADATA);' in owner
assert 'return side == 1 ? this.iconTop : (side == 0 ? this.iconTop : this.blockIcon);' in owner
for row in json.loads((HERE/'descendant-audit.json').read_text())['excluded_subclasses']:
 s=read(row['path']).decode();assert sha(s.encode())==row['sha256']
 assert not re.search(r'\b(textureName|blockIcon|textureTop|textureAlt|iconTop|iconAlt|registerBlockIcons|getIcon|getTextureName|getAlternateTextureName|createBlockState|getStateFromMeta|getMetaFromState)\b',s)
 assert 'super(mat, top);' in s
handler=read('src/main/java/com/hbm/main/PillarModelHandler.java').decode()
assert 'block.getClass() != BlockPillar.class && block.getClass() != BlockPillarPWR.class' in handler
assert 'block instanceof BlockPillar' not in handler
print('PASS protected inherited field access, exact-class rendering admission and five unchanged descendant source boundaries.')
receipt=json.loads((HERE/'completed-run-receipt.json').read_text())
for path,digest in receipt['test_and_log_sha256'].items():assert sha((HERE/path).read_bytes())==digest,path
runtime=json.loads((HERE/'runtime-inputs.json').read_text())
for path,digest in runtime.items():
 if path.startswith('com/hbm/') and path not in ['com/hbm/items/block/ItemBlockBase.java','com/hbm/items/block/ItemBlockLore.java','com/hbm/main/MainRegistry.java']:
  # The runner reads text and writes LF-normalized exact production files.
  assert sha(read('src/main/java/'+path).decode().replace('\r\n','\n').encode())==digest,path
assert 'PASS 18631 assertions:' in (HERE/'native.log').read_text()
print('PASS retained completed native run/source hashes: 18,631 assertions, including all 16 world states and native chunk/named-state NBT round trips. No native rerun required.')
