package inventoryproduction;

import java.lang.reflect.*;

/** Reflection only selects genuine old/native engine APIs; no reflective writes. */
public final class DamageFacts {
    public static void main(String[] args)throws Exception {
        boolean old=args[0].equals("old");
        Class.forName(old?"kl":"net.minecraft.init.Bootstrap").getMethod(old?"b":"register").invoke(null);
        Class<?> item=Class.forName(old?"adb":"net.minecraft.item.Item"),
            stack=Class.forName(old?"add":"net.minecraft.item.ItemStack"),
            nbt=Class.forName(old?"dh":"net.minecraft.nbt.NBTTagCompound");
        Object diamond=item.getMethod(old?"d":"getItemById",int.class).invoke(null,264);
        Constructor<?> ctor=stack.getConstructor(item,int.class,int.class);
        Method getItem=stack.getMethod(old?"b":"getItem"),getDamage=stack.getMethod(old?"k":"getItemDamage"),
            write=stack.getMethod(old?"b":"writeToNBT",nbt),
            getShort=nbt.getMethod(old?"e":"getShort",String.class),
            getCount=old?null:stack.getMethod("getCount"),
            empty=old?null:stack.getMethod("isEmpty"),
            copy=stack.getMethod(old?"m":"copy");
        Field raw=stack.getDeclaredField(old?"b":"stackSize");raw.setAccessible(true);
        for(int damage:new int[]{Integer.MIN_VALUE,-65536,-32768,-1,0,32767,32768,65534,65535,65536,Integer.MAX_VALUE}) {
            Object original=ctor.newInstance(diamond,7,damage);
            Object saved=write.invoke(original,nbt.newInstance());
            Object loaded=old?stack.getMethod("a",nbt).invoke(null,saved):stack.getConstructor(nbt).newInstance(saved);
            Object duplicate=copy.invoke(original);
            // Model the original pre-depletion constructor arguments with genuine
            // constructors. This is not a spawned-world assertion.
            Object originalStyleDrop=ctor.newInstance(diamond,7,(Integer)getDamage.invoke(original));
            System.out.println("DAMAGE,"+(old?"old":"native")+","+damage+
                ","+getDamage.invoke(original)+","+(getItem.invoke(original)==diamond)+
                ","+raw.getInt(original)+","+(old?raw.getInt(original):getCount.invoke(original))+
                ","+(old?false:empty.invoke(original))+","+getShort.invoke(saved,"Damage")+
                ","+getDamage.invoke(loaded)+","+(getItem.invoke(loaded)==diamond)+
                ","+getDamage.invoke(duplicate)+","+(getItem.invoke(duplicate)==diamond)+
                ","+(getItem.invoke(originalStyleDrop)==diamond)+","+(old?false:empty.invoke(originalStyleDrop)));
        }
    }
}
