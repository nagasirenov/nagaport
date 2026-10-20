from pathlib import Path
import re, subprocess, zipfile, hashlib, json
out=Path(__file__).resolve().parent; root=out.parents[3]; jp='/workspace/shared/nagaport-toolchain/java8/bin/javap'; original='/workspace/shared/nagaport/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar'
# Recreate temporary raw inputs, then retain only concise excerpts. No compile, network, or repository writes.
new=next(x for x in Path('/workspace/shared/nagaport-toolchain/native-classpath.txt').read_text().strip().split(':') if '/forgeBin-' in x)
for version, jar, names in [('old','/tmp/nagaport-legacy-client.jar',['adb','add','sv','yz','abb','tl']),('native',new,['net.minecraft.item.Item','net.minecraft.item.ItemStack','net.minecraft.entity.EntityLivingBase','net.minecraft.item.ItemArmor','net.minecraft.entity.ai.attributes.AbstractAttributeMap']),('original-mod',original,['com.hbm.items.armor.'+x for x in ['ArmorDiesel','ArmorDesh','ArmorLiquidator','ArmorNCRPA','ArmorEnvsuit','ArmorDNT']])]:
 for name in names:
  text=subprocess.check_output([jp,'-classpath',jar,'-private','-c',name],text=True)
  (out/(version+'-'+name.rsplit('.',1)[-1]+'.javap.txt')).write_text(text)
for version, archive in [('old','/tmp/nagaport-forge1614-patches.zip'),('native','/workspace/shared/nagaport-toolchain/gradle-user-home/caches/minecraft/net/minecraftforge/forge/1.12.2-14.23.5.2768/userdev/patches.zip')]:
 with zipfile.ZipFile(archive) as z:
  for name in ['Item','ItemStack','ItemArmor','EntityLivingBase']:
   path='net/minecraft/'+('entity' if name=='EntityLivingBase' else 'item')+'/'+name+'.java.patch'
   (out/(version+'-'+name+'.java.patch.txt')).write_text('# SOURCE '+archive+'!/'+path+'\n'+z.read(path).decode())
with zipfile.ZipFile('/tmp/nagaport-forge1614-userdev.jar') as z:
 srg=z.read('conf/packaged.srg').decode();csv=z.read('conf/methods.csv').decode()
 (out/'old-mappings.txt').write_text(srg+'\n'+csv)
def method(file, starts, end=None):
 lines=(out/file).read_text().splitlines(); chunks=[]
 for start in starts:
  i=next(i for i,l in enumerate(lines) if l.startswith(start));j=i+1
  while j<len(lines) and not re.match(r'^  (?:public |private |protected |static \{)', lines[j]): j+=1
  if end:
   j=next(k for k in range(i,j) if end in lines[k])
  chunks.append(f'// {file}, javap output lines {i+1}-{j}\n'+'\n'.join(lines[i:j]))
 return '\n\n'.join(chunks)+'\n'
def excerpt(file,key,before,after):
 ls=(out/file).read_text().splitlines();i=next(i for i,l in enumerate(ls) if key in l);a=max(0,i-before);b=min(len(ls),i+after)
 return f'// {file}, source output lines {a+1}-{b}\n'+'\n'.join(ls[a:b])+'\n'
parts=[]
for f,starts in [('old-adb.javap.txt',['  public com.google.common.collect.Multimap k();']),('old-add.javap.txt',['  public com.google.common.collect.Multimap D();']),('old-yz.javap.txt',['  public add q(int);']),('native-Item.javap.txt',['  public com.google.common.collect.Multimap<java.lang.String, net.minecraft.entity.ai.attributes.AttributeModifier> getItemAttributeModifiers(', '  public com.google.common.collect.Multimap<java.lang.String, net.minecraft.entity.ai.attributes.AttributeModifier> getAttributeModifiers(']),('native-ItemStack.javap.txt',['  public com.google.common.collect.Multimap<java.lang.String, net.minecraft.entity.ai.attributes.AttributeModifier> getAttributeModifiers(']),('native-ItemArmor.javap.txt',['  public com.google.common.collect.Multimap<java.lang.String, net.minecraft.entity.ai.attributes.AttributeModifier> getItemAttributeModifiers(']),('native-AbstractAttributeMap.javap.txt',['  public void removeAttributeModifiers(', '  public void applyAttributeModifiers('])]:
 parts.append(method(f,starts))
parts.append(method('old-sv.javap.txt',['  public void h();'],'     198: aload_0'))
parts.append(method('native-EntityLivingBase.javap.txt',['  public void onUpdate();'],'     413: aload_0'))
(out/'engine-methods.excerpts.txt').write_text('\n'.join(parts))
parts=[]
for f,k,b,a in [('old-Item.java.patch.txt','public Multimap func_111205_h',5,17),('old-ItemStack.java.patch.txt','object = this.func_77973_b().getAttributeModifiers',6,4),('old-EntityLivingBase.java.patch.txt','public void func_70071_h_()',2,8),('native-Item.java.patch.txt','public Multimap<String, AttributeModifier> func_111205_h',2,14),('native-ItemStack.java.patch.txt','multimap = this.func_77973_b().getAttributeModifiers',6,4),('native-EntityLivingBase.java.patch.txt','public void func_70071_h_()',2,20)]:
 parts.append(excerpt(f,k,b,a))
(out/'forge-hooks.excerpts.txt').write_text('\n'.join(parts))
parts=[]
for cls in ['ArmorDiesel','ArmorDesh','ArmorLiquidator']:
 parts.append(method('original-mod-'+cls+'.javap.txt',['  public com.google.common.collect.Multimap func_111205_h();']))
for cls in ['ArmorNCRPA','ArmorEnvsuit','ArmorDNT']:
 parts.append(excerpt('original-mod-'+cls+'.javap.txt','getAttributeModifiers:',4,32))
(out/'original-custom-maps.excerpts.txt').write_text('\n'.join(parts))
classes=['ArmorNCRPA','ArmorEnvsuit','ArmorDNT','ArmorFSBPowered','ArmorFSB','ItemModWD40','ItemModInsert','ItemModHealth','ItemModIron','ItemModServos','ItemModV1','ItemArmorMod']
text=[]
for cls in classes:
 s=subprocess.check_output([jp,'-classpath',original,'-private','com.hbm.items.armor.'+cls],text=True)
 lines=s.splitlines();text.append(lines[1]);text.extend(l for l in lines if 'getAttributeModifiers' in l or 'func_111205_h' in l)
 p=root/'src/main/java/com/hbm/items/armor'/f'{cls}.java';ss=p.read_text(); declarations=[l.strip() for l in ss.splitlines() if re.search(r'\b(public|protected|private)\b.*\b(getAttributeModifiers|getItemAttributeModifiers)\s*\(',l)]
 text.append('  Native source hook declarations: '+(' | '.join(declarations) if declarations else 'none')+'; sha256 '+hashlib.sha256(p.read_bytes()).hexdigest())
text.append('All twelve original declarations above have no getAttributeModifiers/func_111205_h declarations; the filtered outputs would include any such declaration.')
s=subprocess.check_output([jp,'-classpath','/tmp/nagaport-legacy-client.jar','-private','abb'],text=True);text.append('\nComplete legacy ItemArmor declaration (abb):\n'+s)
(out/'owner-hierarchies.txt').write_text('\n'.join(text)+'\n')
# Keep only relevant mapping names, exact raw records.
mp=(out/'old-mappings.txt').read_text().splitlines(); keys=['func_111205_h','func_111283_C','func_70071_h_','func_71124_b','func_111147_b','func_111148_a','getItemAttributeModifiers','getAttributeModifiers','getEquipmentInSlot','applyAttributeModifiers','removeAttributeModifiers']
(out/'old-mappings.txt').write_text('\n'.join(l for l in mp if any(k in l for k in keys))+'\n')
paths=['/tmp/nagaport-legacy-client.jar','/tmp/nagaport-forge1614-userdev.jar','/tmp/nagaport-forge1614-patches.zip',new,original,'/workspace/shared/nagaport-toolchain/gradle-user-home/caches/minecraft/net/minecraftforge/forge/1.12.2-14.23.5.2768/userdev/patches.zip']
manifest=[{'path':x,'resolved_path':str(Path(x).resolve()),'sha256':hashlib.sha256(Path(x).read_bytes()).hexdigest()} for x in paths]
(out/'inputs.sha256.json').write_text(json.dumps(manifest,indent=2)+'\n')
# Remove verbose raw disassemblies/patches created solely by this audit.
for p in out.iterdir():
 if p.name.endswith('.javap.txt') or p.name.endswith('.java.patch.txt'):p.unlink()
