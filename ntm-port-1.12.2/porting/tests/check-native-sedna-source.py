#!/usr/bin/env python3
"""Bounded source contract against the immutable pre-Sedna checkpoint. No downloads."""
import pathlib, re, importlib.util
root=pathlib.Path(__file__).resolve().parents[2]
# CP30 did not change these Sedna sources from CP29. The pinned reversible
# checkpoint boundary supplies the exact old bytes without local Git objects.
_spec=importlib.util.spec_from_file_location('sedna_boundary',root/'porting/tests/check-native-sedna-boundary.py')
_boundary=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_boundary)
def before(path):
 path=pathlib.Path(path)
 return _boundary.reverse_source(path.as_posix(),(root/path).read_bytes().decode('utf-8'))
def normalized(s):
 s=s.replace('package com.hbm.render.item.weapon.sedna;','package com.hbm.render.item.weapon.sedna;\n\nimport com.hbm.render.item.nativeitem.context.NativeItemContext;\nimport net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;') if 'ItemRenderType' in s or 'Object... data' in s else s
 for old,new in [('ItemRenderType type, Object... data','NativeItemContext context'),('renderEquipped(ItemStack stack, Object... data)','renderEquipped(ItemStack stack, NativeItemContext context)'),('(EntityLivingBase) data[1]','context.holder'),('type != ItemRenderType.EQUIPPED','!isThirdPerson(context.transform)'),('type == ItemRenderType.EQUIPPED','isThirdPerson(context.transform)'),('type == type.EQUIPPED','isThirdPerson(context.transform)'),('type == ItemRenderType.INVENTORY','context.transform == TransformType.GUI'),('type == ItemRenderType.ENTITY','context.transform == TransformType.GROUND'),('renderOther(stack, ItemRenderType.INVENTORY)','renderOther(stack, new NativeItemContext(TransformType.GUI, null))'),('renderOther(stack, ItemRenderType.ENTITY)','renderOther(stack, new NativeItemContext(TransformType.GROUND, null))'),('renderOther(stack, ItemRenderType.EQUIPPED, data)','renderOther(stack, context)'),('renderOther(stack, ItemRenderType.EQUIPPED)','renderOther(stack, new NativeItemContext(TransformType.THIRD_PERSON_RIGHT_HAND, null))'),('Minecraft.getMinecraft().renderEngine','Minecraft.getMinecraft().getTextureManager()'),('.thePlayer','.player'),('.theWorld','.world'),('MathHelper.sqrt_float','MathHelper.sqrt'),('MathHelper.floor_double','MathHelper.floor')]:s=s.replace(old,new)
 return s
files=list((root/'src/main/java/com/hbm/render/item/weapon/sedna').glob('*.java'))
consumers=[p for p in files if p.name!='ItemRenderWeaponBase.java']
assert len(consumers)==51
for p in consumers: assert [l.rstrip() for l in normalized(before(p.relative_to(root))).splitlines()]==[l.rstrip() for l in p.read_text().splitlines()],str(p)+' changed beyond explicit typed context/native names'
factory=pathlib.Path('src/main/java/com/hbm/items/weapon/sedna/factory/GunFactoryClient.java');old=before(factory);new=(root/factory).read_text()
a=old.index('\t\t//GUNS');b=old.index('\t\t//PROJECTILES');guns=old[a:b].replace('MinecraftForgeClient.registerItemRenderer(','renderers.put(')
assert old[a:b].count('MinecraftForgeClient.registerItemRenderer(')==67
assert '\n'.join(l.rstrip() for l in guns.splitlines()) in '\n'.join(l.rstrip() for l in new.splitlines()) and new.count('renderers.put(')==67
assert old[b:old.rfind('}')].rstrip()==new[new.index('\t\t//PROJECTILES'):new.index('    public static Map<Item, ItemRenderWeaponBase>')].rstrip(),'Projectile registration bytes changed'
base=pathlib.Path('src/main/java/com/hbm/render/item/weapon/sedna/ItemRenderWeaponBase.java');old=before(base);new=(root/base).read_text()
def args(s):return [re.sub(r'\s+','',v) for v in s.split(',')]
oldvertices=[args(m) for m in re.findall(r'tess\.addVertex(?:WithUV)?\(([^;]+)\);',old)]
newvertices=[]
for pos,uv in re.findall(r'buffer\.pos\(([^;]+?)\)\.color\([^;]+?\)(?:\.tex\(([^;]+?)\))?\.(?:lightmap|normal)',new):newvertices.append(args(pos)+(args(uv) if uv else []))
assert oldvertices==newvertices,'Muzzle/gap/laser/smoke positions, UV or order changed'
assert len(oldvertices)==44
assert new.count('.lightmap(0, 240).normal(0, 1, 0).endVertex();')==36
assert new.count('.color(1F, 1F, 1F, alpha).normal(0, 1, 0).endVertex();')==8
assert re.findall(r'tess.setColorRGBA_F\(1F, 1F, 1F, ([^;]+)\);',old[old.index('public static void renderSmokeNodes'):old.index('public static void renderMuzzleFlash')])==re.findall(r'alpha = ([^;]+);',new)[1:]
assert 'IItemRenderer' not in new and 'Object...' not in new and 'ItemRenderType' not in new
print('PASS source: 51 exact type-only consumer migrations, 67 exact renderer entries, projectile bytes unchanged, all 44 shared effect vertices/UVs/order and alpha preserved')
