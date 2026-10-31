from pathlib import Path

# 1. ArmorFSB.java
p = Path('src/main/java/com/hbm/items/armor/ArmorFSB.java')
raw = p.read_bytes()
method_code = (
    b'\t@Override\r\n'
    b'\tpublic ArmorFSB setTranslationKey(String key) {\r\n'
    b'\t\tsuper.setTranslationKey(key);\r\n'
    b'\t\treturn this;\r\n'
    b'\t}\r\n\r\n'
    b'\tpublic ArmorFSB setTextureName(String texture) {\r\n'
    b'\t\tNativeItemTextureRegistry.texture(this, texture);\r\n'
    b'\t\treturn this;\r\n'
    b'\t}\r\n\r\n'
)
anchor = b'\tpublic ArmorFSB addEffect(PotionEffect effect) {'
assert anchor in raw, 'ArmorFSB anchor not found'
raw = raw.replace(anchor, method_code + anchor, 1)
p.write_bytes(raw)
print('ArmorFSB updated.')

# 2. ModItemsArmor.java
p = Path('src/main/java/com/hbm/items/ModItemsArmor.java')
raw = p.read_bytes()
raw = raw.replace(
    b'\t\tno9 = new ArmorNo9(MainRegistry.aMatSteel, 0).setTranslationKey("no9").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":no9");',
    b'\t\tno9 = NativeItemTextureRegistry.texture(new ArmorNo9(MainRegistry.aMatSteel, 0).setTranslationKey("no9").setMaxStackSize(1), RefStrings.MODID + ":no9");'
)
raw = raw.replace(
    b'\t\tbeta = new ItemDrop().setTranslationKey("beta").setMaxStackSize(1).setTextureName(RefStrings.MODID + ":beta");',
    b'\t\tbeta = NativeItemTextureRegistry.texture(new ItemDrop().setTranslationKey("beta").setMaxStackSize(1), RefStrings.MODID + ":beta");'
)
raw = raw.replace(b'.setUnlocalizedName(', b'.setTranslationKey(')
p.write_bytes(raw)
print('ModItemsArmor updated.')

# 3. ItemSimpleConsumable.java
p = Path('src/main/java/com/hbm/items/special/ItemSimpleConsumable.java')
raw = p.read_bytes()
method_code = (
    b'\t@Override\r\n'
    b'\tpublic ItemSimpleConsumable setTranslationKey(String key) {\r\n'
    b'\t\tsuper.setTranslationKey(key);\r\n'
    b'\t\treturn this;\r\n'
    b'\t}\r\n\r\n'
    b'\t@Override\r\n'
    b'\tpublic ItemSimpleConsumable setFull3D() {\r\n'
    b'\t\tsuper.setFull3D();\r\n'
    b'\t\treturn this;\r\n'
    b'\t}\r\n\r\n'
    b'\tpublic ItemSimpleConsumable setTextureName(String texture) {\r\n'
    b'\t\tcom.hbm.render.item.NativeItemTextureRegistry.texture(this, texture);\r\n'
    b'\t\treturn this;\r\n'
    b'\t}\r\n\r\n'
)
anchor = b'\tpublic ItemSimpleConsumable() {'
assert anchor in raw, 'ItemSimpleConsumable anchor not found'
raw = raw.replace(anchor, method_code + anchor, 1)
raw = raw.replace(b'.setUnlocalizedName(', b'.setTranslationKey(')
p.write_bytes(raw)
print('ItemSimpleConsumable updated.')

# 4. XFactoryRocket.java & XFactoryTool.java
p = Path('src/main/java/com/hbm/items/weapon/sedna/factory/XFactoryRocket.java')
raw = p.read_bytes().replace(b'.setUnlocalizedName("gun_stinger")', b'.setTranslationKey("gun_stinger")')
p.write_bytes(raw)

p = Path('src/main/java/com/hbm/items/weapon/sedna/factory/XFactoryTool.java')
raw = p.read_bytes().replace(b'.setUnlocalizedName("gun_charge_thrower")', b'.setTranslationKey("gun_charge_thrower")')
p.write_bytes(raw)
print('XFactoryRocket & XFactoryTool updated.')

# 5. ItemStack.getUnlocalizedName() in target files
itemstack_targets = [
    ('src/main/java/com/hbm/items/tool/ItemCasingBag.java', b'casing.getUnlocalizedName()', b'casing.getTranslationKey()'),
    ('src/main/java/com/hbm/items/weapon/sedna/factory/XFactory12ga.java', b'stack.getUnlocalizedName()', b'stack.getTranslationKey()'),
    ('src/main/java/com/hbm/items/weapon/sedna/factory/XFactory22lr.java', b'stack.getUnlocalizedName()', b'stack.getTranslationKey()'),
    ('src/main/java/com/hbm/items/weapon/sedna/factory/XFactory44.java', b'stack.getUnlocalizedName()', b'stack.getTranslationKey()'),
    ('src/main/java/com/hbm/items/weapon/sedna/factory/XFactory556mm.java', b'stack.getUnlocalizedName()', b'stack.getTranslationKey()'),
    ('src/main/java/com/hbm/items/weapon/sedna/factory/XFactory9mm.java', b'stack.getUnlocalizedName()', b'stack.getTranslationKey()'),
    ('src/main/java/com/hbm/tileentity/IRadioisotopeFuel.java', b'instance.getDecayItem().getUnlocalizedName()', b'instance.getDecayItem().getTranslationKey()'),
    ('src/main/java/com/hbm/tileentity/machine/albion/TileEntityPADetector.java', b'slot.getUnlocalizedName()', b'slot.getTranslationKey()'),
    ('src/main/java/com/hbm/tileentity/machine/albion/TileEntityPASource.java', b'slot.getUnlocalizedName()', b'slot.getTranslationKey()'),
    ('src/main/java/com/hbm/tileentity/machine/fusion/TileEntityFusionBreeder.java', b'input.getUnlocalizedName()', b'input.getTranslationKey()'),
    ('src/main/java/com/hbm/tileentity/machine/fusion/TileEntityFusionBreeder.java', b'output.getUnlocalizedName()', b'output.getTranslationKey()'),
    ('src/main/java/com/hbm/tileentity/machine/rbmk/TileEntityRBMKConsole.java', b'rod.getUnlocalizedName()', b'rod.getTranslationKey()'),
    ('src/main/java/com/hbm/tileentity/machine/rbmk/TileEntityRBMKConsole.java', b'input.getUnlocalizedName()', b'input.getTranslationKey()'),
    ('src/main/java/com/hbm/tileentity/machine/rbmk/TileEntityRBMKOutgasser.java', b'slots[0].getUnlocalizedName()', b'slots[0].getTranslationKey()'),
    ('src/main/java/com/hbm/tileentity/machine/rbmk/TileEntityRBMKOutgasser.java', b'input.getUnlocalizedName()', b'input.getTranslationKey()'),
    ('src/main/java/com/hbm/tileentity/machine/storage/TileEntityBatterySocket.java', b'slots[0].getUnlocalizedName()', b'slots[0].getTranslationKey()'),
    ('src/main/java/com/hbm/util/ArmorUtil.java', b'item.getUnlocalizedName()', b'item.getTranslationKey()')
]

for file_path, src_bytes, dst_bytes in itemstack_targets:
    p = Path(file_path)
    raw = p.read_bytes()
    assert src_bytes in raw, f'Target {src_bytes} not found in {file_path}'
    raw = raw.replace(src_bytes, dst_bytes)
    p.write_bytes(raw)
print('ItemStack translation keys updated.')
