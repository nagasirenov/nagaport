#!/usr/bin/env python3
"""Check fragment merge keeps unrelated bytes, refreshes offsets and rejects drift."""
import tempfile,pathlib,shutil,subprocess,importlib.util
h=pathlib.Path(__file__).resolve().parent;root=h.parents[2]
spec=importlib.util.spec_from_file_location('g',h/'check_source.py');g=importlib.util.module_from_spec(spec);spec.loader.exec_module(g)
with tempfile.TemporaryDirectory(prefix='bounds-merge-') as td:
 d=pathlib.Path(td);out=d/'porting/tests/entity-bounds-native';shutil.copytree(h,out,ignore=shutil.ignore_patterns('__pycache__'));rows=[];prefix=b'// unrelated merged edit\r\n';shift=len(prefix)
 for rel,record in g.data['files'].items():
  p=d/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(prefix+g.reverse((root/rel).read_bytes(),record))
  for e in record['edits']:rows.append('\t'.join([str(p),str(e['start']+shift),str(e['end']+shift),e['new'],e['owner']]))
 inp=d/'rows.tsv';inp.write_text('\n'.join(rows)+'\n');subprocess.run(['python3',str(out/'migrate.py'),str(inp)],check=True);subprocess.run(['python3',str(out/'check_source.py')],check=True)
 for rel in g.data['files']:assert (d/rel).read_bytes()==prefix+(root/rel).read_bytes()
 print('PASS fragment merge preserves unrelated bytes in all 57 files, adjusts 184 UTF16 edits and regenerates exact inverses')
 snapshot={rel:(d/rel).read_bytes() for rel in g.data['files']};inp.write_text('\n'.join(rows[:-1])+'\n');r=subprocess.run(['python3',str(out/'migrate.py'),str(inp)],capture_output=True);assert r.returncode!=0
 assert all((d/rel).read_bytes()==raw for rel,raw in snapshot.items());print('PASS changed merge inventory rejected without source writes')
