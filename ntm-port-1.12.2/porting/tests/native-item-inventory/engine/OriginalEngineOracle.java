package inventoryengine;

import java.lang.reflect.*;
import java.nio.file.*;
import java.util.Random;

/** Runs real old ItemStack/NBT and name-remapped full original NTM classfiles. */
public final class OriginalEngineOracle {
    static int checks;
    static Class<?> stack, item, compound;
    static Object diamond;
    static Field count;
    static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    static Object stack(int amount) throws Exception {
        return stack.getConstructor(item,int.class,int.class).newInstance(diamond,amount,9);
    }
    static Object tag() throws Exception { return compound.newInstance(); }
    static void put(Object target, String key, Class<?> type, Object value) throws Exception {
        compound.getMethod("a",String.class,type).invoke(target,key,value);
    }
    static Object data(int length) throws Exception {
        Object root=tag();
        put(root,"label",String.class,"inventory oracle \u03bb");
        put(root,"byte",byte.class,(byte)-7);
        put(root,"short",short.class,(short)32760);
        put(root,"int",int.class,12345678);
        put(root,"long",long.class,-1234567890123L);
        put(root,"float",float.class,1.25F);
        put(root,"double",double.class,-2.125D);
        put(root,"ints",int[].class,new int[]{Integer.MIN_VALUE,-1,0,1,Integer.MAX_VALUE});
        byte[] payload=new byte[length];new Random(9917).nextBytes(payload);
        put(root,"payload",byte[].class,payload);
        Object nested=tag();put(nested,"name",String.class,"tag");
        put(root,"nested",Class.forName("dy"),nested);
        return root;
    }
    static void originalNestedConstructors(Field slotsField) throws Exception {
        String[] names={"com.hbm.items.tool.ItemLeadBox$InventoryLeadBox",
                "com.hbm.items.tool.ItemToolBox$InventoryToolBox",
                "com.hbm.items.tool.ItemRebarPlacer$InventoryRebar"};
        int[] capacities={20,24,1};
        Class<?> player=Class.forName("yz"),listType=Class.forName("dq"),nbtBase=Class.forName("dy");
        for(int kind=0;kind<names.length;kind++) {
            // The complete genuine nested classfile supplies its real constructor
            // and size override. Neither its outer gameplay class nor any method
            // body is replaced. A null player is accepted by these constructors.
            Constructor<?> constructor=Class.forName(names[kind]).getConstructor(player,stack);
            for(int ownerCount:new int[]{Integer.MIN_VALUE,-1,0})for(int storedCount:new int[]{-128,-1,0}) {
                Object owner=stack(ownerCount),root=tag(),list=listType.newInstance();
                Object stored=stack(storedCount),storedTag=tag();
                put(storedTag,"nested",String.class,"retained");
                stack.getMethod("d",compound).invoke(stored,storedTag);
                Object record=stack.getMethod("b",compound).invoke(stored,tag());
                put(record,"slot",byte.class,(byte)0);
                listType.getMethod("a",nbtBase).invoke(list,record);
                put(root,"items",nbtBase,list);put(root,"unrelated",String.class,"owner content");
                Object before=compound.getMethod("b").invoke(root);
                stack.getMethod("d",compound).invoke(owner,root);
                Object inventory=constructor.newInstance(null,owner);
                String context=names[kind]+" owner="+ownerCount+" stored="+storedCount;
                check(stack.getMethod("q").invoke(owner)==root,"original constructor owner tag identity "+context);
                check(root.equals(before),"original constructor owner tag content "+context);
                check(compound.getMethod("a",String.class).invoke(root,"items")==list,"original constructor items list identity "+context);
                check(count.getInt(owner)==ownerCount,"original constructor raw owner count "+context);
                Object slots=slotsField.get(inventory),loaded=Array.get(slots,0);
                check(Array.getLength(slots)==capacities[kind],"original constructor actual size "+context);
                check(loaded!=null&&count.getInt(loaded)==storedCount,"original constructor loads nonpositive content "+context);
                check(stack.getMethod("b").invoke(loaded)==diamond,"original constructor loaded item identity "+context);
                check(stack.getMethod("q").invoke(loaded).equals(storedTag),"original constructor loaded NBT "+context);
                for(int slot=1;slot<capacities[kind];slot++)
                    check(Array.get(slots,slot)==null,"original constructor sparse absent slot "+context);
            }
        }
        System.out.println("PASS original complete LeadBox/ToolBox/Rebar constructors: 27 nonpositive owner/content cases; raw NBT identity retained");
    }
    public static void main(String[] args) throws Exception {
        Class.forName("kl").getMethod("b").invoke(null);
        stack=Class.forName("add");item=Class.forName("adb");compound=Class.forName("dh");
        diamond=item.getMethod("d",int.class).invoke(null,264);
        count=stack.getField("b");
        int[] counts={Integer.MIN_VALUE,-65536,-257,-256,-129,-128,-1,0,1,64,127,128,256,Integer.MAX_VALUE};
        for(int amount:counts) {
            Object original=stack(amount),tag=tag();put(tag,"payload",String.class,"kept");
            stack.getMethod("d",compound).invoke(original,tag);
            check(count.getInt(original)==amount,"old raw count "+amount);
            check(stack.getMethod("b").invoke(original)==diamond,"old empty item identity "+amount);
            check((Boolean)stack.getMethod("p").invoke(original),"old empty tag presence "+amount);
            Object copy=stack.getMethod("m").invoke(original);
            check(copy!=original&&count.getInt(copy)==amount,"old copy identity/count "+amount);
            Object copiedTag=stack.getMethod("q").invoke(copy);
            check(copiedTag!=tag&&copiedTag.equals(tag),"old deep copied tag "+amount);
            Object nbt=stack.getMethod("b",compound).invoke(original,tag());
            check((Byte)compound.getMethod("d",String.class).invoke(nbt,"Count")==(byte)amount,"old signed NBT count "+amount);
            Object loaded=stack.getMethod("a",compound).invoke(null,nbt);
            check(loaded!=null&&count.getInt(loaded)==(byte)amount,"old zero/negative NBT load "+amount);
            for(int take:new int[]{Integer.MIN_VALUE,-2,-1,0,1,3,64,Integer.MAX_VALUE}) {
                if(amount<=take)continue;
                Object source=stack(amount);
                Object result=stack.getMethod("a",int.class).invoke(source,take);
                check(result!=source&&count.getInt(result)==take,"old split count "+amount+"/"+take);
                check(count.getInt(source)==amount-take,"old split signed overflow "+amount+"/"+take);
            }
        }
        Class<?> fixture=Class.forName("inventoryengine.OriginalInventoryFixture");
        Class<?> base=fixture.getSuperclass();
        Method setter=base.getMethod("a",int.class,stack),decrement=base.getMethod("a",int.class,int.class),remove=base.getMethod("a_",int.class),dirty=base.getMethod("e");
        Field slotsField=base.getField("slots");
        originalNestedConstructors(slotsField);
        for(int amount:counts) {
            Object inventory=fixture.getConstructor(stack).newInstance(stack(1));
            Object source=stack(amount);
            setter.invoke(inventory,0,source);
            Object slots=slotsField.get(inventory);
            check(count.getInt(source)==Math.min(amount,64),"original setter upper clamp "+amount);
            check(Array.get(slots,0)==(amount==0?null:source),"original exact zero cleanup "+amount);
        }
        for(int amount:counts)for(int take:new int[]{Integer.MIN_VALUE,-2,-1,0,1,3,64,Integer.MAX_VALUE}) {
            Object inventory=fixture.getConstructor(stack).newInstance(stack(1)),source=stack(amount);
            Object slots=slotsField.get(inventory);Array.set(slots,0,source);
            Object result=decrement.invoke(inventory,0,take);
            if(amount>take) {
                check(result!=source&&count.getInt(result)==take,"original full-method split identity/count");
                check(count.getInt(source)==amount-take,"original full-method split source");
                check(Array.get(slots,0)==((amount-take)==0?null:source),"original full-method split zero cleanup");
            } else {
                check(result==source&&count.getInt(result)==amount,"original full-method whole identity/count");
                check(Array.get(slots,0)==null,"original whole removal");
            }
        }
        for(int amount:counts) {
            Object inventory=fixture.getConstructor(stack).newInstance(stack(1)),source=stack(amount);
            Object slots=slotsField.get(inventory);Array.set(slots,0,source);
            check(remove.invoke(inventory,0)==source&&Array.get(slots,0)==null,"original remove identity "+amount);
        }
        Object inventory=fixture.getConstructor(stack).newInstance(stack(1));
        check(decrement.invoke(inventory,0,1)==null,"original absent decrement");
        check(remove.invoke(inventory,0)==null,"original absent remove");
        Object slots=slotsField.get(inventory),negative=stack(-1),zero=stack(0);
        Array.set(slots,0,negative);Array.set(slots,1,zero);dirty.invoke(inventory);
        check(Array.get(slots,0)==negative&&Array.get(slots,1)==null,"original real markDirty exact zero");
        Method checkNbt=base.getMethod("checkNBT",compound);
        check(checkNbt.invoke(inventory,new Object[]{null})==null,"original checkNBT null");
        check(checkNbt.invoke(inventory,tag())==null,"original checkNBT empty");
        Object small=data(20);check(checkNbt.invoke(inventory,small)==small,"original checkNBT under threshold identity");
        Path out=Paths.get(args[0]);Files.createDirectories(out);
        Method compress=Class.forName("du").getMethod("a",compound);
        int exactThresholds=0;
        for(int length=5700;length<=6100;length++) {
            Object root=data(length);
            byte[] compressed=(byte[])compress.invoke(null,root);
            Files.write(out.resolve(length+".nbt.gz"),compressed);
            if(compressed.length==5999||compressed.length==6000) {
                check(checkNbt.invoke(inventory,root)==root,"actual original <=6000 keeps same NBT");
                exactThresholds++;
            }
            if(compressed.length==6001) {
                // A null player is deliberate: reaching its warning call proves
                // the real original >6000 branch, without faking any world/player.
                try { checkNbt.invoke(inventory,root); throw new AssertionError("missing overflow branch"); }
                catch(InvocationTargetException expected) {
                    check(expected.getCause() instanceof NullPointerException,"actual original 6001 enters ejection branch: "+expected.getCause());
                }
                exactThresholds++;
            }
        }
        check(exactThresholds==3,"all exact compressed threshold cases exercised");
        System.out.println("PASS original engine checks="+checks+"; full original ItemInventory decrement/set/remove/markDirty/checkNBT-small invoked");
    }
}
