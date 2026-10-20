from pathlib import Path
import json,hashlib
r=Path(__file__).resolve().parents[2];d=json.loads((r/'porting/tests/native-tape-scope.json').read_text());o=r.parent/'ntm-original-pristine'
for key in ['production','unchanged_helpers','unchanged_assets']:
 for f,h in d[key].items():assert hashlib.sha256((r/f).read_bytes()).hexdigest()==h,f
for f,h in d['original'].items():assert hashlib.sha256((o/f).read_bytes()).hexdigest()==h,f
s=(r/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text()
for owner in ['tape_recorder','steel_poles']:
 line=next(x for x in s.splitlines() if '\t\t'+owner+' =' in x)
 assert '.setTranslationKey("'+owner+'")' in line and '.setBlockTextureName(' not in line
 assert 'GameRegistry.registerBlock('+owner+', '+owner+'.getTranslationKey());' in s
assert 'registerBlockHandler(new RenderTapeBlock())' not in (r/'src/main/java/com/hbm/main/ClientProxy.java').read_text()
print('PASS tape/poles: 6 production paths, 8 unchanged helpers, 4 unchanged original assets and 4 original sources; exact native registrations')
