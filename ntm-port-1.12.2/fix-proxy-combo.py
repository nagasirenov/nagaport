with open('src/main/java/com/hbm/tileentity/TileEntityProxyCombo.java', 'rb') as f:
    data = f.read()

target = (b'\t@Override\r\n'
          b'\tpublic String getInventoryName() {\r\n\r\n'
          b'\t\tif(!inventory)\r\n'
          b'\t\t\treturn null;\r\n\r\n'
          b'\t\tif(getCoreObject() instanceof ISidedInventory) {\r\n'
          b'\t\t\treturn ((ISidedInventory)getCoreObject()).getInventoryName();\r\n'
          b'\t\t}\r\n\r\n'
          b'\t\treturn null;\r\n'
          b'\t}\r\n\r\n'
          b'\t@Override\r\n'
          b'\tpublic boolean hasCustomInventoryName() {\r\n\r\n'
          b'\t\tif(!inventory)\r\n'
          b'\t\t\treturn false;\r\n\r\n'
          b'\t\tif(getCoreObject() instanceof ISidedInventory) {\r\n'
          b'\t\t\treturn ((ISidedInventory)getCoreObject()).hasCustomInventoryName();\r\n'
          b'\t\t}\r\n\r\n'
          b'\t\treturn false;\r\n'
          b'\t}')

rep = (b'\t@Override\r\n'
       b'\tpublic String getName() {\r\n\r\n'
       b'\t\tif(!inventory)\r\n'
       b'\t\t\treturn "";\r\n\r\n'
       b'\t\tif(getCoreObject() instanceof ISidedInventory) {\r\n'
       b'\t\t\treturn ((ISidedInventory)getCoreObject()).getName();\r\n'
       b'\t\t}\r\n\r\n'
       b'\t\treturn "";\r\n'
       b'\t}\r\n\r\n'
       b'\t@Override\r\n'
       b'\tpublic boolean hasCustomName() {\r\n\r\n'
       b'\t\tif(!inventory)\r\n'
       b'\t\t\treturn false;\r\n\r\n'
       b'\t\tif(getCoreObject() instanceof ISidedInventory) {\r\n'
       b'\t\t\treturn ((ISidedInventory)getCoreObject()).hasCustomName();\r\n'
       b'\t\t}\r\n\r\n'
       b'\t\treturn false;\r\n'
       b'\t}\r\n\r\n'
       b'\tpublic String getInventoryName() {\r\n'
       b'\t\treturn getName();\r\n'
       b'\t}\r\n\r\n'
       b'\tpublic boolean hasCustomInventoryName() {\r\n'
       b'\t\treturn hasCustomName();\r\n'
       b'\t}')

assert target in data, 'Target not found in TileEntityProxyCombo'
data = data.replace(target, rep)
with open('src/main/java/com/hbm/tileentity/TileEntityProxyCombo.java', 'wb') as f:
    f.write(data)
print('TileEntityProxyCombo updated')
