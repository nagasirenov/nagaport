#!/usr/bin/env python3
import hashlib,json,pathlib
H=pathlib.Path(__file__).resolve().parent
ROOT=H.parents[2]
data=json.loads((H/'manifest.json').read_text())
def digest(raw): return hashlib.sha256(raw).hexdigest()
def reverse(raw, entry):
    assert digest(raw)==entry['after_sha256'], 'after drift'
    for e in reversed(entry['edits']):
        off=e['after_offset'];new=e['new'].encode();old=e['old'].encode()
        assert raw[off:off+len(new)]==new
        raw=raw[:off]+old+raw[off+len(new):]
    assert digest(raw)==entry['before_sha256'], 'before drift'
    return raw
def verify(root=ROOT):
    assert len(data['files'])==29 and len(data['callers'])==31 and len(data['owners'])==15 and len(data['already_native'])==7
    rejected=0
    for rel, entry in data['files'].items():
        raw=(pathlib.Path(root)/rel).read_bytes();before=reverse(raw,entry);forward=before
        for e in reversed(entry['edits']):
            off=e['before_offset'];old=e['old'].encode();new=e['new'].encode()
            assert forward[off:off+len(old)]==old
            forward=forward[:off]+new+forward[off+len(old):]
        assert forward==raw
        for mutant in (raw+b'\n',raw[:-1],raw.replace(b'\r\n',b'\n') if b'\r\n' in raw else raw.replace(b'\n',b'\r\n')):
            assert mutant!=raw
            try: reverse(mutant,entry)
            except AssertionError: rejected+=1
            else: raise AssertionError('mutation accepted '+rel)
    print('PASS 29 byte-exact inverses and round trips; 31 placement calls and 15 native owner signatures with bounded body hooks; %d rejected mutations' % rejected)
if __name__=='__main__': verify()
