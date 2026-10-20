#!/usr/bin/env python3
from pathlib import Path
import json,re,subprocess,tempfile
H=Path(__file__).resolve().parent;J=Path('/workspace/shared/nagaport-toolchain/java8');CP='/workspace/shared/nagaport-toolchain/native-classpath.txt'
prefix='import net.minecraft.entity.*; import net.minecraft.entity.player.*; import net.minecraft.entity.monster.EntityZombie; import net.minecraft.item.ItemStack; import net.minecraft.world.World; '
cases=[
 ('class T {Object f(EntityPlayer p){return p.getEquipmentInSlot(0);}}',1),
 ('class T {Object f(EntityLivingBase p){return p.getEquipmentInSlot(4);}}',1),
 ('class T {Object f(EntityZombie p){return p.getEquipmentInSlot(4);}}',1),
 ('class T {Object f(EntityPlayer p){return p.getCurrentArmor(0);}}',1),
 ('class T {Object f(EntityLivingBase p){return p.getCurrentArmor(0);}}',0),
 ('class T {Object f(Object p){return p.getEquipmentInSlot(4);}}',0),
 ('class T {Object f(Missing p){return p.getEquipmentInSlot(4);}}',0),
 ('class T {Object getEquipmentInSlot(int i){return null;} Object f(T p){return p.getEquipmentInSlot(4);}}',0),
 ('class T extends EntityZombie {T(){super((World)null);} public ItemStack getEquipmentInSlot(int i){return null;} Object f(){return this.getEquipmentInSlot(4);}}',0),
 ('class T extends EntityZombie {T(){super((World)null);} Object f(){return this.getEquipmentInSlot(4);}}',1),
 ('class T extends EntityZombie {T(){super((World)null);} Object f(){return super.getEquipmentInSlot(4);}}',0),
 ('class T {Object f(){return EntityPlayer.getCurrentArmor(0);}}',0),
 ('class T {Object f(EntityPlayer p){return ((EntityLivingBase)p).getEquipmentInSlot(4);}}',1),
 ('class T<E extends EntityPlayer> {Object f(E p){return p.getCurrentArmor(0);}}',0),
 ('class T {Object f(EntityPlayer p){String s="😀";return p.getCurrentArmor(0);}}',1),
 ('class T {Object f(EntityPlayer p){return p.\\u0067etCurrentArmor(0);}}',0),
 ('class T {Object f(EntityPlayer p,int com){return p.getCurrentArmor(0);}}',0),
 ('class T<com> {Object f(EntityPlayer p){return p.getCurrentArmor(0);}}',0),
 ('class T extends EntityZombie {T(){super((World)null);} public ItemStack getEquipmentInSlot(Object... x){return null;} Object f(){return this.getEquipmentInSlot(4);}}',0),
 ('class T extends EntityZombie {T(){super((World)null);} public static ItemStack getEquipmentInSlot(int i){return null;} Object f(){return this.getEquipmentInSlot(4);}}',0),
 ('abstract class T extends EntityZombie implements EquipShadow {T(){super((World)null);} Object f(){return this.getEquipmentInSlot(4);}} interface EquipShadow {ItemStack getEquipmentInSlot(int i);}',0),
 ('class T {Object f(EntityPlayer p){return p.getEquipmentInSlot(4,0);}}',0),
]
with tempfile.TemporaryDirectory(prefix='equipment-owner-fixtures-') as td:
 d=Path(td);classes=d/'classes';classes.mkdir();paths=[]
 subprocess.run([str(J/'bin/javac'),'-cp',str(J/'lib/tools.jar'),'-d',str(classes),str(H/'EquipmentReadScanner.java')],check=True)
 for i,(source,_) in enumerate(cases):
  path=d/('T'+str(i)+'.java');path.write_text(prefix+re.sub(r'\bT\b','T'+str(i),source));paths.append(path)
 inputs=d/'inputs';inputs.write_text('\n'.join(map(str,paths))+'\n')
 result=subprocess.run([str(J/'bin/java'),'-cp',str(classes)+':'+str(J/'lib/tools.jar'),'EquipmentReadScanner',str(inputs),CP],capture_output=True,text=True,check=True)
 rows=list(map(json.loads,result.stdout.splitlines()))
 for i,(src,count) in enumerate(cases):assert sum(row['accept'] for row in rows if row['path']==str(paths[i]))==count,(i,src)
print('PASS 22 real javac ownership fixtures: player/mob/base distinction, owned overloads and overrides, interfaces, static/super/unknown/generic exclusions, Unicode positions, and package shadow rejection')
