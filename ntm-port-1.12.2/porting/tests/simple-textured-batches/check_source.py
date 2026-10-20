#!/usr/bin/env python3
"""Frozen, reversible POSITION_TEX source layer. No Git dependency at replay."""
import hashlib,json,pathlib,re
HERE=pathlib.Path(__file__).resolve().parent
ROOT=HERE.parents[2]
MANIFEST=json.loads((HERE/'manifest.json').read_text())
FILES=MANIFEST['files']
def digest(s):return hashlib.sha256(s.encode()).hexdigest()
def reverse_source(relative,text):
    if relative not in FILES:return text
    record=FILES[relative]
    assert digest(text)==record['after_sha256'],('changed native source',relative)
    lines=text.splitlines(True)
    for edit in reversed(record['edits']):
        i=edit['new_start']; n=len(edit['new'])
        assert lines[i:i+n]==edit['new']
        lines[i:i+n]=edit['old']
    original=''.join(lines)
    assert digest(original)==record['before_sha256'],('changed reconstructed baseline',relative)
    return original

def split_args(s):
    args=[];start=depth=0
    for i,c in enumerate(s):
        if c in '([':depth+=1
        if c in ')]':depth-=1
        if c==',' and depth==0:args.append(s[start:i].strip());start=i+1
    args.append(s[start:].strip())
    return args

def validate():
    batches=vertices=0
    for relative,record in FILES.items():
        current=(ROOT/relative).read_text();old=reverse_source(relative,current)
        for name in record['names']:
            # The local singleton cannot escape to helper methods or alias fields.
            for line in old.splitlines():
                if re.search(r'\b'+name+r'\b',line):
                    assert re.fullmatch(r'\s*(?:Tessellator '+name+r' = Tessellator.instance;|'+name+r'\.(?:startDrawingQuads|addVertexWithUV|draw)\(.*\);)\s*',line),line
            before=re.findall(name+r'\.startDrawingQuads\(\);(.*?)'+name+r'\.draw\(\);',old,re.S)
            after=re.findall(name+r'Buffer.begin\(7, DefaultVertexFormats.POSITION_TEX\);(.*?)'+name+r'\.draw\(\);',current,re.S)
            assert len(before)==len(after)>0
            assert old.count(name+'.draw();')==current.count(name+'.draw();')==len(before)
            assert current.count('BufferBuilder '+name+'Buffer = '+name+'.getBuffer();')==old.count('Tessellator '+name+' = Tessellator.instance;')
            for a,b in zip(before,after):
                av=re.findall(name+r'\.addVertexWithUV\(([^\n]+)\);',a)
                bv=re.findall(name+r'Buffer.pos\(([^\n]+)\)\.tex\(([^\n]+)\)\.endVertex\(\);',b)
                assert len(av)==len(bv)>0 and len(av)%4==0
                for x,(pos,uv) in zip(av,bv):assert split_args(x)==split_args(pos)+split_args(uv)
                # Within each batch only vertex emissions, whitespace, or the two
                # audited Stard ar local integer constants exist. No nested draw.
                rest=re.sub(name+r'\.addVertexWithUV\([^\n]+\);','',a)
                rest=rest.replace('int offsetX = 85;','').replace('int offsetY = 60;','')
                assert not rest.strip(),(relative,rest)
                batches+=1;vertices+=len(av)
        # Every change is limited to imports, local native buffer acquisition,
        # begin and vertex calls. Exact reversal pins all GL, formulas and order.
        expected=old.replace('import net.minecraft.client.renderer.Tessellator;', 'import net.minecraft.client.renderer.Tessellator;\nimport net.minecraft.client.renderer.BufferBuilder;\nimport net.minecraft.client.renderer.vertex.DefaultVertexFormats;')
        for name in record['names']:
            expected=re.sub(r'(?m)^([\t ]*)Tessellator '+name+r' = Tessellator.instance;',lambda m:m[1]+'Tessellator '+name+' = Tessellator.getInstance();\n'+m[1]+'BufferBuilder '+name+'Buffer = '+name+'.getBuffer();',expected)
            expected=expected.replace(name+'.startDrawingQuads();',name+'Buffer.begin(7, DefaultVertexFormats.POSITION_TEX);')
            def vertex(m):
                # Preserve argument spelling/spacing other than the split boundary.
                raw=m[1];args=split_args(raw);depth=0;commas=[]
                for i,c in enumerate(raw):
                    if c in '([':depth+=1
                    if c in ')]':depth-=1
                    if c==',' and depth==0:commas.append(i)
                return name+'Buffer.pos('+raw[:commas[2]]+').tex('+', '.join(args[3:])+').endVertex();'
            expected=re.sub(name+r'\.addVertexWithUV\(([^\n]+)\);',vertex,expected)
        assert expected==current,('nonmechanical change',relative)
        for target,replacement in [('POSITION_TEX','POSITION_COLOR'),('.endVertex();',';'),('.draw();','.getBuffer();'),('.tex(','.color('),('begin(7,','begin(4,')]:
            mutated=current.replace(target,replacement,1)
            assert mutated!=current
            try:reverse_source(relative,mutated)
            except AssertionError:pass
            else:raise AssertionError('mutation accepted')
    assert batches==22 and vertices==108
    assert reverse_source('unrelated.java','untouched')=='untouched'
    print('PASS 15 exact source reversals, 22 complete owned POSITION_TEX batches, 108 vertex expressions, 75 rejected mutations',flush=True)
if __name__=='__main__':validate()
