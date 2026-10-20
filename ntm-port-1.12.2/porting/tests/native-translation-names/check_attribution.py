import pathlib,json,sys
h=pathlib.Path(__file__).resolve().parent;root=h.parents[3];m=json.loads((h/'manifest.json').read_text());rows={}
for line in pathlib.Path(sys.argv[1]).read_text().splitlines():
 r=line.split('\t');rows[(r[1],int(r[2]))]=r
selected=[];owners=set()
for f in m['files']:
 shift=0
 for e in sorted(f['edits'],key=lambda e:e['start']):
  r=rows[(str(root/f['path']),e['start']+shift)];assert r[0]=='YES' and r[4]==e['new'];assert r[-1] in ['net.minecraft.item.Item','net.minecraft.block.Block','net.minecraft.item.ItemBlock','com.hbm.items.ItemEnumMulti','com.hbm.items.ItemCustomLore'];selected.append('\t'.join(r));owners.add(r[-1]);shift+=len(e['new'])-len(e['old'])
(h/'attributed-after-selected.tsv').write_text('\n'.join(selected)+'\n')
for line in (h/'after.log').read_text().splitlines():
 if line.startswith('DECL\t'):
  _,owner,name,argc,isItemBlock=line.split('\t');assert not(isItemBlock=='true' and name=='getUnlocalizedName' and argc=='0'),line
print('PASS',len(selected),'exact-position native method attributions;',sorted(owners),'; no NTM Item/Block zero-argument legacy overrides')
