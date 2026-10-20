#!/usr/bin/env python3
"""CP34 exact reversible particle/crane/registry source boundary; not runtime/GL parity proof."""
import hashlib,json,importlib.util
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
_spec=importlib.util.spec_from_file_location('rtg_items_boundary',ROOT/'porting/tests/check-native-rtg-items-boundary.py')
_next=importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_next)
RAW=Path(__file__).with_name('native-particle-crane-boundary-manifest.json').read_bytes()
assert hashlib.sha256(RAW).hexdigest() == '5d2386d5289cab17029ca96598ec3d4ab57c538c313391e89f3f156636982d2f'
MANIFEST=json.loads(RAW)
assert MANIFEST['baseline']=='b07d39647303bde866af809b678f88b48b562da3'
FILES=MANIFEST['files']; NEW_FILES=MANIFEST['new_files']
assert len(FILES)==40 and len(NEW_FILES)==13

def digest(text): return hashlib.sha256(text.encode('utf-8')).hexdigest()
def reverse_source(relative,text):
    text=_next.reverse_source(relative,text)
    if relative in MANIFEST['deleted_files']:
        assert text=='',(relative,'retired source reappeared')
        return MANIFEST['deleted_files'][relative]['original']
    entry=FILES.get(relative)
    if entry is None:
        if relative in NEW_FILES: assert digest(text)==NEW_FILES[relative],(relative,'new native source drift')
        return text
    assert digest(text)==entry['after_sha256'],(relative,'native source drift')
    for edit in reversed(entry['edits']):
        off=edit['after_offset']; new=edit['new']
        assert text[off:off+len(new)]==new
        text=text[:off]+edit['old']+text[off+len(new):]
    assert digest(text)==entry['before_sha256'],(relative,'baseline drift')
    return text

def verify(root=ROOT,verbose=True):
    for relative,entry in MANIFEST['deleted_files'].items():
        assert not (Path(root)/relative).exists(),(relative,'retired source reappeared')
        assert digest(entry['original'])==entry['before_sha256']
    for relative,entry in MANIFEST['binary_files'].items():
        path=Path(root)/relative
        if entry['after_sha256'] is None: assert not path.exists(),relative
        else: assert hashlib.sha256(path.read_bytes()).hexdigest()==entry['after_sha256'],relative
    _next.verify(root,verbose)
    rejected=0
    for relative in sorted(set(FILES)|set(NEW_FILES)):
        current=(Path(root)/relative).read_bytes().decode('utf-8')
        before=reverse_source(relative,current)
        if relative in FILES:
            forward=before
            for edit in reversed(FILES[relative]['edits']):
                off=edit['before_offset']; old=edit['old']
                assert forward[off:off+len(old)]==old
                forward=forward[:off]+edit['new']+forward[off+len(old):]
            assert forward==_next.reverse_source(relative,current)
        for mutant in (current+'\n',current[:-1],current.replace('\r\n','\n') if '\r\n' in current else current.replace('\n','\r\n')):
            assert mutant!=current
            try: reverse_source(relative,mutant)
            except AssertionError: rejected+=1
            else: raise AssertionError((relative,'drift accepted'))
    assert reverse_source('unrelated.java','\ufeffuntouched\r\n')=='\ufeffuntouched\r\n'
    if verbose: print('PASS Particle/crane/registry source: 40 byte-exact inverses/round trips, 13 new, 0 retired sources, 4 binary paths, %d rejected mutations. Not runtime/GL proof.' % rejected)
    return MANIFEST
if __name__=='__main__':
    import sys
    assert not sys.flags.optimize
    verify()
