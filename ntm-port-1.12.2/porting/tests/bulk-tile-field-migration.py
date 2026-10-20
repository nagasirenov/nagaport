#!/usr/bin/env python3
"""Fail-closed native TileEntity field migration and exact inverse. No Git required.
Only native inherited reads in the audited 434-owner closure are candidates.
Default verification checks exact selected file bytes and semantic ownership;
--strict-untouched also checks all other production Java source fingerprints.
This tool does not compile complete production machines or certify gameplay.
"""
import argparse
import collections
import hashlib
import importlib.util
import json
import os
import re
import stat
import sys
import tempfile
from pathlib import Path
from functools import lru_cache

ROOT = Path(__file__).resolve().parents[2]
MANIFEST = ROOT / 'porting/tests/bulk-tile-field-source-manifest.json'
if sys.flags.optimize:
    raise RuntimeError('Run without -O: ownership safety assertions must remain enabled.')

def digest(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()

def require(ok, detail):
    if not ok:
        raise AssertionError(detail)

def build_manifest(root=ROOT, transform=None):
    """Discover only the fixed audited scope; construct a manifest in memory.
    transform is an audit-only newer-layer inverse, never a production shim.
    """
    ROOT = Path(root).resolve()
    s=importlib.util.spec_from_file_location('tile_ownership',ROOT/'porting/tests/check-tile-tick-migration.py');m=importlib.util.module_from_spec(s);s.loader.exec_module(m)
    texts,classes,_=m.scan(transform=transform);by=collections.defaultdict(list)
    for c in classes:by[c['path']].append(c)
    assert sum(c['tile'] for c in classes)==434
    ACTIVE={'TileEntityFurnaceBrick','TileEntityFurnaceIron','TileEntityFurnaceSteel'}
    WORLD_LOCAL={'TileEntityTesla','TileEntityFusionKlystron','TileEntityCrateBase'}
    COORD_LOCAL={'TileEntityMultiblock','TileEntityReactorControl','TileEntityFusionKlystron'}
    POS_SHADOW={'TileEntityRBMKRod','TileEntityRBMKRodReaSim'}
    D=lambda x:hashlib.sha256(x.encode('utf-8')).hexdigest()
    edits=collections.defaultdict(list);counts=collections.Counter();excluded=collections.Counter();nested=set();anonymous_hits=[];newtexts=dict(texts);zero_arg_overrides=[];found_shadows=set()
    for rel,text in texts.items():
     clean=m.mask(text);pairs={};stack=[]
     for i,ch in enumerate(clean):
      if ch in '{(':stack.append((ch,i))
      elif ch in '})' and stack:
       typ,start=stack.pop();assert (typ,ch) in (('{','}'),('(',')'));pairs[start]=i
     anonymous=[]
     for hit in re.finditer(r'\bnew\s+[\w.$]+(?:\s*<[^;{}]*?>)?\s*\(',clean):
      end=pairs.get(hit.end()-1)
      if end is not None:
       after=re.match(r'\s*\{',clean[end+1:])
       if after:start=end+after.end();anonymous.append((start,pairs[start]))
     def own(at):
      choices=[c for c in by[rel] if c['head_end']<at<c['end']]
      return min(choices,key=lambda c:c['end']-c['start']) if choices else None
     for c in by[rel]:
      if not c['tile']:continue
      depth=paren=0
      for token in re.finditer(r'[{}()]|\b(?:worldObj|world|pos|xCoord|yCoord|zCoord)\b',clean[c['head_end']+1:c['end']]):
       word=token[0]
       if word=='{':depth+=1
       elif word=='}':depth-=1
       elif word=='(':paren+=1
       elif word==')':paren-=1
       elif depth==0 and paren==0:
        assert word=='pos' and c['name'] in POS_SHADOW,('Unexpected field shadow',c['key'],word)
        found_shadows.add(c['name'])
     for hit in re.finditer(r'\b(?:public|protected|private)\s+(?:(?:final|static|synchronized)\s+)*[\w.$<>\[\]]+\s+(getPos|getWorld)\s*\(\s*\)\s*\{',clean):
      o=own(hit.start())
      if o and o['tile']:zero_arg_overrides.append({'owner':o['key'],'method':hit[1],'line':text.count('\n',0,hit.start())+1})
     for hit in re.finditer(r'\b(worldObj|xCoord|yCoord|zCoord)\b',clean):
      o=own(hit.start());field=hit[1];prefix=clean[:hit.start()]
      this=re.search(r'\bthis\s*\.\s*$',prefix)
      if this and re.search(r'\.\s*$',prefix[:this.start()]):this=None
      qualifier='this' if this else 'receiver' if re.search(r'\.\s*$',prefix) else 'bare'
      family='world' if field=='worldObj' else 'coords'
      reason=None
      if not o or not o['tile']:reason='outside_named_tile'
      elif qualifier=='receiver':reason='arbitrary_receiver'
      elif any(lo<hit.start()<hi for lo,hi in anonymous):reason='anonymous_receiver';anonymous_hits.append([rel,hit.start(),field])
      elif o['name'] in ACTIVE:reason='active_thermal'
      elif family=='world' and o['name'] in WORLD_LOCAL:reason='world_local_or_parameter_class'
      elif family=='coords' and o['name'] in COORD_LOCAL:reason='coord_local_or_parameter_class'
      elif family=='coords' and any(key.rsplit(':',1)[1] in POS_SHADOW for key in o['ancestors']):reason='pos_shadow_ancestry'
      if reason:excluded[(reason,field,qualifier)]+=1;continue
      start=this.start() if this else hit.start();end=hit.end()
      assert not re.match(r'\s*(?:=(?!=)|[+*/%&|^\-]=|[<>]{2,3}=|\+\+|--)',clean[end:]),('Write',rel,start)
      assert not re.search(r'(?:\+\+|--)\s*$',clean[:start]),('Prefix write',rel,start)
      if qualifier=='bare':
       possible_binding=re.search(r'\b([A-Za-z_$][\w$]*)\s+$',clean[:start])
       assert not possible_binding or possible_binding[1] in ('return','else'),('Possible declaration',rel,start,possible_binding[1])
      new='this.world' if family=='world' else 'this.pos.get'+field[0].upper()+'()'
      edits[rel].append({'before_start':start,'old':text[start:end],'new':new,'field':field,'qualifier':qualifier,'owner':o['key'],'line':text.count('\n',0,start)+1})
      counts[(field,qualifier)]+=1
      if any(c['start']<o['start']<c['end'] for c in by[rel] if c is not o):nested.add(rel)
    assert found_shadows==POS_SHADOW
    assert not anonymous_hits
    assert not [x for x in zero_arg_overrides if x['method']=='getPos']
    manifest={'schema':1,'status':'Exact source migration specification; not proof of full production compilation','rules':{'world':'bare inherited worldObj -> this.world','coords':'bare or this inherited xCoord/yCoord/zCoord -> this.pos.getX()/getY()/getZ()','active_exclusions':sorted(ACTIVE),'world_family_class_exclusions':sorted(WORLD_LOCAL),'coordinate_family_class_exclusions':sorted(COORD_LOCAL),'coordinate_pos_shadow_ancestry_exclusions':sorted(POS_SHADOW),'all_other_receivers':'Excluded, even if likely a TileEntity; no receiver typing assumed.'},'tile_closure':sorted(c['key'] for c in classes if c['tile']),'counts':[{'field':k[0],'qualifier':k[1],'count':v} for k,v in sorted(counts.items())],'excluded_counts':[{'reason':k[0],'field':k[1],'qualifier':k[2],'count':v} for k,v in sorted(excluded.items())],'nested_named_tile_files':sorted(nested),'zero_argument_accessors':zero_arg_overrides,'files':{},'untouched_source_sha256':{rel:D(text) for rel,text in texts.items() if rel not in edits}}
    for rel,records in edits.items():
     text=texts[rel];new=text;delta=0
     for rec in records:rec['after_start']=rec['before_start']+delta;delta+=len(rec['new'])-len(rec['old'])
     for rec in reversed(records):
      start=rec['before_start'];assert new[start:start+len(rec['old'])]==rec['old'];new=new[:start]+rec['new']+new[start+len(rec['old']):]
     restore=new
     for rec in reversed(records):
      start=rec['after_start'];assert restore[start:start+len(rec['new'])]==rec['new'];restore=restore[:start]+rec['old']+restore[start+len(rec['new']):]
     assert restore.encode('utf-8')==text.encode('utf-8')
     newtexts[rel]=new
     manifest['files'][rel]={'before_sha256':D(text),'after_sha256':D(new),'old_count':len(records),'edits':records}
    _,after_classes,_=m.scan(transform=lambda rel,text:newtexts[rel])
    assert {c['key'] for c in classes if c['tile']}=={c['key'] for c in after_classes if c['tile']}
    after_by=collections.defaultdict(list)
    for c in after_classes:after_by[c['path']].append(c)
    for rel,records in edits.items():
     for rec in records:
      choices=[c for c in after_by[rel] if c['head_end']<rec['after_start']<c['end']];o=min(choices,key=lambda c:c['end']-c['start']);assert o['key']==rec['owner'] and o['tile']
    assert sum(counts.values())==10939 and len(edits)==384
    assert sum(v for k,v in counts.items() if k[0]=='worldObj')==2751
    assert sum(v for k,v in counts.items() if k[0]!='worldObj')==8188
    assert len(nested)==38
    manifest['proofs']=['All 434 named TileEntity owners retained after in-memory transformation.','Every selected receiver stays in its original named TileEntity owner.','Every candidate source file reconstructs exactly, including comments, literals and original line endings.','No selected field writes, plain or compound assignment, prefix or postfix increment/decrement.','No selected anonymous-class, nested non-tile, qualified outer-this, or arbitrary receiver tokens.','No world/worldObj/xCoord/yCoord/zCoord field shadows in closure; pos-shadow ancestry excluded.','No selected bare old-name declaration candidates; all local/parameter hazard classes excluded per field family.']
    return manifest

@lru_cache(maxsize=4)
def read_manifest(path=None):
    """Cached immutable specification per process. Callers must not mutate it.
    Capture refuses overwrite; explicit manifest arguments bypass loading entirely.
    A developer intentionally changing a manifest in-process must call cache_clear().
    """
    return json.loads(Path(path or MANIFEST).read_text(encoding='utf-8'))

def _reverse_record(relative, text, record):
    require(digest(text) == record['after_sha256'], (relative, 'native whole-file drift'))
    require(len(record['edits']) == record['old_count'], (relative, 'manifest count drift'))
    for edit in reversed(record['edits']):
        start = edit['after_start']; new = edit['new']
        require(text[start:start + len(new)] == new, (relative, start, 'native token drift'))
        text = text[:start] + edit['old'] + text[start + len(new):]
    require(digest(text) == record['before_sha256'], (relative, 'inverse baseline drift'))
    return text

def reverse_source(relative, text, manifest=None):
    """Exact newest-first inverse for parent history guards; unrelated paths pass through."""
    spec = manifest if manifest is not None else read_manifest()
    record = spec['files'].get(relative)
    return text if record is None else _reverse_record(relative, text, record)

def _apply_record(relative, text, record):
    require(digest(text) == record['before_sha256'], (relative, 'baseline whole-file drift'))
    require(len(record['edits']) == record['old_count'], (relative, 'manifest count drift'))
    for edit in reversed(record['edits']):
        start = edit['before_start']; old = edit['old']
        require(text[start:start + len(old)] == old, (relative, start, 'baseline token drift'))
        text = text[:start] + edit['new'] + text[start + len(old):]
    require(digest(text) == record['after_sha256'], (relative, 'native output drift'))
    require(_reverse_record(relative, text, record) is not None, 'inverse unavailable')
    return text

def _compare_discovery(actual, expected):
    for key in ('rules', 'tile_closure', 'counts', 'excluded_counts', 'nested_named_tile_files', 'zero_argument_accessors', 'files'):
        require(actual[key] == expected[key], ('migration ownership/scope drift', key))

def verify(transform=None, root=ROOT, manifest=None, strict_untouched=False, verbose=True):
    """Verify applied source, optionally after an exact inverse of newer migrations."""
    root = Path(root).resolve(); spec = manifest if manifest is not None else read_manifest()
    views = {}
    for path in sorted((root / 'src/main/java').rglob('*.java')):
        relative = path.relative_to(root).as_posix()
        text = path.read_bytes().decode('utf-8')
        if transform is not None:
            text = transform(relative, text)
        views[relative] = text
    historical = {relative: reverse_source(relative, text, spec) for relative, text in views.items()}
    actual = build_manifest(root, transform=lambda relative, text: historical[relative])
    _compare_discovery(actual, spec)
    if strict_untouched:
        require(set(views) == set(spec['files']) | set(spec['untouched_source_sha256']), 'source file set drift')
        for relative, expected in spec['untouched_source_sha256'].items():
            require(digest(views[relative]) == expected, (relative, 'untouched source drift'))
    if verbose:
        print('PASS: 10,939 exact read-only native field tokens / 384 files; 434 tile owners; 38 nested named-owner files.')
        print('PASS: full selected-file inverse, owner/count/shadow/write/anonymous and excluded receiver guards.')
        print('SOURCE PARITY ONLY: not complete machine compilation, gameplay, saves or network acceptance.')
    return spec

def _atomic_bytes(path, data):
    # Preserve permissions and CRLF. Stage on the same filesystem for atomic replace.
    path = Path(path); fd, temporary = tempfile.mkstemp(prefix=path.name + '.', suffix='.bulk-field-tmp', dir=str(path.parent))
    try:
        with os.fdopen(fd, 'wb') as stream:
            stream.write(data); stream.flush(); os.fsync(stream.fileno())
        os.chmod(temporary, stat.S_IMODE(path.stat().st_mode))
        os.replace(temporary, str(path))
    finally:
        if os.path.exists(temporary):
            os.unlink(temporary)

def change_sources(root, spec, reverse=False):
    """Apply or reverse exact recorded states. Resume only recognized per-file states.
    Validate every input and all ownership before writing anything. If a write fails,
    roll back already-written files when their bytes still match this invocation.
    """
    root = Path(root).resolve(); baseline = {}; outputs = {}; originals = {}
    for relative, record in spec['files'].items():
        raw = (root / relative).read_bytes(); text = raw.decode('utf-8'); current = digest(text)
        require(current in (record['before_sha256'], record['after_sha256']), (relative, 'unrecognized current state'))
        old = text if current == record['before_sha256'] else _reverse_record(relative, text, record)
        new = _apply_record(relative, old, record)
        baseline[relative] = old; originals[relative] = raw
        outputs[relative] = (old if reverse else new).encode('utf-8')
    actual = build_manifest(root, transform=lambda relative, text: baseline.get(relative, text))
    _compare_discovery(actual, spec)
    changed = []
    try:
        for relative, output in outputs.items():
            if output == originals[relative]:
                continue
            require((root / relative).read_bytes() == originals[relative], (relative, 'concurrent change before write'))
            _atomic_bytes(root / relative, output); changed.append(relative)
        for relative, output in outputs.items():
            require((root / relative).read_bytes() == output, (relative, 'output changed during migration'))
    except BaseException:
        for relative in reversed(changed):
            if (root / relative).read_bytes() == outputs[relative]:
                _atomic_bytes(root / relative, originals[relative])
        raise
    print(('REVERSED' if reverse else 'APPLIED') + ': ' + str(len(changed)) + ' production files; exact known states only.')
    return len(changed)

def self_test(root=ROOT, manifest=None):
    path = Path(root) / 'porting/tests/bulk-tile-field-source-manifest.json'
    spec = manifest if manifest is not None else read_manifest(path) if path.exists() else build_manifest(root)
    for relative, record in spec['files'].items():
        text = (Path(root) / relative).read_bytes().decode('utf-8')
        if digest(text) == record['after_sha256']:
            text = _reverse_record(relative, text, record)
        native = _apply_record(relative, text, record)
        require(_reverse_record(relative, native, record) == text, (relative, 'round trip'))
        try:
            _reverse_record(relative, native + '\n', record)
        except AssertionError:
            pass
        else:
            raise AssertionError((relative, 'unrelated selected-file drift accepted'))
    # Neither apply nor reverse writes source during this test.
    print('PASS: 384 in-memory full-file round trips and 384 added-byte drift rejections; no production writes.')

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('operation', choices=('dry-run', 'capture', 'apply', 'reverse', 'verify', 'self-test'))
    parser.add_argument('--root', type=Path, default=ROOT)
    parser.add_argument('--manifest', type=Path)
    parser.add_argument('--strict-untouched', action='store_true')
    args = parser.parse_args(); root = args.root.resolve()
    path = args.manifest or root / 'porting/tests/bulk-tile-field-source-manifest.json'
    if args.operation in ('dry-run', 'capture'):
        spec = build_manifest(root)
        print('CANDIDATE: 10,939 tokens / 384 files; 2,751 world / 362 files; 8,188 coordinates / 359 files.')
        if args.operation == 'capture':
            # Deliberately refuse overwrite: capturing cannot silently rebaseline history.
            with path.open('x', encoding='utf-8', newline='\n') as stream:
                json.dump(spec, stream, indent=2); stream.write('\n')
            print('CAPTURED:', path)
    elif args.operation == 'self-test':
        self_test(root, read_manifest(path) if path.exists() else None)
    else:
        spec = read_manifest(path)
        if args.operation == 'verify':
            verify(root=root, manifest=spec, strict_untouched=args.strict_untouched)
        else:
            change_sources(root, spec, reverse=args.operation == 'reverse')

if __name__ == '__main__':
    main()
