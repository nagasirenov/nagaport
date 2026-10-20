import collections,csv,gzip,json
from pathlib import Path
E=Path(__file__).resolve().parent
rows=json.loads((E/'sites.json').read_text());expected=collections.Counter({(r['path'],'compiler.err.method.does.not.override.superclass','method does not override or implement a method from a supertype'):1 for r in rows})
def errors(n):
 p=E/n/'errors.tsv';s=p.read_text() if p.exists() else gzip.decompress(p.with_suffix('.tsv.gz').read_bytes()).decode();return collections.Counter((r[0],r[2],r[3]) for r in csv.reader(s.splitlines(),delimiter='\t'))
b,a=errors('before'),errors('attribution');assert b-a==expected;assert not a-b;print('PASS: exactly 62 selected invalid legacy override errors removed; all other 13,904 attribution errors retained (line offsets excluded). Not a successful full build.')
