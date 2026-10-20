# Native clear argument batch

This source-only batch converts 19 literal null arguments in 17 files to ItemStack.EMPTY at native Slot.putStack / IInventory.setInventorySlotContents calls. All other source bytes are preserved. It does not change domain arrays, helper APIs, getter contracts, returned values, callback signatures, or recipe behavior. No runtime inventory whitelist is used.

Original files were captured under /tmp/clears-before. The persisted manifest supports fail-closed exact whole-file reversal without that temporary directory or Git history. Compose check-native-clears-source.py reverse_source before historical reversals; do not rehash old manifests.

Validation: python porting/tests/check-native-clears-source.py --migration-only passes 17 reversals, 19 exact argument edits and 51 drift rejections. The default mode additionally scans all Java source lexically, ignoring comments/string literals and accounting for nested call arguments, and requires zero native literal-null clears. The four separately owned ingress calls listed in the manifest have now been migrated by their owner; default audit passes with zero remaining literal-null native clears. No Gradle, Minecraft runtime, compilation, or gameplay acceptance is claimed.

## Changed source files

- src/main/java/com/hbm/inventory/container/ContainerAnvil.java (2 calls)
- src/main/java/com/hbm/inventory/container/ContainerArmorTable.java (1 calls)
- src/main/java/com/hbm/inventory/container/ContainerMachineAnnihilator.java (1 calls)
- src/main/java/com/hbm/inventory/container/ContainerMachineAssemblyFactory.java (1 calls)
- src/main/java/com/hbm/inventory/container/ContainerMachineAssemblyMachine.java (1 calls)
- src/main/java/com/hbm/inventory/container/ContainerMachineChemicalFactory.java (1 calls)
- src/main/java/com/hbm/inventory/container/ContainerMachineChemicalPlant.java (1 calls)
- src/main/java/com/hbm/inventory/container/ContainerMachinePUREX.java (1 calls)
- src/main/java/com/hbm/inventory/container/ContainerMachinePrecAss.java (1 calls)
- src/main/java/com/hbm/inventory/container/ContainerMachineStrandCaster.java (1 calls)
- src/main/java/com/hbm/inventory/container/ContainerMachineWarController.java (1 calls)
- src/main/java/com/hbm/inventory/container/ContainerSatDock.java (1 calls)
- src/main/java/com/hbm/inventory/container/ContainerToolBox.java (1 calls)
- src/main/java/com/hbm/inventory/container/ContainerWeaponTable.java (2 calls)
- src/main/java/com/hbm/items/tool/ItemToolBox.java (1 calls)
- src/main/java/com/hbm/main/ModEventHandler.java (1 calls)
- src/main/java/com/hbm/tileentity/network/TileEntityDroneCrate.java (1 calls)
