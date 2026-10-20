#!/usr/bin/env python3
"""Attribute original/native callback contract to supplied class bytes and SRG mappings."""
import argparse,hashlib,json,pathlib,re,subprocess,zipfile
p=argparse.ArgumentParser();p.add_argument('--out',type=pathlib.Path,required=True);p.add_argument('--java-home',default='/workspace/shared/nagaport-toolchain/java8');p.add_argument('--classpath-file',default='/tmp/nagaport-ae2-native-classpath.txt');p.add_argument('--legacy',default='/tmp/nagaport-legacy-client.jar');p.add_argument('--srg',default='/tmp/nagaport1710-srg.zip');a=p.parse_args();a.out.mkdir(parents=True,exist_ok=True);cp=pathlib.Path(a.classpath_file).read_text().strip();j=pathlib.Path(a.java_home)/'bin';sha=lambda b:hashlib.sha256(b).hexdigest();provenance={};outputs=[]
def capture(name,classpath,signature):
 data=subprocess.check_output([str(j/'javap'),'-classpath',classpath,'-c','-p',name],text=True);start=data.index('  '+signature);end=re.search(r'\n  (?:public|protected|private|static) ',data[start+3:]);end=start+3+end.start() if end else len(data);method=data[start:end];outputs.append('CLASS '+name+'\n'+method+'\n')
 entry=name.replace('.','/')+'.class'
 for file in classpath.split(':'):
  path=pathlib.Path(file)
  if not path.is_file():continue
  try:
   with zipfile.ZipFile(path) as z:
    if entry not in z.namelist():continue
    provenance[name]={'archive':str(path),'archive_sha256':sha(path.read_bytes()),'class_entry':entry,'class_sha256':sha(z.read(entry))};break
  except zipfile.BadZipFile:pass
 else:raise AssertionError('No real class provider '+name)
 return method
old=capture('adb',a.legacy,'public void a(add, yz, java.util.List, boolean);');native=capture('net.minecraft.item.Item',cp,'public void addInformation(net.minecraft.item.ItemStack, net.minecraft.world.World, java.util.List<java.lang.String>, net.minecraft.client.util.ITooltipFlag);');assert '0: return' in old and '0: return' in native
oldstack=capture('add',a.legacy,'public java.util.List a(yz, boolean);');nativestack=capture('net.minecraft.item.ItemStack',cp,'public java.util.List<java.lang.String> getTooltip(net.minecraft.entity.player.EntityPlayer, net.minecraft.client.util.ITooltipFlag);');assert 'adb.a:(Ladd;Lyz;Ljava/util/List;Z)V' in oldstack;assert 'Item.addInformation:(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/client/util/ITooltipFlag;)V' in nativestack;assert 'EntityPlayer.world:Lnet/minecraft/world/World;' in nativestack
with zipfile.ZipFile(a.srg) as z:
 mappings=z.read('joined.srg').decode();lines=[s for s in mappings.splitlines() if s.endswith('net/minecraft/item/Item') or 'net/minecraft/item/Item/func_77624_a ' in s or 'net/minecraft/item/ItemStack/func_82840_a ' in s];assert len(lines)==3
provenance['mapping']={'archive_sha256':sha(pathlib.Path(a.srg).read_bytes()),'joined_srg_sha256':sha(mappings.encode()),'exact_lines':lines}
(a.out/'engine-provenance.json').write_text(json.dumps(provenance,indent=2)+'\n');(a.out/'engine-callbacks.javap.txt').write_text('\n'.join(outputs));print('PASS original/native Item and ItemStack callback descriptors, empty base bodies, nullable world forwarding and exact SRG attribution')
