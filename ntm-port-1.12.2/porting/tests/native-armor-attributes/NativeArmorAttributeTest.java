import java.util.*;import com.google.common.collect.*;import net.minecraft.item.*;import net.minecraft.entity.*;import net.minecraft.entity.ai.attributes.*;import net.minecraft.inventory.EntityEquipmentSlot;import com.hbm.util.ArmorSlotOrder;
public class NativeArmorAttributeTest extends ProductionAttributeProjections {
 static int assertions;
 static void check(boolean b,String m){assertions++;if(!b)throw new AssertionError(m);}
 static void eq(double a,double b,String m){check(Double.doubleToLongBits(a)==Double.doubleToLongBits(b),m+": "+a+" != "+b);}
 static String armor=SharedMonsterAttributes.ARMOR.getName(),tough=SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(),speedKey=SharedMonsterAttributes.MOVEMENT_SPEED.getName(),healthKey=SharedMonsterAttributes.MAX_HEALTH.getName(),knock=SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(),attack=SharedMonsterAttributes.ATTACK_DAMAGE.getName();
 static AttributeModifier only(Multimap m,String key){Collection<AttributeModifier> c=m.get(key);check(c.size()==1,"one modifier "+key+" got "+c.size());return c.iterator().next();}
 static void modifier(Multimap m,String key,UUID uuid,String name,double amount,int op){AttributeModifier a=only(m,key);check(a.getID().equals(uuid),"UUID "+key);check(a.getName().equals(name),"name "+key);eq(a.getAmount(),amount,"amount "+key);check(a.getOperation()==op,"operation "+key);}
 static void same(Multimap a,Multimap b,String why){check(a.keySet().equals(b.keySet()),why+" keys");for(Object k:a.keySet()){Collection<AttributeModifier>x=a.get(k),y=b.get(k);check(x.size()==y.size(),why+" size");for(AttributeModifier v:x){AttributeModifier w=null;for(AttributeModifier z:y)if(z.getID().equals(v.getID()))w=z;check(w!=null,why+" uuid");eq(v.getAmount(),w.getAmount(),why+" amount");check(v.getOperation()==w.getOperation(),why+" op");check(v.getName().equals(w.getName()),why+" name");}}}
 static AbstractAttributeMap attributes(){AbstractAttributeMap m=new AttributeMap();for(IAttribute a:new IAttribute[]{SharedMonsterAttributes.ARMOR,SharedMonsterAttributes.ARMOR_TOUGHNESS,SharedMonsterAttributes.MOVEMENT_SPEED,SharedMonsterAttributes.MAX_HEALTH,SharedMonsterAttributes.KNOCKBACK_RESISTANCE,SharedMonsterAttributes.ATTACK_DAMAGE})m.registerAttribute(a);return m;}
 static void armorHooks(){
  for(ItemArmor.ArmorMaterial material:ItemArmor.ArmorMaterial.values())for(int i=0;i<4;i++)for(int kind=0;kind<3;kind++){
   ItemArmor p=kind==0?new ArmorDiesel(material,i):kind==1?new ArmorDesh(material,i):new ArmorLiquidator(material,i);
   ItemArmor base=new ItemArmor(material,0,ArmorSlotOrder.fromIndex(i));ItemStack stack=new ItemStack(p);
   for(EntityEquipmentSlot slot:EntityEquipmentSlot.values()){
    Multimap direct=p.getItemAttributeModifiers(slot),bridge=((Item)p).getAttributeModifiers(slot,stack),throughStack=stack.getAttributeModifiers(slot);
    same(direct,bridge,"Item virtual bridge");same(direct,throughStack,"ItemStack dispatch");
    Multimap expected=base.getItemAttributeModifiers(slot);UUID uuid=ArmorModHandler.fixedUUIDs[i];int count=expected.size();
    if(slot!=EntityEquipmentSlot.OFFHAND){
     if(kind!=1){modifier(direct,knock,uuid,"Armor modifier",kind==0?0.25D:100D,kind==0?1:0);count++;}
     if(kind!=0){modifier(direct,speedKey,uuid,"Armor modifier",kind==1?-0.025D:-0.1D,1);count++;}
    }
    check(direct.size()==count,"only native base plus legacy custom payload at "+slot);
    for(Object key:expected.keySet())same(copyKey(direct,(String)key),copyKey(expected,(String)key),"native protection baseline");
    if(slot==p.armorType){eq(only(direct,armor).getAmount(),p.damageReduceAmount,"worn armor");eq(only(direct,tough).getAmount(),p.toughness,"worn toughness");}
    else check(!direct.containsKey(armor)&&!direct.containsKey(tough),"no protection wrong slot");
   }
  }
 }
 static Multimap copyKey(Multimap m,String key){Multimap r=HashMultimap.create();r.putAll(key,m.get(key));return r;}
 static final UUID markerID=UUID.fromString("d42f4037-008f-4143-8ccd-55d471a5bc30");
 static EntityEquipmentSlot seen;static Multimap marker(EntityEquipmentSlot slot){seen=slot;Multimap m=HashMultimap.create();m.put(attack,new AttributeModifier(markerID,"virtual marker",0.125D,0));return m;}
 static ItemArmorMod marked(int n){switch(n){case 0:return new ItemModWD40(){public Multimap getItemAttributeModifiers(EntityEquipmentSlot s){return marker(s);}};case 1:return new ItemModIron(){public Multimap getItemAttributeModifiers(EntityEquipmentSlot s){return marker(s);}};case 2:return new ItemModServos(){public Multimap getItemAttributeModifiers(EntityEquipmentSlot s){return marker(s);}};case 3:return new ItemModInsert(0.8F){public Multimap getItemAttributeModifiers(EntityEquipmentSlot s){return marker(s);}};case 4:return new ItemModV1(){public Multimap getItemAttributeModifiers(EntityEquipmentSlot s){return marker(s);}};default:return new ItemModHealth(3){public Multimap getItemAttributeModifiers(EntityEquipmentSlot s){return marker(s);}};}}
 static void originalParity(){
  check(ORIGINAL.length==16,"actual original payload tuple count");
  for(String[] row:ORIGINAL){int i=Integer.parseInt(row[1]);ItemArmor p=row[0].equals("ArmorDiesel")?new ArmorDiesel(ItemArmor.ArmorMaterial.IRON,i):row[0].equals("ArmorDesh")?new ArmorDesh(ItemArmor.ArmorMaterial.IRON,i):new ArmorLiquidator(ItemArmor.ArmorMaterial.IRON,i);for(EntityEquipmentSlot slot:EntityEquipmentSlot.values())if(slot!=EntityEquipmentSlot.OFFHAND)modifier(new ItemStack(p).getAttributeModifiers(slot),row[2],UUID.fromString(row[3]),row[4],Double.parseDouble(row[5]),Integer.parseInt(row[6]));}
 }
 static void speedVirtual(){for(int kind=0;kind<3;kind++){
  ItemArmor p=kind==0?new ArmorNCRPA(){public Multimap getItemAttributeModifiers(EntityEquipmentSlot s){Multimap a=super.getItemAttributeModifiers(s);a.putAll(marker(s));return a;}}:kind==1?new ArmorEnvsuit(){public Multimap getItemAttributeModifiers(EntityEquipmentSlot s){Multimap a=super.getItemAttributeModifiers(s);a.putAll(marker(s));return a;}}:new ArmorDNT(){public Multimap getItemAttributeModifiers(EntityEquipmentSlot s){Multimap a=super.getItemAttributeModifiers(s);a.putAll(marker(s));return a;}};
  ModItems.ncrpa_plate=kind==0?p:null;ModItems.envsuit_plate=kind==1?p:null;ModItems.dns_plate=kind==2?p:null;AbstractAttributeMap m=attributes();SpeedContext c=new SpeedContext(m);c.fullSet=true;for(boolean sprint:new boolean[]{true,false,true,false}){c.sprint=sprint;seen=null;tick(p,c,new ItemStack(p));check(seen==EntityEquipmentSlot.MAINHAND,"speed slice retains virtual MAINHAND bridge");check((m.getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE).getModifier(markerID)!=null)==sprint,"speed virtual marker toggle");}
 }}
 static void mods(){
  ItemModServos normal=new ItemModServos(),desh=new ItemModServos(),other=new ItemModServos();ModItems.servo_set=normal;ModItems.servo_set_desh=desh;
  for(int i=0;i<4;i++){
   ItemStack stack=new ItemStack(new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND,0,ArmorSlotOrder.fromIndex(i)));UUID id=ArmorModHandler.UUIDs[i];
   Multimap a=new ItemModWD40().getModifiers(stack);check(a.size()==1,"WD40 size");modifier(a,healthKey,id,"NTM Armor Mod Health",4,0);
   a=new ItemModIron().getModifiers(stack);check(a.size()==1,"iron size");modifier(a,knock,id,"NTM Armor Mod Knockback",0.5,0);
   for(float value:new float[]{0,0.5F,4,16,100}){a=new ItemModHealth(value).getModifiers(stack);check(a.size()==1,"health size");modifier(a,healthKey,id,"NTM Armor Mod Health",value,0);}
   for(float value:new float[]{0,0.5F,0.8F,1,1.25F}){a=new ItemModInsert(value).getModifiers(stack);if(value==1)check(a==null,"neutral insert remains null");else{check(a.size()==1,"insert size");modifier(a,speedKey,id,"NTM Armor Mod Speed",-1F+value,2);}}
   a=new ItemModV1().getModifiers(stack);check(a.size()==1,"V1 size");modifier(a,speedKey,UUID.fromString("1d11e63e-28c4-4e14-b09f-fe0bd1be708f"),"V1 SPEED",0.5,2);
   for(ItemModServos servo:new ItemModServos[]{normal,desh,other}){a=servo.getModifiers(stack);if((i!=1&&i!=2)||servo==other)check(a.isEmpty(),"servo branch empty");else{check(a.size()==1,"servo size");modifier(a,i==1?attack:speedKey,id,"NTM Armor Mod Servos",i==1?(servo==normal?0.5:1.5):(servo==normal?0.25:0.5),2);}}
   for(int n=0;n<6;n++){seen=null;a=marked(n).getModifiers(stack);check(seen==EntityEquipmentSlot.MAINHAND,"manual map preserves virtual MAINHAND hook");check(a.containsEntry(attack,new AttributeModifier(markerID,"unused",0,0)),"virtual marker retained");check(!a.containsKey(armor)&&!a.containsKey(tough),"manual map no native armor");}
  }
 }
 static void tick(ItemArmor p,SpeedContext c,ItemStack s){if(p instanceof ArmorNCRPA)((ArmorNCRPA)p).speedSlice(c,s);else if(p instanceof ArmorEnvsuit)((ArmorEnvsuit)p).speedSlice(c,s);else ((ArmorDNT)p).speedSlice(c,s);}
 static void sprint(){UUID uuid=UUID.fromString("6ab858ba-d712-485c-bae9-e5e765fc555a");for(int kind=0;kind<3;kind++){
  ItemArmor p=kind==0?new ArmorNCRPA():kind==1?new ArmorEnvsuit():new ArmorDNT();ModItems.ncrpa_plate=kind==0?p:null;ModItems.envsuit_plate=kind==1?p:null;ModItems.dns_plate=kind==2?p:null;ItemStack s=new ItemStack(p);AbstractAttributeMap m=attributes();Multimap worn=s.getAttributeModifiers(EntityEquipmentSlot.CHEST);m.applyAttributeModifiers(worn);double defense=m.getAttributeInstance(SharedMonsterAttributes.ARMOR).getAttributeValue(),toughness=m.getAttributeInstance(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue();SpeedContext c=new SpeedContext(m);
  for(boolean full:new boolean[]{false,true})for(boolean sprint:new boolean[]{false,true,true,false,true,false}){c.fullSet=full;c.sprint=sprint;tick(p,c,s);AttributeModifier speed=m.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).getModifier(uuid);boolean active=sprint&&(kind!=1||full);check((speed!=null)==active,"sprint/full-set toggle "+kind);if(active){eq(speed.getAmount(),kind==2?0.25:0.1,"sprint payload");check(speed.getOperation()==0,"sprint operation");check(speed.getName().equals(kind==0?"NCRPA SPEED":kind==1?"SQUIRREL SPEED":"DNT SPEED"),"sprint name");}eq(m.getAttributeInstance(SharedMonsterAttributes.ARMOR).getAttributeValue(),defense,"sprint keeps defense");eq(m.getAttributeInstance(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue(),toughness,"sprint keeps toughness");}
  ItemArmor different=kind==0?new ArmorNCRPA():kind==1?new ArmorEnvsuit():new ArmorDNT();c.sprint=true;c.fullSet=true;tick(different,c,new ItemStack(different));check(m.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).getModifier(uuid)==null,"item identity early return");
 }}
 public static void main(String[]args){net.minecraft.init.Bootstrap.register();armorHooks();originalParity();mods();sprint();speedVirtual();System.out.println("PASS native armor attributes: "+assertions+" assertions; 9 complete method projections and 3 exact speed slices, actual Item/ItemStack/native attribute maps");}
}
