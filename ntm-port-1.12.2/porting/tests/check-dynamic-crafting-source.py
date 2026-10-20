#!/usr/bin/env python3
"""Freeze dynamic handler algorithms and registrations across native adaptation."""
import hashlib,json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
m=json.loads((ROOT/'porting/tests/dynamic-crafting-source-manifest.json').read_text())
for path, expected in m['nonimport_sha256'].items():
 s=(ROOT/('src/main/java/com/hbm/'+path+'.java')).read_text()
 if path.endswith('CraftingManager'):
  s,n=re.subn(r'NtmCraftingRegistry\.special\("special/(?:rbmk_fuel|scraps|cargo_shell)", ', 'GameRegistry.addRecipe(',s);assert n==3
 else:
  s=s.replace(' extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe>', '')
  s=s.replace('!stack.isEmpty()', 'stack != null').replace('stack.isEmpty()', 'stack == null').replace('return ItemStack.EMPTY;', 'return null;')
  s=s.replace('\tpublic int getRecipeSize()', '\t@Override\n\tpublic int getRecipeSize()')
  s,n=re.subn(r'\t@Override\n\tpublic boolean canFit\(int width, int height\) \{\n\t\treturn width \* height >= [12];\n\t}\n\n\t@Override\n\tpublic boolean isDynamic\(\) \{\n\t\treturn true;\n\t}\n\n','',s);assert n==1
  if path.endswith('CargoShellCraftingHandler'):
   s=s.replace('\t\t\t\t// Both original NTM leave-grid opt-outs also report a container; vanilla always left the grid.\n\t\t\t\t// The removed Forge hook adds no exclusion in that verified domain. See dynamic-crafting notes.\n','')
   s=s.replace('if(stack.getItem().hasContainerItem(stack))','if(stack.getItem().hasContainerItem(stack) || !stack.getItem().doesContainerItemLeaveCraftingGrid(stack))')
 s='\n'.join(x for x in s.splitlines() if not x.startswith('import '))
 assert hashlib.sha256(s.encode()).hexdigest()==expected,path
 print('PASS exact source conservation',path)
