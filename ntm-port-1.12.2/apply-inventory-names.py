import os, re

def fix(path, target, rep):
    with open(path, 'rb') as f:
        data = f.read()
    if target.encode('utf-8') not in data:
        print(f'ERROR in {path}: target not found')
        return False
    data = data.replace(target.encode('utf-8'), rep.encode('utf-8'))
    with open(path, 'wb') as f:
        f.write(data)
    print(f'OK: {path}')
    return True

# 1. BlockPWR
fix('src/main/java/com/hbm/blocks/machine/BlockPWR.java',
    '\t\t@Override public String getInventoryName() { return ""; }\r\n\t\t@Override public boolean hasCustomInventoryName() { return false; }',
    '\t\t@Override public String getName() { return ""; }\r\n\t\t@Override public boolean hasCustomName() { return false; }\r\n\t\tpublic String getInventoryName() { return getName(); }\r\n\t\tpublic boolean hasCustomInventoryName() { return hasCustomName(); }')

# 2. PistonInserter
fix('src/main/java/com/hbm/blocks/machine/PistonInserter.java',
    '@Override public String getInventoryName() { return null; }\r\n\t@Override public boolean hasCustomInventoryName() { return false; }',
    '@Override public String getName() { return ""; }\r\n\t@Override public boolean hasCustomName() { return false; }\r\n\tpublic String getInventoryName() { return getName(); }\r\n\tpublic boolean hasCustomInventoryName() { return hasCustomName(); }')

# 3. EntityDeliveryDrone
fix('src/main/java/com/hbm/entity/item/EntityDeliveryDrone.java',
    '@Override public String getInventoryName() { return "container.drone"; }\r\n\t@Override public boolean hasCustomInventoryName() { return false; }',
    '@Override public String getName() { return "container.drone"; }\r\n\t@Override public boolean hasCustomName() { return false; }\r\n\tpublic String getInventoryName() { return getName(); }\r\n\tpublic boolean hasCustomInventoryName() { return hasCustomName(); }')

# 4. ItemAmmoBag
fix('src/main/java/com/hbm/items/tool/ItemAmmoBag.java',
    '@Override public String getInventoryName() { return "container.ammoBag"; }\r\n\t@Override public boolean hasCustomInventoryName() { return false; }',
    '@Override public String getName() { return "container.ammoBag"; }\r\n\t@Override public boolean hasCustomName() { return false; }\r\n\tpublic String getInventoryName() { return getName(); }\r\n\tpublic boolean hasCustomInventoryName() { return hasCustomName(); }')

# 5. ItemCasingBag
fix('src/main/java/com/hbm/items/tool/ItemCasingBag.java',
    '@Override public String getInventoryName() { return "container.casingBag"; }\r\n\t@Override public boolean hasCustomInventoryName() { return false; }',
    '@Override public String getName() { return "container.casingBag"; }\r\n\t@Override public boolean hasCustomName() { return false; }\r\n\tpublic String getInventoryName() { return getName(); }\r\n\tpublic boolean hasCustomInventoryName() { return hasCustomName(); }')

# 6. ItemPlasticBag
fix('src/main/java/com/hbm/items/tool/ItemPlasticBag.java',
    '@Override public String getInventoryName() { return "container.plasticBag"; }\r\n\t@Override public boolean hasCustomInventoryName() { return false; }',
    '@Override public String getName() { return "container.plasticBag"; }\r\n\t@Override public boolean hasCustomName() { return false; }\r\n\tpublic String getInventoryName() { return getName(); }\r\n\tpublic boolean hasCustomInventoryName() { return hasCustomName(); }')

# 7. EntityMinecartModBase
fix('src/main/java/com/hbm/entity/item/EntityMinecartModBase.java',
    '@Override\r\n\tpublic boolean hasCustomInventoryName()\r\n    {\r\n        return this.entityName != null;\r\n    }',
    '@Override\r\n\tpublic boolean hasCustomName()\r\n    {\r\n        return this.entityName != null;\r\n    }\r\n\r\n\tpublic boolean hasCustomInventoryName()\r\n    {\r\n        return hasCustomName();\r\n    }')

# 8. EntityMinecartContainerBase
fix('src/main/java/com/hbm/entity/cart/EntityMinecartContainerBase.java',
    '@Override\r\n\tpublic String getInventoryName() {\r\n\t\treturn this.hasCustomInventoryName() ? this.func_95999_t() : "container.minecart";\r\n\t}',
    '@Override\r\n\tpublic String getName() {\r\n\t\treturn this.hasCustomName() ? this.func_95999_t() : "container.minecart";\r\n\t}\r\n\r\n\tpublic String getInventoryName() {\r\n\t\treturn getName();\r\n\t}')

# 9. TrainCargoTram
fix('src/main/java/com/hbm/entity/train/TrainCargoTram.java',
    '@Override public String getInventoryName() { return this.hasCustomInventoryName() ? this.getEntityName() : "container.trainTram"; }',
    '@Override public String getName() { return this.hasCustomName() ? this.getEntityName() : "container.trainTram"; }\r\n\tpublic String getInventoryName() { return getName(); }')

# 10. TrainCargoTramTrailer
fix('src/main/java/com/hbm/entity/train/TrainCargoTramTrailer.java',
    '@Override public String getInventoryName() { return this.hasCustomInventoryName() ? this.getEntityName() : "container.trainTramTrailer"; }',
    '@Override public String getName() { return this.hasCustomName() ? this.getEntityName() : "container.trainTramTrailer"; }\r\n\tpublic String getInventoryName() { return getName(); }')

# 11. TileEntitySolarMirror
fix('src/main/java/com/hbm/tileentity/machine/TileEntitySolarMirror.java',
    '\t@Override\r\n\tpublic String getInventoryName() {\r\n\t\treturn null;\r\n\t}',
    '\tpublic String getInventoryName() {\r\n\t\treturn null;\r\n\t}')

# 12. TileEntityICFController
fix('src/main/java/com/hbm/tileentity/machine/TileEntityICFController.java',
    '\t@Override\r\n\tpublic String getInventoryName() {\r\n\t\treturn "container.icfController";\r\n\t}',
    '\tpublic String getInventoryName() {\r\n\t\treturn "container.icfController";\r\n\t}')

# 13. TileEntityMachineStrandCaster
fix('src/main/java/com/hbm/tileentity/machine/TileEntityMachineStrandCaster.java',
    '\t@Override\r\n\tpublic String getInventoryName() {\r\n\t\treturn getName();\r\n\t}',
    '\tpublic String getInventoryName() {\r\n\t\treturn getName();\r\n\t}')

# 14. TileEntityRequestNetworkContainer
fix('src/main/java/com/hbm/tileentity/network/TileEntityRequestNetworkContainer.java',
    '@Override public String getInventoryName() { return this.hasCustomInventoryName() ? this.customName : getName(); }',
    'public String getInventoryName() { return this.hasCustomInventoryName() ? this.customName : getName(); }')

# 15. TileEntityProxyCombo
fix('src/main/java/com/hbm/tileentity/TileEntityProxyCombo.java',
    '\t@Override\r\n\tpublic String getInventoryName() {\r\n\t\treturn ((ISidedInventory)getCoreObject()).getInventoryName();\r\n\t}\r\n\r\n\t@Override\r\n\tpublic boolean hasCustomInventoryName() {\r\n\t\treturn ((ISidedInventory)getCoreObject()).hasCustomInventoryName();\r\n\t}',
    '\t@Override\r\n\tpublic String getName() {\r\n\t\treturn ((ISidedInventory)getCoreObject()).getName();\r\n\t}\r\n\r\n\t@Override\r\n\tpublic boolean hasCustomName() {\r\n\t\treturn ((ISidedInventory)getCoreObject()).hasCustomName();\r\n\t}\r\n\r\n\tpublic String getInventoryName() {\r\n\t\treturn getName();\r\n\t}\r\n\r\n\tpublic boolean hasCustomInventoryName() {\r\n\t\treturn hasCustomName();\r\n\t}')

# 16. TileEntityProxyInventory
fix('src/main/java/com/hbm/tileentity/TileEntityProxyInventory.java',
    '\t@Override\r\n\tpublic String getInventoryName() {\r\n\t\treturn inv.getInventoryName();\r\n\t}\r\n\r\n\t@Override\r\n\tpublic boolean hasCustomInventoryName() {\r\n\t\treturn inv.hasCustomInventoryName();\r\n\t}',
    '\t@Override\r\n\tpublic String getName() {\r\n\t\treturn inv.getName();\r\n\t}\r\n\r\n\t@Override\r\n\tpublic boolean hasCustomName() {\r\n\t\treturn inv.hasCustomName();\r\n\t}\r\n\r\n\tpublic String getInventoryName() {\r\n\t\treturn getName();\r\n\t}\r\n\r\n\tpublic boolean hasCustomInventoryName() {\r\n\t\treturn hasCustomName();\r\n\t}')

# 17. The 24 TileEntities
tile_files = [
    'src/main/java/com/hbm/tileentity/bomb/TileEntityBombMulti.java',
    'src/main/java/com/hbm/tileentity/bomb/TileEntityCompactLauncher.java',
    'src/main/java/com/hbm/tileentity/bomb/TileEntityLaunchTable.java',
    'src/main/java/com/hbm/tileentity/bomb/TileEntityNukeBoy.java',
    'src/main/java/com/hbm/tileentity/bomb/TileEntityNukeCustom.java',
    'src/main/java/com/hbm/tileentity/bomb/TileEntityNukeFleija.java',
    'src/main/java/com/hbm/tileentity/bomb/TileEntityNukeGadget.java',
    'src/main/java/com/hbm/tileentity/bomb/TileEntityNukeMan.java',
    'src/main/java/com/hbm/tileentity/bomb/TileEntityNukeMike.java',
    'src/main/java/com/hbm/tileentity/bomb/TileEntityNukeN2.java',
    'src/main/java/com/hbm/tileentity/bomb/TileEntityNukePrototype.java',
    'src/main/java/com/hbm/tileentity/bomb/TileEntityNukeSolinium.java',
    'src/main/java/com/hbm/tileentity/bomb/TileEntityNukeTsar.java',
    'src/main/java/com/hbm/tileentity/machine/TileEntityForceField.java',
    'src/main/java/com/hbm/tileentity/machine/TileEntityMachineKeyForge.java',
    'src/main/java/com/hbm/tileentity/machine/TileEntityMachineMissileAssembly.java',
    'src/main/java/com/hbm/tileentity/machine/TileEntityMachineSatDock.java',
    'src/main/java/com/hbm/tileentity/machine/TileEntityMachineSatLinker.java',
    'src/main/java/com/hbm/tileentity/machine/TileEntityMachineShredder.java',
    'src/main/java/com/hbm/tileentity/machine/TileEntityMachineSiren.java',
    'src/main/java/com/hbm/tileentity/machine/TileEntityMachineTurbine.java',
    'src/main/java/com/hbm/tileentity/machine/TileEntityRtgFurnace.java',
    'src/main/java/com/hbm/tileentity/machine/TileEntityWasteDrum.java',
    'src/main/java/com/hbm/tileentity/machine/rbmk/TileEntityRBMKSlottedBase.java'
]

pattern_multi = re.compile(
    r'(\t*)@Override\r?\n\t*public\s+String\s+getInventoryName\s*\(\s*\)\s*\{\r?\n'
    r'(\t*)return\s+this\.hasCustomInventoryName\(\)\s*\?\s*this\.customName\s*:\s*\"([^\"]+)\";\r?\n'
    r'(\t*)\}\r?\n\r?\n'
    r'(\t*)@Override\r?\n\t*public\s+boolean\s+hasCustomInventoryName\s*\(\s*\)\s*\{\r?\n'
    r'(\t*)return\s+this\.customName\s*!=\s*null\s*&&\s*this\.customName\.length\(\)\s*>\s*0;\r?\n'
    r'(\t*)\}'
)

pattern_single = re.compile(
    r'(\t*)@Override\s+public\s+String\s+getInventoryName\s*\(\s*\)\s*\{\s*return\s+this\.hasCustomInventoryName\(\)\s*\?\s*this\.customName\s*:\s*\"([^\"]+)\";\s*\}\r?\n'
    r'(\t*)@Override\s+public\s+boolean\s+hasCustomInventoryName\s*\(\s*\)\s*\{\s*return\s+this\.customName\s*!=\s*null\s*&&\s*this\.customName\.length\(\)\s*>\s*0;\s*\}'
)

for p in tile_files:
    with open(p, 'rb') as f:
        data = f.read()
    content = data.decode('utf-8')
    m = pattern_multi.search(content)
    if m:
        tabs = m.group(1)
        container_id = m.group(3)
        rep = (f'{tabs}@Override\r\n'
               f'{tabs}public String getName() {{\r\n'
               f'{tabs}\treturn this.hasCustomName() ? this.customName : \"{container_id}\";\r\n'
               f'{tabs}}}\r\n\r\n'
               f'{tabs}@Override\r\n'
               f'{tabs}public boolean hasCustomName() {{\r\n'
               f'{tabs}\treturn this.customName != null && this.customName.length() > 0;\r\n'
               f'{tabs}}}\r\n\r\n'
               f'{tabs}public String getInventoryName() {{\r\n'
               f'{tabs}\treturn getName();\r\n'
               f'{tabs}}}\r\n\r\n'
               f'{tabs}public boolean hasCustomInventoryName() {{\r\n'
               f'{tabs}\treturn hasCustomName();\r\n'
               f'{tabs}}}')
        new_content = pattern_multi.sub(rep, content, count=1)
        with open(p, 'wb') as f:
            f.write(new_content.encode('utf-8'))
        print(f'Multi matched: {p}')
    else:
        m2 = pattern_single.search(content)
        if m2:
            tabs = m2.group(1)
            container_id = m2.group(2)
            rep = (f'{tabs}@Override public String getName() {{ return this.hasCustomName() ? this.customName : \"{container_id}\"; }}\r\n'
                   f'{tabs}@Override public boolean hasCustomName() {{ return this.customName != null && this.customName.length() > 0; }}\r\n'
                   f'{tabs}public String getInventoryName() {{ return getName(); }}\r\n'
                   f'{tabs}public boolean hasCustomInventoryName() {{ return hasCustomName(); }}')
            new_content = pattern_single.sub(rep, content, count=1)
            with open(p, 'wb') as f:
                f.write(new_content.encode('utf-8'))
            print(f'Single matched: {p}')
        else:
            print(f'NO MATCH: {p}')
