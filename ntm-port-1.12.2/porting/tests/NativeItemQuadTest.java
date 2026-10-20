import com.hbm.render.util.NativeItemQuadRenderer;
import java.lang.reflect.*;
import java.nio.*;
import java.util.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

/** Real native RenderItem.renderQuads is the byte oracle, not a copied expected algorithm. */
public class NativeItemQuadTest {
    static int checks;
    static void check(boolean test,String message){checks++;if(!test)throw new AssertionError(message);}
    static byte[] finish(BufferBuilder buffer) {
        int length=buffer.getVertexCount()*buffer.getVertexFormat().getSize();
        buffer.finishDrawing();ByteBuffer bytes=buffer.getByteBuffer().duplicate();bytes.position(0);
        byte[] result=new byte[length];bytes.get(result);return result;
    }
    static BakedQuad quad(int tint,EnumFacing face,TextureAtlasSprite sprite) {
        BufferBuilder b=new BufferBuilder(256);b.begin(7,DefaultVertexFormats.ITEM);
        for(int i=0;i<4;i++)b.pos(i%2,(i/2)%2,.25).color(20+i*30,220-i*20,128,230).tex(i%2,(i/2)%2).normal(face.getXOffset(),face.getYOffset(),face.getZOffset()).endVertex();
        ByteBuffer bytes=ByteBuffer.wrap(finish(b)).order(ByteOrder.nativeOrder());int[] data=new int[bytes.remaining()/4];bytes.asIntBuffer().get(data);
        return new BakedQuad(data,tint,face,sprite,true,DefaultVertexFormats.ITEM);
    }
    public static final class SpyRenderer extends RenderItem {
        List<String> calls; ItemStack seen; String label; boolean failModel,failOverlay;
        SpyRenderer(){super(null,null,null);}
        @Override public void renderItemAndEffectIntoGUI(ItemStack stack,int x,int y){check(zLevel==0,"local GUI z");calls.add("model:"+x+":"+y);seen=stack;if(failModel)throw new IllegalStateException("model failure");}
        @Override public void renderItemOverlayIntoGUI(net.minecraft.client.gui.FontRenderer font,ItemStack stack,int x,int y,String text){check(zLevel==0&&stack==seen,"overlay same stack/local z");calls.add("overlay:"+x+":"+y);label=text;if(failOverlay)throw new IllegalStateException("overlay failure");}
    }
    static void guiDispatch(Object allocator)throws Exception {
        SpyRenderer spy=(SpyRenderer)allocator.getClass().getMethod("allocateInstance",Class.class).invoke(allocator,SpyRenderer.class);spy.calls=new ArrayList<>();spy.zLevel=37;
        ItemStack stack=new ItemStack(Items.DIAMOND_SWORD,2,7);stack.addEnchantment(Enchantments.SHARPNESS,3);NBTTagCompound before=stack.writeToNBT(new NBTTagCompound());
        com.hbm.render.util.NativeGuiItemDraw.withOverlay(spy,null,stack,12,23,0,null);
        check(spy.calls.equals(Arrays.asList("model:12:23","overlay:12:23"))&&spy.seen==stack,"ordered normal native dispatch with unchanged stack identity");check(spy.zLevel==37,"normal restores shared z");
        for(boolean model:new boolean[]{true,false}) {
            spy.calls.clear();spy.failModel=model;spy.failOverlay=!model;
            try {com.hbm.render.util.NativeGuiItemDraw.withOverlay(spy,null,stack,1,2,0,null);throw new AssertionError("expected failure");}
            catch(IllegalStateException expected){check(spy.zLevel==37,"exception restores shared z");check(spy.calls.size()==(model?1:2),"exception preserves callback order");}
        }
        spy.failModel=spy.failOverlay=false;spy.calls.clear();
        com.hbm.render.util.NativeGuiItemDraw.withOverlay(spy,null,ItemStack.EMPTY,1,2,0,null);com.hbm.render.util.NativeGuiItemDraw.withOverlay(spy,null,null,1,2,0,null);check(spy.calls.isEmpty(),"null/shared EMPTY does not render");
        stack.setCount(0);NBTTagCompound emptyBefore=stack.writeToNBT(new NBTTagCompound());
        com.hbm.render.util.NativeGuiItemDraw.withOverlay(spy,null,stack,1,2,0,null);check(spy.seen!=stack&&spy.seen.getItem()==Items.DIAMOND_SWORD&&spy.seen.getCount()==1,"zero-count manual icon gets presentation-only copy");check("0".equals(spy.label)&&spy.seen.hasEffect()&&spy.seen.getItemDamage()==7,"zero-count overlay, enchantment and damage retained");check(emptyBefore.equals(stack.writeToNBT(new NBTTagCompound()))&&stack.isEmpty(),"zero-count source never resurrected");
        stack.setCount(2);check(before.equals(stack.writeToNBT(new NBTTagCompound())),"GUI dispatch preserves original complete NBT");
    }
    public static void main(String[] args)throws Exception {
        Constructor<?> constructor=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();constructor.setAccessible(true);
        try(AutoCloseable context=(AutoCloseable)constructor.newInstance()) {
            Method settings=Class.forName("MaterialTextureAtlasSpriteNativeTest").getDeclaredMethod("initializeNativeClientSettings");settings.setAccessible(true);settings.invoke(null);run();
        }
    }
    static void run()throws Exception {
        Bootstrap.register();ItemColors colors=new ItemColors();
        colors.registerItemColorHandler((stack,tint)->tint==0?0x3c93e1:0x12ff4070,Items.DIAMOND_SWORD);
        Field unsafe=Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");unsafe.setAccessible(true);
        Object allocator=unsafe.get(null);guiDispatch(allocator);RenderItem renderer=(RenderItem)allocator.getClass().getMethod("allocateInstance",Class.class).invoke(allocator,RenderItem.class);
        Field colorField=RenderItem.class.getDeclaredField("itemColors");colorField.setAccessible(true);colorField.set(renderer,colors);
        Method nativeQuads=RenderItem.class.getDeclaredMethod("renderQuads",BufferBuilder.class,List.class,int.class,ItemStack.class);nativeQuads.setAccessible(true);
        TextureAtlasSprite sprite=new TextureAtlasSprite("test:quad"){};sprite.setIconWidth(16);sprite.setIconHeight(16);sprite.initSprite(64,64,16,16,false);
        ItemStack enchanted=new ItemStack(Items.DIAMOND_SWORD,3,17);enchanted.addEnchantment(net.minecraft.init.Enchantments.SHARPNESS,2);
        NBTTagCompound original=enchanted.writeToNBT(new NBTTagCompound());
        boolean previous=EntityRenderer.anaglyphEnable;
        try {
            for(boolean anaglyph:new boolean[]{false,true})for(ItemStack stack:new ItemStack[]{ItemStack.EMPTY,new ItemStack(Items.DIAMOND_SWORD),enchanted})for(EnumFacing face:EnumFacing.values()) {
                EntityRenderer.anaglyphEnable=anaglyph;
                List<BakedQuad> quads=Arrays.asList(quad(-1,face,sprite),quad(0,face,sprite),quad(1,face,sprite));
                BufferBuilder expected=new BufferBuilder(512),actual=new BufferBuilder(512);
                expected.begin(7,DefaultVertexFormats.ITEM);actual.begin(7,DefaultVertexFormats.ITEM);
                nativeQuads.invoke(renderer,expected,quads,-1,stack);NativeItemQuadRenderer.appendQuads(actual,quads,stack,colors);
                check(Arrays.equals(finish(expected),finish(actual)),"native geometry/tint/anaglyph bytes");
            }
            final List<EnumFacing> queried=new ArrayList<>();
            IBakedModel model=new IBakedModel() {
                public List<BakedQuad> getQuads(net.minecraft.block.state.IBlockState state,EnumFacing face,long seed){check(state==null&&seed==0,"native item query inputs");queried.add(face);return Collections.singletonList(quad(-1,EnumFacing.UP,sprite));}
                public boolean isAmbientOcclusion(){return false;} public boolean isGui3d(){return false;} public boolean isBuiltInRenderer(){return false;}
                public TextureAtlasSprite getParticleTexture(){return sprite;} public ItemCameraTransforms getItemCameraTransforms(){return ItemCameraTransforms.DEFAULT;} public ItemOverrideList getOverrides(){return ItemOverrideList.NONE;}
            };
            BufferBuilder buffer=new BufferBuilder(1024);buffer.begin(7,DefaultVertexFormats.ITEM);NativeItemQuadRenderer.appendModel(buffer,model,enchanted,colors);
            List<EnumFacing> order=new ArrayList<>(Arrays.asList(EnumFacing.values()));order.add(null);
            check(queried.equals(order),"six face batches then unculled batch");check(buffer.getVertexCount()==28,"all seven batches retained");finish(buffer);
            check(original.equals(enchanted.writeToNBT(new NBTTagCompound()))&&enchanted.hasEffect(),"count/damage/enchantment/NBT unchanged");
        } finally {EntityRenderer.anaglyphEnable=previous;}
        System.out.println("PASS "+checks+" native quad byte-oracle/order/stack-identity checks. Real offscreen GL context initializes native TextureUtil; no GUI draw, model selection or fullmod proof.");
    }
}
