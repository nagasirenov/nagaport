#!/usr/bin/env python3
"""Full production ContainerNT; bounded Base and utility collaborators. Real MC/Forge."""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile
p=argparse.ArgumentParser()
p.add_argument('--java-home',type=Path,required=True)
p.add_argument('--classpath',required=True)
a=p.parse_args()
root=Path(__file__).resolve().parents[2]
def source(name): return (root/'src/main/java'/name).read_text(encoding='utf-8')
def method(s,signature):
    start=s.index(signature); opening=s.index('{',start); depth=1; end=opening+1
    while depth:
        if s[end]=='{': depth+=1
        elif s[end]=='}': depth-=1
        end+=1
    return s[start:end]
with tempfile.TemporaryDirectory(prefix='hbm-container-clicks-') as tmp:
    out=Path(tmp); classes=out/'classes'; classes.mkdir(); files=[]
    def write(name,s):
        dest=out/name;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(s, encoding='utf-8');files.append(str(dest))
    # Compile complete production click implementation without editing its text.
    files.append(str(root/'src/main/java/com/hbm/inventory/container/ContainerNT.java'))
    # Unrelated Base slot-construction helpers depend on unported achievement/output slots.
    s=source('com/hbm/inventory/container/ContainerBase.java')
    methods=[method(s,x) for x in ['public ContainerBase(', 'public boolean canInteractWith(', 'public ItemStack transferStackInSlot(', 'protected boolean mergeItemStack(']]
    write('com/hbm/inventory/container/ContainerBase.java','package com.hbm.inventory.container;\nimport net.minecraft.entity.player.*;\nimport net.minecraft.inventory.*;\nimport net.minecraft.item.ItemStack;\npublic class ContainerBase extends Container { public IInventory tile;\n'+'\n'.join(methods)+'\n}')
    merge=method(source('com/hbm/util/InventoryUtil.java'),'public static boolean mergeItemStack(')
    write('com/hbm/util/InventoryUtil.java','package com.hbm.util;\nimport java.util.List;\nimport net.minecraft.inventory.Slot;\nimport net.minecraft.item.ItemStack;\npublic class InventoryUtil {\n'+merge+'\n}')
    copy=method(source('com/hbm/util/ItemStackUtil.java'),'public static ItemStack carefulCopyWithSize(')
    write('com/hbm/util/ItemStackUtil.java','package com.hbm.util;\nimport net.minecraft.item.ItemStack;\npublic class ItemStackUtil {\n'+copy+'\n}')
    files.append(str(root/'porting/tests/ContainerClicksMinecraftTest.java'))
    subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-cp',a.classpath,'-d',str(classes)]+files,check=True)
    subprocess.run([str(a.java_home/'bin/java'),'-cp',str(classes)+os.pathsep+a.classpath,'ContainerClicksMinecraftTest'],cwd=tmp,check=True)
