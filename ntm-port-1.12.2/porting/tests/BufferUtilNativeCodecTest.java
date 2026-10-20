import com.hbm.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.GZIPOutputStream;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.Vec3d;

/** Entire production utility, real native Netty/Minecraft/Forge. No platform substitutes. */
public final class BufferUtilNativeCodecTest {
    private static int checks;
    // Independently encoded named NBT roots and RFC1952 GZIP bytes, not output captured from BufferUtil.
    private static final String EMPTY_NBT = "00181f8b0800000000000000e36260600000783ff94e04000000";
    private static final String INT_NBT = "00201f8b0800000000000000e36260606660ac103209ab6000003b9cd7030c000000";
    private static void check(boolean value, String label) { checks++; if(!value) throw new AssertionError(label); }
    private static byte[] hex(String value) { byte[] a = new byte[value.length()/2]; for(int i=0;i<a.length;i++) a[i]=(byte)Integer.parseInt(value.substring(i*2,i*2+2),16); return a; }
    private static String hex(byte[] a) { StringBuilder s=new StringBuilder(); for(byte b:a) s.append(String.format("%02x",b & 255)); return s.toString(); }
    private static byte[] written(ByteBuf b) { byte[] a=new byte[b.writerIndex()]; b.getBytes(0,a); return a; }
    private static ByteBuf input(String expected) { return Unpooled.wrappedBuffer(hex(expected)); }
    private static ByteBuf output() { return Unpooled.buffer(); }
    private static void bytes(ByteBuf b,String expected,String label) { check(Arrays.equals(written(b),hex(expected)),label+": "+hex(written(b))); }
    private static void indexes(ByteBuf b,int reader,int writer,String label) { check(b.readerIndex()==reader && b.writerIndex()==writer,label+" indices "+b.readerIndex()+"/"+b.writerIndex()); }
    private static Throwable fails(Class<? extends Throwable> kind,Runnable action,String label) {
        try { action.run(); } catch(Throwable failure) { check(kind.isInstance(failure),label+": "+failure); return failure; }
        throw new AssertionError(label+": no exception");
    }
    private static String stderr(Runnable action) throws Exception {
        PrintStream before=System.err; ByteArrayOutputStream captured=new ByteArrayOutputStream();
        try(PrintStream sink=new PrintStream(captured,true,"UTF-8")) { System.setErr(sink); action.run(); } finally { System.setErr(before); }
        return captured.toString("UTF-8");
    }
    private static NBTTagCompound integerTag() { NBTTagCompound n=new NBTTagCompound(); n.setInteger("x",0x12345678); return n; }
    private static byte[] gzip(byte[] raw) throws Exception {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(GZIPOutputStream gzip=new GZIPOutputStream(bytes)) { gzip.write(raw); } return bytes.toByteArray();
    }
    private static ByteBuf framed(byte[] data) { ByteBuf b=output(); b.writeShort(data.length); b.writeBytes(data); return b; }
    private static void strings() {
        ByteBuf b=output(); BufferUtil.writeString(b,null); BufferUtil.writeString(b,""); BufferUtil.writeString(b,"A\u00e9\ud83d\ude00");
        bytes(b,"ffffffff000000000000000741c3a9f09f9880","UTF-8 byte length, null and empty independent bytes");
        check(BufferUtil.readString(b)==null,"null string"); check("".equals(BufferUtil.readString(b)),"empty string"); check("A\u00e9\ud83d\ude00".equals(BufferUtil.readString(b)),"unicode roundtrip"); indexes(b,19,19,"string exact consumption"); b.release();
        for(String negative:new String[]{"ffffffff","fffffffe","80000000"}) { b=input(negative+"55"); check(BufferUtil.readString(b)==null,"all negative string lengths return null"); indexes(b,4,5,"negative string leaves suffix"); b.release(); }
        b=output(); BufferUtil.writeString(b,"x\ud800y"); bytes(b,"00000003783f79","unpaired surrogate replacement unchanged"); b.release();
        b=input("00000002c32855"); check("\ufffd(".equals(BufferUtil.readString(b)),"malformed UTF-8 replacement unchanged"); indexes(b,6,7,"invalid UTF-8 consumption"); b.release();
        final ByteBuf shortHeader=input("000000"); fails(IndexOutOfBoundsException.class,()->BufferUtil.readString(shortHeader),"short string length"); indexes(shortHeader,0,3,"short string header not consumed"); shortHeader.release();
        final ByteBuf truncated=input("000000036162"); fails(IndexOutOfBoundsException.class,()->BufferUtil.readString(truncated),"short string bytes"); indexes(truncated,4,6,"string payload atomic readBytes"); truncated.release();
        final ByteBuf bounded=Unpooled.buffer(4,4); fails(IndexOutOfBoundsException.class,()->BufferUtil.writeString(bounded,"x"),"bounded string writer"); bytes(bounded,"00000001","string length remains after failed payload"); bounded.release();
    }
    private static void arrays() {
        ByteBuf b=output(); BufferUtil.writeIntArray(b,new int[]{0,1,-1,Integer.MIN_VALUE,Integer.MAX_VALUE,0x12345678});
        bytes(b,"000000060000000000000001ffffffff800000007fffffff12345678","independent big-endian int array fixture");
        check(Arrays.equals(BufferUtil.readIntArray(b),new int[]{0,1,-1,Integer.MIN_VALUE,Integer.MAX_VALUE,0x12345678}),"all int32 values roundtrip"); indexes(b,28,28,"int array consumption"); b.release();
        b=output(); BufferUtil.writeIntArray(b,new int[0]); bytes(b,"00000000","zero array"); check(BufferUtil.readIntArray(b).length==0,"zero array read"); b.release();
        final ByteBuf nullArray=output(); fails(NullPointerException.class,()->BufferUtil.writeIntArray(nullArray,null),"null int array keeps old NPE"); indexes(nullArray,0,0,"null array no writes"); nullArray.release();
        for(String negative:new String[]{"ffffffff","fffffffe","80000000"}) { final ByteBuf n=input(negative+"55"); fails(NegativeArraySizeException.class,()->BufferUtil.readIntArray(n),"negative array allocation"); indexes(n,4,5,"negative array header consumed"); n.release(); }
        final ByteBuf truncated=input("0000000200000001abcdef"); fails(IndexOutOfBoundsException.class,()->BufferUtil.readIntArray(truncated),"partial int array"); indexes(truncated,8,11,"previous integer consumed, partial next untouched"); truncated.release();
        final ByteBuf shortHeader=input("0000"); fails(IndexOutOfBoundsException.class,()->BufferUtil.readIntArray(shortHeader),"short array length"); indexes(shortHeader,0,2,"short array header not consumed"); shortHeader.release();
        final ByteBuf bounded=Unpooled.buffer(8,8); fails(IndexOutOfBoundsException.class,()->BufferUtil.writeIntArray(bounded,new int[]{1,2}),"bounded array writer"); bytes(bounded,"0000000200000001","array partial writer remains"); bounded.release();
        b=output(); b.writeByte(0x55); b.readerIndex(1); BufferUtil.writeIntArray(b,new int[]{7,8}); BufferUtil.writeIntArray(b,new int[]{9}); b.writeByte(0x66);
        check(Arrays.equals(BufferUtil.readIntArray(b),new int[]{7,8}) && Arrays.equals(BufferUtil.readIntArray(b),new int[]{9}),"successive singular arrays, no extra enclosing frame"); indexes(b,21,22,"nonzero starting reader/writer preserved"); check(b.readByte()==0x66,"array suffix"); b.release();
    }
    private static void vectors() {
        ByteBuf b=output(); BufferUtil.writeVec3(b,null); BufferUtil.writeVec3(b,new Vec3d(1.25,-2.5,3.75));
        bytes(b,"00013ff4000000000000c004000000000000400e000000000000","boolean and xyz doubles independent fixture");
        check(BufferUtil.readVec3(b)==null,"null vector"); Vec3d v=BufferUtil.readVec3(b); check(v.x==1.25 && v.y==-2.5 && v.z==3.75,"xyz vector"); indexes(b,26,26,"vector exact consumption"); b.release();
        b=input("023ff00000000000004000000000000000400800000000000055"); v=BufferUtil.readVec3(b); check(v.x==1&&v.y==2&&v.z==3,"nonzero boolean truth"); indexes(b,25,26,"vector trailing bytes"); b.release();
        b=output(); BufferUtil.writeVec3(b,new Vec3d(Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY)); v=BufferUtil.readVec3(b); check(Double.isNaN(v.x)&&v.y==Double.POSITIVE_INFINITY&&v.z==Double.NEGATIVE_INFINITY,"nonfinite vector not sanitized"); b.release();
        final ByteBuf truncated=input("013ff000000000000000"); fails(IndexOutOfBoundsException.class,()->BufferUtil.readVec3(truncated),"partial vector"); indexes(truncated,9,10,"vector previous components consumed"); truncated.release();
        final ByteBuf empty=output(); fails(IndexOutOfBoundsException.class,()->BufferUtil.readVec3(empty),"missing vector flag"); indexes(empty,0,0,"empty vector unchanged"); empty.release();
    }
    private static void nbt() throws Exception {
        ByteBuf b=output(); BufferUtil.writeNBT(b,null); bytes(b,"ffff","NBT null short sentinel"); NBTTagCompound absent=BufferUtil.readNBT(b); check(absent!=null&&absent.isEmpty(),"NBT null decodes new empty compound not null"); b.release();
        b=input("ffffffff"); NBTTagCompound a=BufferUtil.readNBT(b), c=BufferUtil.readNBT(b); check(a!=c,"sentinel creates independent compounds"); b.release();
        b=output(); BufferUtil.writeNBT(b,new NBTTagCompound()); bytes(b,EMPTY_NBT,"pinned independent empty GZIP root"); check(BufferUtil.readNBT(b).isEmpty(),"empty compound roundtrip"); b.release();
        b=output(); BufferUtil.writeNBT(b,integerTag()); bytes(b,INT_NBT,"pinned independent typed integer GZIP root"); check(BufferUtil.readNBT(b).equals(integerTag()),"integer tag roundtrip"); b.release();
        NBTTagCompound rich=integerTag(); rich.setByte("b",(byte)-128); rich.setShort("s",(short)-32768); rich.setLong("l",Long.MIN_VALUE); rich.setFloat("f",1.25F); rich.setDouble("d",Double.NEGATIVE_INFINITY); rich.setString("text","A\u00e9\u0000\ud83d\ude00"); rich.setByteArray("bytes",new byte[]{-1,0,127}); rich.setIntArray("ints",new int[]{Integer.MIN_VALUE,0,Integer.MAX_VALUE}); rich.setTag("child",integerTag()); NBTTagList list=new NBTTagList(); list.appendTag(new NBTTagString("first")); list.appendTag(new NBTTagString("second")); rich.setTag("list",list);
        b=output(); BufferUtil.writeNBT(b,rich); b.writeByte(0x55); NBTTagCompound copy=BufferUtil.readNBT(b); check(copy.equals(rich)&&copy!=rich&&copy.getCompoundTag("child")!=rich.getCompoundTag("child"),"nested all common primitive NBT native roundtrip"); check(b.readableBytes()==1&&b.readByte()==0x55,"NBT bounded payload leaves outer suffix"); b.release();
        for(String negative:new String[]{"fffe","8000"}) { final ByteBuf n=input(negative+"55"); fails(NegativeArraySizeException.class,()->BufferUtil.readNBT(n),"signed short negative NBT length"); indexes(n,2,3,"negative NBT payload not consumed"); n.release(); }
        final ByteBuf shortHeader=input("ff"); fails(IndexOutOfBoundsException.class,()->BufferUtil.readNBT(shortHeader),"short NBT length"); indexes(shortHeader,0,1,"short NBT header unchanged"); shortHeader.release();
        final ByteBuf truncated=input("0004010203"); fails(IndexOutOfBoundsException.class,()->BufferUtil.readNBT(truncated),"truncated framed NBT bytes"); indexes(truncated,2,5,"NBT payload atomic readBytes"); truncated.release();
        for(String broken:new String[]{"0000","0003010203","000a1f8b0800000000000000"}) { final ByteBuf n=input(broken); String error=stderr(()->check(BufferUtil.readNBT(n).isEmpty(),"caught compressed IO returns empty")); check(error.contains("Exception"),"old IO error printing preserved"); indexes(n,n.writerIndex(),n.writerIndex(),"malformed compressed bytes consumed"); n.release(); }
        final ByteBuf noncompound=framed(gzip(hex("03000012345678"))); String rootError=stderr(()->check(BufferUtil.readNBT(noncompound).isEmpty(),"noncompound root caught")); check(rootError.contains("Root tag must be a named compound tag"),"native root validation error printed"); noncompound.release();
        final NBTTagCompound tooLong=new NBTTagCompound(); char[] chars=new char[65536]; Arrays.fill(chars,'a'); tooLong.setString("x",new String(chars)); final ByteBuf failedWrite=output(); String writeError=stderr(()->BufferUtil.writeNBT(failedWrite,tooLong)); check(writeError.contains("UTFDataFormatException"),"native too-long modified UTF write IOException retained"); bytes(failedWrite,"0000","failed NBT compression writes zero length not partial data"); failedWrite.release();
        final NBTTagCompound huge=new NBTTagCompound(); huge.setByteArray("x",new byte[2097152]); final ByteBuf limited=output(); BufferUtil.writeNBT(limited,huge); check(limited.getUnsignedShort(0)<32768,"compressed oversized decoded payload fits positive frame"); Throwable limitFailure=fails(RuntimeException.class,()->BufferUtil.readNBT(limited),"old 2 MiB NBT tracker remains enforced"); check(limitFailure.getMessage().contains("Tried to read NBT tag that was too big"),"failure is native size tracker, not unrelated runtime error"); indexes(limited,limited.writerIndex(),limited.writerIndex(),"oversize decode consumes framed compressed bytes before failure"); limited.release();
        // No length guard: exact signed-short boundaries preserve legacy wrapping and all written bytes.
        for(int target:new int[]{32767,32768,65535,65536}) {
            NBTTagCompound payload=payloadWithCompressedLength(target); final ByteBuf large=output(); BufferUtil.writeNBT(large,payload); check(large.writerIndex()==target+2 && large.getShort(0)==(short)target,"full oversized gzip retained with short wrap "+target);
            if(target==32767) { check(BufferUtil.readNBT(large).equals(payload),"largest positive compressed NBT roundtrip"); indexes(large,target+2,target+2,"32767 full consumption"); }
            else if(target==32768) { fails(NegativeArraySizeException.class,()->BufferUtil.readNBT(large),"32768 signed negative allocation"); indexes(large,2,target+2,"32768 data remains unread"); }
            else if(target==65535) { check(BufferUtil.readNBT(large).isEmpty(),"65535 length collides with null sentinel"); indexes(large,2,target+2,"65535 data remains unread"); }
            else { String error=stderr(()->check(BufferUtil.readNBT(large).isEmpty(),"65536 length wraps to zero")); check(error.contains("EOFException"),"wrapped zero logs EOF"); indexes(large,2,target+2,"65536 data remains unread"); }
            large.release();
        }
    }
    private static NBTTagCompound payloadWithCompressedLength(int target) {
        byte[] random=new byte[target]; new Random(424242L).nextBytes(random);
        for(int n=Math.max(0,target-100);n<target;n++) { NBTTagCompound payload=new NBTTagCompound(); payload.setByteArray("x",Arrays.copyOf(random,n)); ByteBuf b=output(); BufferUtil.writeNBT(b,payload); int actual=b.writerIndex()-2; b.release(); if(actual==target) return payload; }
        throw new AssertionError("Unable to construct exact compressed length "+target);
    }
    private static final class PolicyItem extends Item {
        private final boolean shares; int shareCalls; int damageCalls;
        PolicyItem(boolean shares,boolean damageable) { this.shares=shares; if(damageable)setMaxDamage(10); }
        @Override public boolean getShareTag() { shareCalls++; return shares; }
        @Override public boolean isDamageable() { damageCalls++; return super.isDamageable(); }
    }
    private static void items() {
        check(Item.getIdFromItem(Items.DYE)==351 && Item.getIdFromItem(Items.STICK)==280,"pinned vanilla wire IDs");
        ByteBuf b=output(); BufferUtil.writeItemStack(b,null); BufferUtil.writeItemStack(b,ItemStack.EMPTY); bytes(b,"ffffffff","null and native EMPTY same sentinel"); check(BufferUtil.readItemStack(b)==null&&BufferUtil.readItemStack(b)==null,"readItemStack remains nullable legacy API"); check(!ItemStack.EMPTY.hasTagCompound(),"shared native EMPTY not mutated"); b.release();
        ItemStack plain=new ItemStack(Items.DYE,7,3); b=output(); BufferUtil.writeItemStack(b,plain); bytes(b,"015f070003ffff","pinned item ID, signed count, signed metadata, NBT sentinel"); ItemStack result=BufferUtil.readItemStack(b); check(result.getItem()==Items.DYE&&result.getCount()==7&&result.getMetadata()==3&&result.hasTagCompound()&&result.getTagCompound().isEmpty(),"item no tag decodes explicit empty tag as original"); indexes(b,7,7,"item exact consumption"); b.release();
        plain.setTagCompound(integerTag()); b=output(); BufferUtil.writeItemStack(b,plain); bytes(b,"015f070003"+INT_NBT,"pinned tagged stack bytes"); result=BufferUtil.readItemStack(b); check(result.getTagCompound().equals(integerTag())&&result.getTagCompound()!=plain.getTagCompound(),"item tag roundtrip independent"); b.release();
        for(int count:new int[]{1,64,127,128,255,256,257,Integer.MAX_VALUE}) { b=output(); ItemStack s=new ItemStack(Items.DYE,count,32767); BufferUtil.writeItemStack(b,s); check(b.getByte(2)==(byte)count && b.getShort(3)==32767 && b.writerIndex()==7,"count low byte, no clamp "+count); result=BufferUtil.readItemStack(b); int signed=(byte)count; check(result!=null && result!=ItemStack.EMPTY && result.isEmpty()==(signed<=0),"native signed-count emptiness "+count); check(result.getCount()==(signed<=0?0:signed),"native public count for signed wire value "+count); check(result.getTagCompound().isEmpty(),"signed count still consumes/sets tag"); b.release(); }
        for(int count:new int[]{0,-1,Integer.MIN_VALUE}) { b=output(); BufferUtil.writeItemStack(b,new ItemStack(Items.DYE,count,3)); bytes(b,"ffff","native nonpositive source count treated as empty "+count); b.release(); }
        for(int meta:new int[]{0,32767,32768,65535}) { b=output(); BufferUtil.writeItemStack(b,new ItemStack(Items.DYE,1,meta)); check(b.getShort(3)==(short)meta,"metadata signed short narrowing "+meta); result=BufferUtil.readItemStack(b); check(result.getItemDamage()==Math.max(0,(short)meta),"native constructor negative damage clamp documented "+meta); b.release(); }
        for(int id:new int[]{-1,-2,-32768}) { b=output(); b.writeShort(id); b.writeByte(0x55); check(BufferUtil.readItemStack(b)==null,"all negative item IDs skip payload"); indexes(b,2,3,"negative item ID leaves suffix"); b.release(); }
        b=input("7fff010003ffff55"); result=BufferUtil.readItemStack(b); check(result!=null&&result.isEmpty()&&result.getTagCompound().isEmpty(),"unknown positive native registry ID creates empty object but consumes NBT"); indexes(b,7,8,"unknown positive ID consumption"); b.release();
        for(boolean damageable:new boolean[]{false,true}) for(boolean shares:new boolean[]{false,true}) { PolicyItem item=new PolicyItem(shares,damageable); ItemStack s=new ItemStack(item,2,5); s.setTagCompound(integerTag()); item.shareCalls=0; item.damageCalls=0; b=output(); BufferUtil.writeItemStack(b,s); check(b.writerIndex()==(damageable||shares?39:7),"damage/share gate "+damageable+"/"+shares); check(item.damageCalls==1 && item.shareCalls==(damageable?0:1),"original short-circuit callback count"); check(b.getShort(0)==-1,"unregistered real item ID remains signed -1 while writer emits full record"); b.release(); }
        String[] fragments={"01","015f","015f07","015f0700","015f070003","015f070003ff","015f07000300030102"}; int[] consumed={0,2,3,3,5,5,7};
        for(int i=0;i<fragments.length;i++) { final ByteBuf shortItem=input(fragments[i]); fails(IndexOutOfBoundsException.class,()->BufferUtil.readItemStack(shortItem),"truncated item field "+i); indexes(shortItem,consumed[i],fragments[i].length()/2,"partial item field reads "+i); shortItem.release(); }
        final ByteBuf bounded=Unpooled.buffer(4,4); fails(IndexOutOfBoundsException.class,()->BufferUtil.writeItemStack(bounded,new ItemStack(Items.DYE,7,3)),"bounded item output"); bytes(bounded,"015f07","item ID/count remain after short metadata failure"); bounded.release();
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register(); strings(); arrays(); vectors(); nbt(); items();
        System.out.println("PASS complete BufferUtil native codec: "+checks+" assertions; pinned independent bytes, signed widths, malformed inputs and exact index behavior. Not packet dispatch/threading or gameplay acceptance.");
    }
}
