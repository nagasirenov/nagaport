path = r'C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\main\ModEventHandlerClient.java'
with open(path, 'rb') as f:
    c = f.read()

target = b'\t\t\tdouble d = Math.random();\r\n\t\t\tif(d < 0.1) setSplash(main, "Redditors aren\'t people!");\r\n\t\t}\r\n\t}\r\n}\r\n'
replacement = b'''\t\t\tdouble d = Math.random();\r
\t\t\tif(d < 0.1) setSplash(main, "Redditors aren\'t people!");\r
\t\t}\r
\t}\r
\r
\tprivate static java.lang.reflect.Field basicInventoryField;\r
\tstatic {\r
\t\ttry {\r
\t\t\tbasicInventoryField = net.minecraft.client.gui.inventory.GuiContainerCreative.class.getDeclaredField("basicInventory");\r
\t\t\tbasicInventoryField.setAccessible(true);\r
\t\t} catch(Exception e) {\r
\t\t\ttry {\r
\t\t\t\tbasicInventoryField = net.minecraft.client.gui.inventory.GuiContainerCreative.class.getDeclaredField("field_147060_v");\r
\t\t\t\tbasicInventoryField.setAccessible(true);\r
\t\t\t} catch(Exception ignored) {}\r
\t\t}\r
\t}\r
\r
\t@SubscribeEvent\r
\tpublic void onGuiDrawPre(net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent.Pre event) {\r
\t\tif(event.getGui() instanceof net.minecraft.client.gui.inventory.GuiContainerCreative) {\r
\t\t\tnet.minecraft.client.gui.inventory.GuiContainerCreative creative = (net.minecraft.client.gui.inventory.GuiContainerCreative) event.getGui();\r
\t\t\tif(creative.getSelectedTabIndex() == net.minecraft.creativetab.CreativeTabs.INVENTORY.getIndex()) {\r
\t\t\t\tif(basicInventoryField != null) {\r
\t\t\t\t\ttry {\r
\t\t\t\t\t\tnet.minecraft.inventory.InventoryBasic inv = (net.minecraft.inventory.InventoryBasic) basicInventoryField.get(null);\r
\t\t\t\t\t\tif(inv != null && !inv.getStackInSlot(0).isEmpty()) {\r
\t\t\t\t\t\t\tinv.setInventorySlotContents(0, net.minecraft.item.ItemStack.EMPTY);\r
\t\t\t\t\t\t}\r
\t\t\t\t\t} catch(Exception ignored) {}\r
\t\t\t\t}\r
\t\t\t}\r
\t\t}\r
\t}\r
}\r
'''

assert target in c, 'target not found'
new_c = c.replace(target, replacement)
with open(path, 'wb') as f:
    f.write(new_c)
print('Successfully updated ModEventHandlerClient.java')
