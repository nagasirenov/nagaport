#!/usr/bin/env python3
"""Bounded, byte-reversible CP51 ModItems mapping; no other production file is edited."""
import argparse, hashlib, json, pathlib, re, subprocess

BASE = '077492a6c31c910f34697783f1aa0cfabd8ff88e'
REL = 'ntm-port-1.12.2/src/main/java/com/hbm/items/ModItems.java'
HERE = pathlib.Path(__file__).resolve().parent
ROOT = HERE.parents[3]
# These override families have additional texture effects and are outside the native base closure.
# The three tooling types inherit ItemCraftingDegradation.setUnlocalizedName; include transitive dispatch.
DEFERRED_SETTERS = {'ItemBlowtorch', 'ItemModMinecart', 'ItemCraftingDegradation', 'ItemBoltgun',
                    'ItemTooling', 'ItemToolingWeapon', 'ItemDefuser'}
TOKEN = re.compile(r'//[^\r\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|[A-Za-z_$][\w$]*|\d+(?:\.\d+)?|[^\s]', re.M)

def encode_manifest(document):
    """Keep the finite record inventories reviewable without 20,000 lines of JSON scaffolding."""
    values=[]
    for key,value in document.items():
        if isinstance(value,list):
            rendered='[\n'+',\n'.join('    '+json.dumps(row) for row in value)+'\n  ]'
        else: rendered=json.dumps(value,indent=2).replace('\n','\n  ')
        values.append('  '+json.dumps(key)+': '+rendered)
    return '{\n'+',\n'.join(values)+'\n}\n'

def tokens(source):
    return [(m.group(), m.start(), m.end()) for m in TOKEN.finditer(source) if not m.group().startswith(('//', '/*'))]

def transform(source):
    ts = tokens(source)
    edits, textures, deferred = [], [], []
    counts = {}
    def edit(start,end,replacement,kind):
        edits.append({'start':start,'end':end,'before':source[start:end],'after':replacement,'kind':kind})
        counts[kind] = counts.get(kind,0)+1
    def statement_start(index):
        # All mapped setters belong to simple assignment RHS chains. Comments/strings are tokens.
        while index >= 0 and ts[index][0] not in [';', '{', '}']:
            index -= 1
        return index+1
    for i,(token,start,end) in enumerate(ts):
        if token == 'setUnlocalizedName' and ts[i-1][0] == '.':
            begin=statement_start(i)
            classes=[ts[j+1][0] for j in range(begin,i) if ts[j][0]=='new']
            if classes and classes[0] in DEFERRED_SETTERS:
                deferred.append({'line':source.count('\n',0,start)+1,'class':classes[0], 'statement':source[ts[begin][1]:ts[i+3][2]]})
                continue
            edit(start,end,'setTranslationKey','ammo_override_setter' if classes and classes[0]=='ItemAmmo' else 'translation_setter')
        elif token == 'getUnlocalizedName' and ts[i-1][0] == '.':
            assert ts[i+1][0]=='(' and ts[i+2][0]==')', 'Only the inherited no-arg getter is mapped'
            edit(start,end,'getTranslationKey','translation_getter')
        elif token == 'setTextureName' and ts[i-1][0] == '.':
            assert ts[i+1][0]=='('
            begin=statement_start(i)
            eq=[j for j in range(begin,i) if ts[j][0]=='=' and ts[j-1][0]!='=' and ts[j+1][0]!='=']
            assert len(eq)==1, source[ts[begin][1]:end]
            assignment=ts[eq[0]-1][0]
            new=next(j for j in range(eq[0]+1,i) if ts[j][0]=='new')
            # Keep an outer cast outside the wrapper, as it was outside the original call.
            prefix=[t[0] for t in ts[eq[0]+1:new]]
            assert not prefix or (prefix[0]=='(' and prefix[-1]==')'), prefix
            depth=1; close=i+2
            while depth:
                if ts[close][0]=='(': depth+=1
                if ts[close][0]==')': depth-=1
                if depth: close+=1
            assert ts[close+1][0]==';', 'CP51 has no chained call after explicit texture assignment'
            argument=source[ts[i+1][2]:ts[close][1]]
            assert re.fullmatch(r'RefStrings\.MODID\s*\+\s*"[^"\r\n]+"(?: \+ MainRegistry\.polaroidID)?',argument),argument
            textures.append({'item':assignment,'class':ts[new+1][0], 'line':source.count('\n',0,start)+1,'argument':argument})
            edit(ts[new][1],ts[new][1],'NativeItemTextureRegistry.texture(','texture_receiver')
            edit(ts[i-1][1],ts[i+1][2],', ','texture_argument')
        elif i>=2 and ts[i-1][0]=='.':
            owner=ts[i-2][0]
            constants={('EnumRarity','common'):'COMMON',('EnumRarity','uncommon'):'UNCOMMON',('EnumRarity','rare'):'RARE',('EnumRarity','epic'):'EPIC',('CreativeTabs','tabCombat'):'COMBAT',('CreativeTabs','tabMisc'):'MISC',('CreativeTabs','tabTools'):'TOOLS',('ToolMaterial','EMERALD'):'DIAMOND',('Blocks','farmland'):'FARMLAND',('Items','glass_bottle'):'GLASS_BOTTLE'}
            if (owner,token) in constants: edit(start,end,constants[owner,token],owner+'_'+token)
    marker='import com.hbm.registry.GameRegistry;'
    at=source.index(marker)+len(marker)
    # The CP51 import uses LF. Do not rewrite any existing CRLF/LF terminator.
    edit(at,at,'\nimport com.hbm.render.item.NativeItemTextureRegistry;','texture_import')
    ordered=sorted(edits,key=lambda e:(e['start'],e['end']))
    assert all(a['end']<=b['start'] for a,b in zip(ordered,ordered[1:])), 'Overlapping changes'
    result=source
    for e in reversed(ordered): result=result[:e['start']]+e['after']+result[e['end']:]
    offset=0
    for e in ordered:
        e['output_start']=e['start']+offset
        offset+=len(e['after'])-len(e['before'])
    restored=result
    for e in reversed(ordered):
        a=e['output_start']; assert restored[a:a+len(e['after'])]==e['after']
        restored=restored[:a]+e['before']+restored[a+len(e['after']):]
    assert restored==source, 'Byte reversal failed'
    edit_digest=hashlib.sha256(json.dumps(ordered,separators=(',',':')).encode()).hexdigest()
    return result,{'edit_sha256':edit_digest,'base_commit':BASE,'source':REL,'base_sha256':hashlib.sha256(source.encode()).hexdigest(),'output_sha256':hashlib.sha256(result.encode()).hexdigest(),'counts':counts,'deferred_setters':deferred,'textures':textures}

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--apply',action='store_true');a=parser.parse_args()
    baseline=subprocess.check_output(['git','show',BASE+':'+REL],cwd=ROOT).decode()
    result,manifest=transform(baseline)
    path=ROOT/REL
    if a.apply: path.write_bytes(result.encode());(HERE/'source-manifest.json').write_text(encode_manifest(manifest))
    assert path.read_bytes()==result.encode(), 'Production ModItems differs from audited mapping'
    assert (HERE/'source-manifest.json').read_text()==encode_manifest(manifest)
    # No original terminator is changed; the helper import adds exactly one LF.
    assert result.count('\r\n')==baseline.count('\r\n')
    assert result.count('\n')==baseline.count('\n')+1
    without_import=result.replace('\nimport com.hbm.render.item.NativeItemTextureRegistry;','',1)
    assert len(without_import.splitlines(keepends=True))==len(baseline.splitlines(keepends=True))
    for old,new in zip(baseline.splitlines(keepends=True),without_import.splitlines(keepends=True)):
        assert re.search(r'\s*$',old).group()==re.search(r'\s*$',new).group(), 'Original trailing whitespace changed'
    print('PASS exact CP51 byte reversal; '+json.dumps(manifest['counts'],sort_keys=True))
    print('PASS',len(manifest['textures']),'explicit texture expressions retained; ',len(manifest['deferred_setters']),'custom override calls deliberately deferred')
if __name__=='__main__':main()
