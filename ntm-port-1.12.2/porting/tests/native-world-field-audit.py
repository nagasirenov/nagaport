#!/usr/bin/env python3
"""Fail-closed, byte-reversible native TileEntity field migration. No Git dependency.
Only the 148 explicitly owned this.worldObj reads are authorized. No runtime shim.
"""
import hashlib, importlib.util, json, re
from functools import lru_cache
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]
MANIFEST = ROOT / 'porting/tests/native-world-field-manifest.json'

@lru_cache(maxsize=1)
def read_manifest(): return json.loads(MANIFEST.read_text())

def digest(text): return hashlib.sha256(text.encode('utf-8')).hexdigest()

def ownership():
    spec = importlib.util.spec_from_file_location('world_field_tile_ownership', ROOT/'porting/tests/check-tile-tick-migration.py')
    module = importlib.util.module_from_spec(spec); spec.loader.exec_module(module)
    return module

def inspect(texts, classes, mask, spelling):
    hits = {}; excluded = []; anonymous_count = 0
    for rel, text in texts.items():
        clean = mask(text); local = [c for c in classes if c['path'] == rel]
        pairs = {}; stack = []
        for i,ch in enumerate(clean):
            if ch in '{(': stack.append((ch,i))
            elif ch in '})' and stack:
                typ,start = stack.pop(); assert (typ,ch) in (('{','}'),('(',')')), (rel,i)
                pairs[start] = i
        anonymous = []
        for match in re.finditer(r'\bnew\s+[\w.$]+(?:\s*<[^;{}]*?>)?\s*\(',clean):
            end = pairs.get(match.end()-1)
            if end is not None:
                after = re.match(r'\s*\{',clean[end+1:])
                if after:
                    start=end+after.end(); anonymous.append((start,pairs[start]))
        for c in local:
            if not c['tile']: continue
            # Reject any world/worldObj identifier at class field depth, excluding
            # method parameters. No such field or initializer exists in this closure.
            depth=paren=0
            for token in re.finditer(r'[{}()]|\b(?:worldObj|world)\b',clean[c['head_end']+1:c['end']]):
                word=token[0]
                if word=='{': depth+=1
                elif word=='}': depth-=1
                elif word=='(': paren+=1
                elif word==')': paren-=1
                elif depth==0 and paren==0: raise AssertionError(('Possible field shadow',c['key'],word))
        for match in re.finditer(r'\bthis\s*\.\s*'+spelling+r'\b',clean):
            owners=[c for c in local if c['head_end']<match.start()<c['end']]
            owner=min(owners,key=lambda c:c['end']-c['start']) if owners else None
            in_anonymous=any(a<match.start()<b for a,b in anonymous)
            if owner and owner['tile'] and not in_anonymous:
                assert not re.match(r'\s*(?:=(?!=)|\+=|-=|\+\+|--)',clean[match.end():]), ('Not a read',rel,match.start())
                hits.setdefault(rel,[]).append(dict(start=match.start(),end=match.end(),owner=owner['key'],line=text.count('\n',0,match.start())+1))
            else:
                anonymous_count += bool(owner and owner['tile'] and in_anonymous)
                excluded.append([rel,match.start(),match[0],owner['key'] if owner else None])
    return hits,excluded,anonymous_count

def legacy_text(rel, text):
    record=read_manifest()['files'].get(rel)
    if record is None: return text
    assert digest(text)==record['after_sha256'], (rel,'Post-migration file drift')
    for hit in reversed(record['reads']):
        start=hit['after_start'];end=start+len('this.world')
        assert text[start:end]=='this.world',(rel,start,'Expected exact owned receiver')
        text=text[:start]+'this.worldObj'+text[end:]
    assert digest(text)==record['before_sha256'],(rel,'Non-reversible mutation')
    return text

def receiver_line(text,pos):
    end=text.find('\n',pos)
    return text[text.rfind('\n',0,pos)+1:end if end>=0 else len(text)].strip()

def verify_bridge(transform=None):
    """Reusable tick bridge: preserve owned/excluded receiver lines and ownership,
    while allowing unrelated edits. Full byte snapshots remain verify()'s job.
    Returns historical spelling solely for source payload comparisons.
    """
    manifest=read_manifest();scanner=ownership();texts,classes,_=scanner.scan(transform=transform)
    assert sum(c['tile'] for c in classes)==434
    hits,_,anonymous=inspect(texts,classes,scanner.mask,'world')
    assert set(hits)==set(manifest['files']) and anonymous==0,'Native receiver file/anonymous scope drift'
    old_hits,excluded,_=inspect(texts,classes,scanner.mask,'worldObj')
    assert not old_hits,'Legacy owned worldObj reintroduced'
    assert [[rel,owner,receiver_line(texts[rel],pos)] for rel,pos,token,owner in excluded]==manifest['excluded_lines'],'Excluded receiver semantics changed'
    restored=dict(texts)
    for rel,reads in hits.items():
        expected=manifest['files'][rel]['reads']
        assert len(reads)==len(expected),(rel,'Receiver count changed')
        for hit,old in zip(reads,expected):
            assert hit['owner']==old['owner'],(rel,'Receiver owner changed')
            assert receiver_line(texts[rel],hit['start'])==old['native_line'],(rel,'Receiver line semantics changed')
        for hit in reversed(reads):
            start=hit['start'];end=hit['end']
            assert restored[rel][start:end]=='this.world'
            restored[rel]=restored[rel][:start]+'this.worldObj'+restored[rel][end:]
    return restored

def verify(verbose=True):
    manifest=read_manifest(); scanner=ownership();texts,classes,_=scanner.scan()
    assert sum(c['tile'] for c in classes)==434
    restored={rel:legacy_text(rel,text) for rel,text in texts.items()}
    # Ownership must be scanned in the same historical character coordinates.
    old_texts,old_classes,_=scanner.scan(transform=legacy_text)
    hits,excluded,anonymous=inspect(old_texts,old_classes,scanner.mask,'worldObj')
    assert hits=={rel:entry['before_reads'] for rel,entry in manifest['files'].items()},'Owned receiver set/ownership drift'
    assert excluded==manifest['excluded_receivers'],'Excluded receiver changed'
    assert anonymous==0
    assert len(hits)==92 and sum(map(len,hits.values()))==148
    # Includes all comments, literals and untouched files, without needing Git.
    for rel,expected in manifest['untouched_sha256'].items():
        assert digest(texts[rel])==expected,(rel,'Excluded source file drift')
    assert set(texts)==set(manifest['files'])|set(manifest['untouched_sha256']),'Java source set drift'
    if verbose:
        print('PASS: 148 exact inherited reads / 92 files; 434-tile closure, field-shadow and anonymous ownership checks')
        print('PASS: byte-reversible migration; all 560 excluded receivers, comments, literals, line endings and unrelated Java files unchanged')
        print('SOURCE PARITY ONLY: not full production tile compilation or gameplay')
    return manifest

if __name__=='__main__': verify()
