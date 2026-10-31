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

# PistonInserter
fix('src/main/java/com/hbm/blocks/machine/PistonInserter.java',
    '\t\t@Override public String getInventoryName() { return null; }\r\n\r\n\t\t@Override public boolean hasCustomInventoryName() { return false; }',
    '\t\t@Override public String getName() { return ""; }\r\n\r\n\t\t@Override public boolean hasCustomName() { return false; }\r\n\r\n\t\tpublic String getInventoryName() { return getName(); }\r\n\r\n\t\tpublic boolean hasCustomInventoryName() { return hasCustomName(); }')

# EntityDeliveryDrone
fix('src/main/java/com/hbm/entity/item/EntityDeliveryDrone.java',
    '\t@Override public String getInventoryName() { return "container.drone"; }\r\n\t@Override public int getInventoryStackLimit() { return 64; }\r\n\t@Override public boolean hasCustomInventoryName() { return false; }',
    '\t@Override public String getName() { return "container.drone"; }\r\n\t@Override public int getInventoryStackLimit() { return 64; }\r\n\t@Override public boolean hasCustomName() { return false; }\r\n\tpublic String getInventoryName() { return getName(); }\r\n\tpublic boolean hasCustomInventoryName() { return hasCustomName(); }')

# ItemAmmoBag
fix('src/main/java/com/hbm/items/tool/ItemAmmoBag.java',
    '\t\t@Override public String getInventoryName() { return "container.ammoBag"; }\r\n\t\t@Override public boolean hasCustomInventoryName() { return box.hasDisplayName(); }',
    '\t\t@Override public String getName() { return "container.ammoBag"; }\r\n\t\t@Override public boolean hasCustomName() { return box.hasDisplayName(); }\r\n\t\tpublic String getInventoryName() { return getName(); }\r\n\t\tpublic boolean hasCustomInventoryName() { return hasCustomName(); }')

# ItemCasingBag
fix('src/main/java/com/hbm/items/tool/ItemCasingBag.java',
    '\t\t@Override public String getInventoryName() { return "container.casingBag"; }\r\n\t\t@Override public boolean hasCustomInventoryName() { return box.hasDisplayName(); }',
    '\t\t@Override public String getName() { return "container.casingBag"; }\r\n\t\t@Override public boolean hasCustomName() { return box.hasDisplayName(); }\r\n\t\tpublic String getInventoryName() { return getName(); }\r\n\t\tpublic boolean hasCustomInventoryName() { return hasCustomName(); }')

# ItemPlasticBag
fix('src/main/java/com/hbm/items/tool/ItemPlasticBag.java',
    '\t\t@Override public String getInventoryName() { return "container.plasticBag"; }\r\n\t\t@Override public boolean hasCustomInventoryName() { return bag.hasDisplayName(); }',
    '\t\t@Override public String getName() { return "container.plasticBag"; }\r\n\t\t@Override public boolean hasCustomName() { return bag.hasDisplayName(); }\r\n\t\tpublic String getInventoryName() { return getName(); }\r\n\t\tpublic boolean hasCustomInventoryName() { return hasCustomName(); }')

# TileEntityRBMKSlottedBase
fix('src/main/java/com/hbm/tileentity/machine/rbmk/TileEntityRBMKSlottedBase.java',
    '\t@Override\r\n\tpublic String getInventoryName() {\r\n\t\treturn this.hasCustomInventoryName() ? this.customName : getName();\r\n\t}\r\n\r\n\t@Override\r\n\tpublic boolean hasCustomInventoryName() {\r\n\t\treturn this.customName != null && this.customName.length() > 0;\r\n\t}',
    '\t@Override\r\n\tpublic boolean hasCustomName() {\r\n\t\treturn this.customName != null && this.customName.length() > 0;\r\n\t}\r\n\r\n\tpublic String getInventoryName() {\r\n\t\treturn getName();\r\n\t}\r\n\r\n\tpublic boolean hasCustomInventoryName() {\r\n\t\treturn hasCustomName();\r\n\t}')

# TileEntityProxyCombo
fix('src/main/java/com/hbm/tileentity/TileEntityProxyCombo.java',
    '\t@Override\r\n\tpublic String getInventoryName() {\r\n\r\n\t\tif(!inventory)\r\n\t\t\treturn "";\r\n\r\n\t\tif(getCoreObject() instanceof ISidedInventory) {\r\n\t\t\treturn ((ISidedInventory)getCoreObject()).getInventoryName();\r\n\t\t}\r\n\r\n\t\treturn "";\r\n\t}\r\n\r\n\t@Override\r\n\tpublic boolean hasCustomInventoryName() {\r\n\r\n\t\tif(!inventory)\r\n\t\t\treturn false;\r\n\r\n\t\tif(getCoreObject() instanceof ISidedInventory) {\r\n\t\t\treturn ((ISidedInventory)getCoreObject()).hasCustomInventoryName();\r\n\t\t}\r\n\r\n\t\treturn false;\r\n\t}',
    '\t@Override\r\n\tpublic String getName() {\r\n\r\n\t\tif(!inventory)\r\n\t\t\treturn "";\r\n\r\n\t\tif(getCoreObject() instanceof ISidedInventory) {\r\n\t\t\treturn ((ISidedInventory)getCoreObject()).getName();\r\n\t\t}\r\n\r\n\t\treturn "";\r\n\t}\r\n\r\n\t@Override\r\n\tpublic boolean hasCustomName() {\r\n\r\n\t\tif(!inventory)\r\n\t\t\treturn false;\r\n\r\n\t\tif(getCoreObject() instanceof ISidedInventory) {\r\n\t\t\treturn ((ISidedInventory)getCoreObject()).hasCustomName();\r\n\t\t}\r\n\r\n\t\treturn false;\r\n\t}\r\n\r\n\tpublic String getInventoryName() {\r\n\t\treturn getName();\r\n\t}\r\n\r\n\tpublic boolean hasCustomInventoryName() {\r\n\t\treturn hasCustomName();\r\n\t}')

# TileEntityProxyInventory
fix('src/main/java/com/hbm/tileentity/TileEntityProxyInventory.java',
    '\t@Override\r\n\tpublic String getInventoryName() {\r\n\t\t\r\n\t\tISidedInventory inv = this.getBase();\r\n\t\t\r\n\t\tif(inv != null)\r\n\t\t\treturn inv.getInventoryName();\r\n\t\t\r\n\t\treturn "";\r\n\t}\r\n\r\n\t@Override\r\n\tpublic boolean hasCustomInventoryName() {\r\n\t\t\r\n\t\tISidedInventory inv = this.getBase();\r\n\t\t\r\n\t\tif(inv != null)\r\n\t\t\treturn inv.hasCustomInventoryName();\r\n\t\t\r\n\t\treturn false;\r\n\t}',
    '\t@Override\r\n\tpublic String getName() {\r\n\t\t\r\n\t\tISidedInventory inv = this.getBase();\r\n\t\t\r\n\t\tif(inv != null)\r\n\t\t\treturn inv.getName();\r\n\t\t\r\n\t\treturn "";\r\n\t}\r\n\r\n\t@Override\r\n\tpublic boolean hasCustomName() {\r\n\t\t\r\n\t\tISidedInventory inv = this.getBase();\r\n\t\t\r\n\t\tif(inv != null)\r\n\t\t\treturn inv.hasCustomName();\r\n\t\t\r\n\t\treturn false;\r\n\t}\r\n\r\n\tpublic String getInventoryName() {\r\n\t\treturn getName();\r\n\t}\r\n\r\n\tpublic boolean hasCustomInventoryName() {\r\n\t\treturn hasCustomName();\r\n\t}')
