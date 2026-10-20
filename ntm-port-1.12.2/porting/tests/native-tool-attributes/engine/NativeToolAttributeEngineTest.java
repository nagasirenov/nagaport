import java.io.File;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import com.google.common.collect.*;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.init.Bootstrap;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.nbt.*;

/** Actual cached engines with test descendants. No fake platform classes. */
public final class NativeToolAttributeEngineTest {
    static int checks;
    static final String DAMAGE="generic.attackDamage", SPEED="generic.attackSpeed", MOVE="generic.movementSpeed";
    static void ok(boolean b,String message) { checks++; if(!b) throw new AssertionError(message); }
    static double amount(Multimap<String,AttributeModifier> map,String key) { ok(map.get(key).size()==1,"one " + key); return map.get(key).iterator().next().getAmount(); }
    static void samePayload(Multimap<String,AttributeModifier> left,Multimap<String,AttributeModifier> right,String label) {
        ok(left.size()==right.size(),label+" cardinality");
        for(Map.Entry<String,AttributeModifier> e:left.entries()) {
            AttributeModifier match=null;
            for(AttributeModifier candidate:right.get(e.getKey())) if(candidate.getID().equals(e.getValue().getID())) match=candidate;
            ok(match!=null&&match.getName().equals(e.getValue().getName())&&match.getAmount()==e.getValue().getAmount()&&match.getOperation()==e.getValue().getOperation(),label+" exact tuple "+e.getKey());
        }
    }
    static class TwoArgTool extends ItemTool { TwoArgTool(ToolMaterial m) { super(m,Collections.emptySet()); } }
    static class FourArgTool extends ItemTool { FourArgTool(ToolMaterial m) { super(0F,0F,m,Collections.emptySet()); } }
    static class ReplacementSword extends ItemSword {
        int hookCalls;
        ReplacementSword() { super(ToolMaterial.IRON); }
        @Override public Multimap<String,AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
            hookCalls++;
            Multimap<String,AttributeModifier> map=HashMultimap.create();
            if(slot!=EntityEquipmentSlot.OFFHAND) {
                map.put(DAMAGE,new AttributeModifier(ATTACK_DAMAGE_MODIFIER,"Tool modifier",75D,0));
                map.put(MOVE,new AttributeModifier(ATTACK_DAMAGE_MODIFIER,"Tool modifier",0.25D,1));
            }
            return map;
        }
    }
    static class ReplacementTool extends ItemTool {
        ReplacementTool() { super(ToolMaterial.IRON,Collections.emptySet()); }
        @Override public Multimap<String,AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
            Multimap<String,AttributeModifier> map=HashMultimap.create();
            if(slot!=EntityEquipmentSlot.OFFHAND) map.put(DAMAGE,new AttributeModifier(ATTACK_DAMAGE_MODIFIER,"Tool modifier",20D,0));
            return map;
        }
    }
    static class SpentCrucibleCounterexample extends ReplacementSword {
        @Override public Multimap<String,AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot,ItemStack stack) {
            return super.getAttributeModifiers(slot,stack);
        }
    }
    static class LegacyPreservedSword extends ItemSword {
        static final UUID EXTRA=UUID.fromString("00000000-0000-0000-0000-000000000123");
        LegacyPreservedSword(ToolMaterial material) { super(material); }
        @Override public Multimap<String,AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
            Multimap<String,AttributeModifier> map=super.getItemAttributeModifiers(slot);
            if(slot==EntityEquipmentSlot.MAINHAND) {
                map.put(DAMAGE,new AttributeModifier(EXTRA,"Unrelated marker",17D,0));
                AttributeModifier legacy=new AttributeModifier(ATTACK_DAMAGE_MODIFIER,"Weapon modifier",(double)(4F+super.getAttackDamage()),0);
                ok(map.remove(DAMAGE,legacy),"same-UUID removal despite different amount"); map.put(DAMAGE,legacy);
            }
            return map;
        }
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        for(Item.ToolMaterial material:Item.ToolMaterial.values()) {
            ItemSword sword=new ItemSword(material); ItemTool two=new TwoArgTool(material),four=new FourArgTool(material);
            for(EntityEquipmentSlot slot:EntityEquipmentSlot.values()) {
                Multimap<String,AttributeModifier> sm=sword.getItemAttributeModifiers(slot), tm=two.getItemAttributeModifiers(slot), fm=four.getItemAttributeModifiers(slot);
                samePayload(tm,fm,"two-arg ItemTool equivalent to (0,0,material,blocks) " + material + "/" + slot);
                if(slot==EntityEquipmentSlot.MAINHAND) {
                    ok(sm.size()==2&&tm.size()==2,"native superclass two payloads");
                    ok(amount(sm,DAMAGE)==(double)(3F+material.getAttackDamage()),"native sword constructor arithmetic");
                    ok(amount(sm,SPEED)==(double)-2.4F,"native sword float widened speed");
                    ok(amount(tm,DAMAGE)==(double)material.getAttackDamage()&&amount(tm,SPEED)==0D,"native tool constructor arithmetic");
                } else ok(sm.isEmpty()&&tm.isEmpty(),"native superclass absent outside mainhand");
            }
        }
        System.out.println("PASS actual native ItemSword = float(3 + material.attack), speed = -2.4000000953674316, MAINHAND only; ItemTool two-arg = four-arg (0F,0F,material,blocks), damage = material.attack, speed = 0.0, MAINHAND only");
        Item.ToolMaterial huge=net.minecraftforge.common.util.EnumHelper.addToolMaterial("CP92_PROBE_HAMMER",3,1,50F,999999996F,0);
        Item.ToolMaterial boundary=net.minecraftforge.common.util.EnumHelper.addToolMaterial("CP92_PROBE_BOUNDARY",3,1,50F,16777214F,0);
        for(Item.ToolMaterial material:new Item.ToolMaterial[]{Item.ToolMaterial.IRON,huge,boundary}) {
            LegacyPreservedSword sword=new LegacyPreservedSword(material);
            ok(sword.getAttackDamage()==material.getAttackDamage(),"actual ItemSword accessor returns raw material, not stored combined modifier");
            Multimap<String,AttributeModifier> corrected=sword.getItemAttributeModifiers(EntityEquipmentSlot.MAINHAND);
            ok(corrected.get(DAMAGE).size()==2,"narrow replacement preserves unrelated damage entry");
            for(AttributeModifier m:corrected.get(DAMAGE)) {
                if(m.getID().equals(LegacyPreservedSword.EXTRA)) ok(m.getAmount()==17D,"unrelated damage UUID unchanged");
                else ok(m.getAmount()==(double)(4F+material.getAttackDamage())&&m.getName().equals("Weapon modifier")&&m.getOperation()==0,"legacy exact superclass tuple");
            }
            ok(amount(corrected,SPEED)==(double)-2.4F,"native attack speed UUID/amount retained");
            if(material==huge||material==boundary) ok((double)(3F+material.getAttackDamage())+1D!=(double)(4F+material.getAttackDamage()),"adding one after rounding is not legacy float arithmetic");
        }
        System.out.println("PASS exact legacy Sword damage restoration uses (double)(4F + super.getAttackDamage()); real accessor is raw material. UUID-equal remove succeeds despite changed amount, retains native attack speed and unrelated damage UUID. Hammer 1.0E9F and 16777214F disprove adding 1 after rounding.");
        Multimap<String,AttributeModifier> sm=new ItemSword(Item.ToolMaterial.IRON).getItemAttributeModifiers(EntityEquipmentSlot.MAINHAND);
        AttributeModifier vanilla=sm.get(DAMAGE).iterator().next();
        ok(vanilla.getID().toString().equals("cb3f55d3-645c-4f38-a497-9c13a33db5cf"),"native attack UUID equals original field_111210_e UUID");
        AttributeModifier attackSpeed=sm.get(SPEED).iterator().next();
        ok(attackSpeed.getID().toString().equals("fa233e1c-4180-4865-b01b-bcce9785aca3")&&attackSpeed.getName().equals("Weapon modifier")&&attackSpeed.getOperation()==0,"native Sword attack-speed exact UUID/name/operation");
        boolean inserted=sm.put(DAMAGE,new AttributeModifier(vanilla.getID(),"Tool modifier",75D,0));
        ok(!inserted&&amount(sm,DAMAGE)==5D,"duplicate UUID does not replace superclass damage");
        System.out.println("PASS counterexample: native IRON sword super map + same UUID explicit damage 75 returns put=false and damage stays 5, not 75");
        for(EntityEquipmentSlot slot:EntityEquipmentSlot.values()) {
            Multimap<String,AttributeModifier> replacement=new ReplacementSword().getItemAttributeModifiers(slot);
            ok(!replacement.containsKey(SPEED),"replacement sword has no accidental speed");
            ok(slot==EntityEquipmentSlot.OFFHAND?replacement.isEmpty():amount(replacement,DAMAGE)==75D,"replacement payload five old slots");
            ok(!new ReplacementTool().getItemAttributeModifiers(slot).containsKey(SPEED),"replacement tool has no zero-valued speed modifier");
        }
        ReplacementSword metadataItem=new ReplacementSword(); ItemStack metadataStack=new ItemStack(metadataItem);
        metadataStack.setItemDamage(2); NBTTagCompound metadata=new NBTTagCompound(); metadata.setInteger("charge",19); metadataStack.setTagCompound(metadata);
        for(EntityEquipmentSlot slot:EntityEquipmentSlot.values()) {
            Multimap<String,AttributeModifier> map=metadataStack.getAttributeModifiers(slot);
            ok(slot==EntityEquipmentSlot.OFFHAND?map.isEmpty():amount(map,DAMAGE)==75D,"real ItemStack dispatch respects old five-slot payload contract");
        }
        ok(metadataStack.getItemDamage()==2&&metadataStack.getTagCompound().getInteger("charge")==19,"ordinary metadata and charge NBT unchanged");
        int beforeNBT=metadataItem.hookCalls;
        NBTTagCompound explicit=SharedMonsterAttributes.writeAttributeModifierToNBT(new AttributeModifier(UUID.fromString("12345678-1234-1234-1234-000000000456"),"Explicit NBT",9D,0));
        explicit.setString("AttributeName",DAMAGE); explicit.setString("Slot","offhand"); NBTTagList list=new NBTTagList(); list.appendTag(explicit); metadata.setTag("AttributeModifiers",list);
        ok(amount(metadataStack.getAttributeModifiers(EntityEquipmentSlot.OFFHAND),DAMAGE)==9D,"explicit native offhand NBT remains authoritative");
        ok(metadataStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).isEmpty(),"NBT slot filter suppresses native hook fallback");
        ok(metadataItem.hookCalls==beforeNBT,"NBT modifiers bypass item hooks");
        System.out.println("PASS actual ItemStack five-old-slot dispatch and no default OFFHAND; metadata/charge NBT unchanged; explicit AttributeModifiers NBT remains authoritative, slot-filtered and bypasses hooks");
        AttributeMap baseline=new AttributeMap(); baseline.registerAttribute(SharedMonsterAttributes.ATTACK_SPEED).setBaseValue(4D);
        baseline.applyAttributeModifiers(new ReplacementSword().getItemAttributeModifiers(EntityEquipmentSlot.MAINHAND));
        ok(baseline.getAttributeInstance(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue()==4D,"replacement preserves default attack speed 4");
        baseline.applyAttributeModifiers(new ItemSword(Item.ToolMaterial.IRON).getItemAttributeModifiers(EntityEquipmentSlot.MAINHAND));
        ok(baseline.getAttributeInstance(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue()==4D+(double)-2.4F,"native super contributes real speed, not compensated");
        SpentCrucibleCounterexample counterexample=new SpentCrucibleCounterexample();
        ok(amount(counterexample.getAttributeModifiers(EntityEquipmentSlot.MAINHAND,new ItemStack(counterexample)),DAMAGE)==75D,"super stack bridge redispatches inherited virtual replacement hook");
        System.out.println("PASS replacement maps contain no ATTACK_SPEED tuple; actual AttributeMap speed remains 4 versus superclass sword 1.5999999046325684; super(slot,stack) revives inherited replacement payload in spent-state counterexample");
        try(URLClassLoader legacy=new URLClassLoader(new URL[]{new File(args[0]).toURI().toURL()},NativeToolAttributeEngineTest.class.getClassLoader())) {
            legacy.loadClass("kl").getMethod("b").invoke(null);
            Class<?> material=legacy.loadClass("adc"), sword=legacy.loadClass("aeh"), tool=legacy.loadClass("acg");
            Constructor<?> toolCtor=tool.getDeclaredConstructor(float.class,material,Set.class); toolCtor.setAccessible(true);
            for(Object m:(Object[])material.getMethod("values").invoke(null)) {
                float ma=(Float)material.getMethod("c").invoke(m);
                Object so=sword.getConstructor(material).newInstance(m), to=toolCtor.newInstance(0F,m,Collections.emptySet());
                Multimap<?,?> swordMap=(Multimap<?,?>)sword.getMethod("k").invoke(so), toolMap=(Multimap<?,?>)tool.getMethod("k").invoke(to);
                ok(swordMap.size()==1&&toolMap.size()==1,"old superclass only damage");
                Object sa=swordMap.values().iterator().next(),ta=toolMap.values().iterator().next();
                ok((Double)sa.getClass().getMethod("d").invoke(sa)==(double)(4F+ma),"old sword constructor arithmetic");
                ok((Double)ta.getClass().getMethod("d").invoke(ta)==(double)ma,"old tool base0 constructor arithmetic");
            }
        }
        ok((double)(4F+999999996F)==1.0E9D&&(double)(3F+999999996F)==1.0E9D,"large hammer float does not exhibit one-point shift");
        System.out.println("PASS actual legacy ItemSword = float(4 + material.attack), ItemTool base0 = material.attack; neither has ATTACK_SPEED. Hammer 999999996F rounds to 1.0E9F, so adding 3F or 4F is equal.");
        System.out.println("PASS "+checks+" bounded actual-engine assertions; test descendants demonstrate candidate policies, not compiled full mod.");
    }
}
