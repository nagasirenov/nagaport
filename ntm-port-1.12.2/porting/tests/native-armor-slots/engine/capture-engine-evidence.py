#!/usr/bin/env python3
"""Capture existing, authoritative engine/Forge armor contracts. No build or download."""
from pathlib import Path
import hashlib, json, re, subprocess, zipfile
OUT=Path(__file__).resolve().parent
TC=Path('/workspace/shared/nagaport-toolchain')
JAVAP=TC/'java8/bin/javap'
OLD=Path('/tmp/nagaport-legacy-client.jar')
OLD_FORGE=Path('/tmp/nagaport-forge1614-userdev.jar')
OLD_PATCHES=Path('/tmp/nagaport-forge1614-patches.zip')
NEW=next(Path(x) for x in (TC/'native-classpath.txt').read_text().strip().split(':') if '/forgeBin-' in x)
NEW_DIR=TC/'gradle-user-home/caches/minecraft/net/minecraftforge/forge/1.12.2-14.23.5.2768/userdev'
NEW_SOURCES=NEW_DIR/'sources.zip'
NEW_PATCHES=NEW_DIR/'patches.zip'
ORIGINAL_MOD=Path('/workspace/shared/nagaport/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar')

def javap(jar, name):
    return subprocess.check_output([str(JAVAP),'-classpath',str(jar),'-private','-c',name],text=True)
def sections(text, starts):
    lines=text.splitlines(True); chunks=[]
    for start in starts:
        i=next(i for i,l in enumerate(lines) if l.startswith(start))
        j=i+1
        while j<len(lines) and not re.match(r'^  (?:public |private |protected |static \{)',lines[j]):j+=1
        chunks.append(''.join(lines[i:j]).rstrip())
    return '\n\n'.join(chunks)+'\n'

def save(name,text): (OUT/name).write_text(text)
def snippets(text, keys, before=350, after=1000):
    parts=[]
    for key in keys:
        i=text.find(key)
        if i<0: raise ValueError(key)
        a=text.rfind('\n',0,max(0,i-before))+1
        b=text.find('\n',min(len(text),i+after))
        if b<0:b=len(text)
        parts.append(f'// EXCERPT containing {key}; source lines {text[:a].count(chr(10))+1}-{text[:b].count(chr(10))+1}\n'+text[a:b])
    return '\n\n'.join(parts)+'\n'

save('old-item-armor.javap.txt', sections(javap(OLD,'abb'),['  public abb(', '  static {};']))
save('old-armor-material.javap.txt',javap(OLD,'abd'))
save('old-inventory-slots.javap.txt',sections(javap(OLD,'aap'),['  public aap('])+'\n'+sections(javap(OLD,'aaq'),['  public boolean a(add);'])+'\n'+sections(javap(OLD,'yx'),['  public int a();','  public add a(int);']))
save('native-item-armor.javap.txt',sections(javap(NEW,'net.minecraft.item.ItemArmor'),['  public net.minecraft.item.ItemArmor(', '  public net.minecraft.inventory.EntityEquipmentSlot getEquipmentSlot();', '  static {};']))
save('native-armor-material.javap.txt',javap(NEW,'net.minecraft.item.ItemArmor$ArmorMaterial'))
save('native-equipment-slot.javap.txt',javap(NEW,'net.minecraft.inventory.EntityEquipmentSlot'))
save('native-player-equipment.javap.txt',sections(javap(NEW,'net.minecraft.entity.player.EntityPlayer'),['  public net.minecraft.item.ItemStack getItemStackFromSlot(', '  public void setItemStackToSlot(']))
save('native-item-callbacks.javap.txt',sections(javap(NEW,'net.minecraft.item.Item'),['  public boolean isValidArmor(', '  public java.lang.String getArmorTexture(', '  public net.minecraft.client.model.ModelBiped getArmorModel(']))
save('original-asbestos-display.javap.txt',sections(javap(ORIGINAL_MOD,'com.hbm.items.armor.ArmorAsbestos'),['  public int getArmorDisplay(']))
with zipfile.ZipFile(OLD_FORGE) as z:
    mapping=z.read('conf/packaged.srg').decode()
    names=['net/minecraft/item/ItemArmor','net/minecraft/inventory/ContainerPlayer','net/minecraft/entity/player/InventoryPlayer']
    lines=[l for l in mapping.splitlines() if any(n in l for n in names)]
    for p in ['conf/fields.csv','conf/methods.csv']:
        lines += [f'{p}: {l}' for l in z.read(p).decode().splitlines() if any(n in l for n in ['field_77881_a,','field_77879_b,','field_77882_bY,','field_70460_b,','func_78044_b,','func_78046_a,','func_70301_a,','func_70302_i_,'])]
    save('old-obfuscation-mappings.txt','\n'.join(lines)+'\n')
for version,archive,prefix in [('old',OLD_FORGE,'src/main/java/'),('native',NEW_SOURCES,'')]:
    with zipfile.ZipFile(archive) as z:
        path=prefix+'net/minecraftforge/common/ISpecialArmor.java'
        text=z.read(path).decode()
        save(version+'-special-armor.excerpt.java.txt',f'// SOURCE: {archive}!/{path}\n'+text[:text.index('        private static void StandardizeList')])
        path=prefix+'net/minecraftforge/common/ForgeHooks.java'
        text=z.read(path).decode()
        save(version+'-forge-hooks.excerpt.java.txt',f'// SOURCE: {archive}!/{path}\n'+snippets(text,['public static int getTotalArmorValue'],before=0,after=1000))
        path=prefix+'net/minecraftforge/client/ForgeHooksClient.java'
        text=z.read(path).decode()
        save(version+'-forge-client-hooks.excerpt.java.txt',f'// SOURCE: {archive}!/{path}\n'+snippets(text,['public static ModelBiped getArmorModel','public static String getArmorTexture'],before=0,after=600))
        path=prefix+'net/minecraftforge/common/util/EnumHelper.java'
        text=z.read(path).decode()
        save(version+'-enum-helper.excerpt.java.txt',f'// SOURCE: {archive}!/{path}\n'+snippets(text,['public static ArmorMaterial addArmorMaterial'],before=0,after=500))
for version,archive in [('old',OLD_PATCHES),('native',NEW_PATCHES)]:
    with zipfile.ZipFile(archive) as z:
        for path in ['net/minecraft/item/ItemArmor.java.patch','net/minecraft/inventory/ContainerPlayer.java.patch'] + (['net/minecraft/client/renderer/entity/RenderPlayer.java.patch','net/minecraft/client/renderer/entity/RenderBiped.java.patch'] if version=='old' else ['net/minecraft/client/renderer/entity/layers/LayerArmorBase.java.patch']):
            save(version+'-'+Path(path).name+'.txt',f'# SOURCE: {archive}!/{path}\n'+z.read(path).decode())
        for path,keys in [('net/minecraft/item/Item.java.patch',['isValidArmor','getArmorTexture','getArmorModel']),('net/minecraft/entity/player/EntityPlayer.java.patch',['ArmorProperties.'+('ApplyArmor' if version=='old' else 'applyArmor')])]:
            save(version+'-'+Path(path).name+'.excerpts.txt',f'# SOURCE: {archive}!/{path}\n'+snippets(z.read(path).decode(),keys,before=650,after=800))
manifest=[]
for p in [OLD,OLD_FORGE,OLD_PATCHES,NEW,NEW_SOURCES,NEW_PATCHES,ORIGINAL_MOD]:
    manifest.append({'path':str(p),'resolved_path':str(p.resolve()),'sha256':hashlib.sha256(p.read_bytes()).hexdigest()})
save('inputs.sha256.json',json.dumps(manifest,indent=2)+'\n')
print(f'Captured engine evidence in {OUT}')
