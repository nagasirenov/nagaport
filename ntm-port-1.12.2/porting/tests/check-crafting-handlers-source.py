#!/usr/bin/env python3
"""Invert only reviewed native API changes; freeze all original owner algorithms/arguments."""
import hashlib,json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
m=json.loads((ROOT/'porting/tests/crafting-handlers-source-manifest.json').read_text())
for path, expected in m['nonimport_sha256'].items():
 s=(ROOT/('src/main/java/com/hbm/'+path+'.java')).read_text()
 if path.endswith('RodRecipes'):
  s=s.replace('NtmCraftingRegistry.shaped("rod/zirnox_empty", true, ', 'GameRegistry.addRecipe(new ShapedOreRecipe(')
  s=s.replace("'B', BE.ingot() });", "'B', BE.ingot() }));")
 elif path.endswith('ContainerUpgradeCraftingHandler'):
  s=s.replace('super(null, result, items);','super(result, items);').replace('itemstack.isEmpty()', 'itemstack == null')
 elif path.endswith('ShapedOreRecipeExt'):
  s=s.replace('super(null, result, recipe);','super(result, recipe);')
 elif path.endswith('MKUCraftingHandler'):
  s=s.replace(' extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe>', '')
  s=s.replace('!stack.isEmpty()', 'stack != null').replace('stack.isEmpty()', 'stack == null')
  s=s.replace('\tpublic int getRecipeSize()', '\t@Override\n\tpublic int getRecipeSize()')
  s=s.replace('\t@Override\n\tpublic boolean canFit(int width, int height) {\n\t\treturn width * height >= 6;\n\t}\n\n\t@Override\n\tpublic boolean isDynamic() {\n\t\treturn true;\n\t}\n\n','')
 elif path.endswith('CraftingManager'):
  s,n=re.subn(r'NtmCraftingRegistry\.special\("[^\"]+", ', 'GameRegistry.addRecipe(',s); assert n==9
 s='\n'.join(x for x in s.splitlines() if not x.startswith('import '))
 assert hashlib.sha256(s.encode()).hexdigest()==expected,path
 print('PASS exact source conservation',path)
