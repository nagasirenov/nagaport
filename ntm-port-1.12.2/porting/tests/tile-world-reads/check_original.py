#!/usr/bin/env python3
"""Verify the retained original source evidence, without rewriting its hashes."""
import pathlib,sys,json,hashlib,re
h=pathlib.Path(__file__).resolve().parent;m=json.loads((h/'original-source-audit.json').read_text());root=pathlib.Path(sys.argv[1])
for rel,spec in m.items():
 if not rel.endswith('.java'):continue
 p=root/rel;s=p.read_text();assert hashlib.sha256(p.read_bytes()).hexdigest()==spec['sha256'];assert re.search(r'class\s+\w+\s+extends\s+(\w+)',s)[1]==spec['superclass'];assert not re.search(r'\bWorld\s+worldObj\b',s)
s=(root/'com/hbm/tileentity/machine/TileEntityAirPump.java').read_text();assert m['original_getWorld_body'] in s
inventory=[]
for p in root.rglob('*.java'):
 for _ in re.finditer(r'\bWorld\s+(getWorldObj|getWorld)\s*\(\s*\)\s*\{',p.read_text()):inventory.append(str(p.relative_to(root)))
assert sorted(inventory)==sorted(m['source_getter_declarations']);print('PASS original AirPump body, three-owner ancestry hashes, no world shadow, source getter inventory')
