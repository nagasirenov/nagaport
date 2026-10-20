import com.hbm.blocks.ModBlocks;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.render.entity.item.RenderMovingItem;
import com.hbm.render.entity.item.RenderMovingPackage;
import com.hbm.render.util.NativeMachineItemRenderer;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

/** Whole production consumers + real RenderItem, frame helper, models and GL.
 * Only NTM entity storage and crate registration are declared fixture boundaries. */
public final class NativeConveyorRendererTest {
    static int checks;
    static void check(boolean ok, String message) { checks++; if(!ok) throw new AssertionError(message); }
    static float[] matrix() { return NativeMachineItemTest.matrix(); }
    static void reset() {
        NativeMachineDrawTest.reset();
        GL11.glTranslatef(32, 32, 0);
        GL11.glRotatef(35, 1, 0, 0);
        GL11.glRotatef(20, 0, 1, 0);
        GL11.glScalef(24, 24, 24);
    }
    static void outer(int id, boolean block, boolean fancy, double x, double y, double z) {
        GL11.glTranslated(x,y,z);
        GL11.glTranslated(0,new Random(id).nextDouble() * .0625,0);
        if(!block) {
            GL11.glRotatef(90,1,0,0);
            GL11.glTranslated(0,-.1875,0);
            if(!fancy) GL11.glRotatef(180,0,1,0);
        }
    }
    static final class Frame extends NativeMachineDrawTest.BaseOwner implements NativeMachineItemRenderer.FrameItemModel {
        int calls; float[] matrix; EntityItem entity; double x,y,z; float yaw,partial;
        NativeMachineItemRenderer.Display display; boolean fail;
        Frame(IBakedModel base) { super(base); }
        public void renderFrame(EntityItem entity, double x, double y, double z, float yaw, float partial, NativeMachineItemRenderer.Display display) {
            calls++; matrix=matrix(); this.entity=entity; this.x=x; this.y=y; this.z=z; this.yaw=yaw; this.partial=partial; this.display=display;
            if(fail) throw new IllegalStateException("controlled native frame failure");
        }
    }
    static NativeMachineDrawTest.Cube cube() {
        List<BakedQuad> quads=new ArrayList<>();FaceBakery bakery=new FaceBakery();
        for(EnumFacing face:EnumFacing.values())quads.add(bakery.makeBakedQuad(new org.lwjgl.util.vector.Vector3f(0,0,0),new org.lwjgl.util.vector.Vector3f(16,16,16),new BlockPartFace(null,-1,"test",new BlockFaceUV(new float[]{0,0,16,16},0)),NativeMachineDrawTest.icon.getParticleTexture(),face,ModelRotation.X0_Y0,null,false,true));
        return new NativeMachineDrawTest.Cube(quads,NativeMachineDrawTest.icon.getParticleTexture());
    }
    static void callbacks() throws Exception {
        RenderMovingItem renderer=new RenderMovingItem(NativeMachineDrawTest.mc.getRenderManager());
        for(boolean fancy:new boolean[]{false,true}) for(boolean block:new boolean[]{false,true}) for(int id:new int[]{1,37,19735}) {
            ItemStack stack=block?new ItemStack(Blocks.STONE,37):new ItemStack(Items.DIAMOND_SWORD,37);
            stack.addEnchantment(Enchantments.UNBREAKING,2);stack.setTagInfo("conveyor",new net.minecraft.nbt.NBTTagString("unchanged"));
            NBTTagCompound before=stack.writeToNBT(new NBTTagCompound());
            Frame model=new Frame(block?cube():NativeMachineDrawTest.icon);NativeMachineDrawTest.model(stack.getItem(),model);
            EntityMovingItem entity=new EntityMovingItem(stack);entity.setEntityId(id);entity.cacheForRender.hoverStart=1.75F;
            NativeMachineDrawTest.mc.gameSettings.fancyGraphics=fancy;
            reset();float[] prior=matrix();int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);
            renderer.doRender(entity,.25,.4,.1,123,.875F);
            check(Arrays.equals(prior,matrix())&&GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==depth,"outer matrix restored "+block+fancy+id);
            check(model.calls==1&&model.entity==entity.cacheForRender&&model.entity.getItem()==stack,"actual native frame receives cached entity and original stack");
            check(model.display==NativeMachineItemRenderer.Display.ENTITY_FRAME&&model.x==0&&model.y==0&&model.z==0&&model.yaw==0&&model.partial==0,"ordinary entity-frame route and zero legacy nested arguments");
            check(model.entity.hoverStart==0,"cached hover reset before actual invocation");
            reset();outer(id,block,fancy,.25,.4,.1);check(Arrays.equals(matrix(),model.matrix),"exact invoked outer orientation/placement/jitter "+block+fancy+id);
            float[] first=model.matrix.clone();reset();renderer.doRender(entity,.25,.4,.1,-90,.125F);check(Arrays.equals(first,model.matrix),"stable entity-ID jitter, no partial/yaw drift");
            check(before.equals(stack.writeToNBT(new NBTTagCompound())),"whole consumer keeps stack identity/count/damage/enchantment/NBT");
        }
        // A custom ItemBlock renderer whose retained inventory model is flat must
        // not be stood upright merely because its built-in wrapper says gui3d.
        ItemStack plant=new ItemStack(Blocks.SAPLING,17);Frame model=new Frame(NativeMachineDrawTest.icon);
        NativeMachineDrawTest.model(plant.getItem(),model);EntityMovingItem entity=new EntityMovingItem(plant);entity.setEntityId(41);
        for(boolean fancy:new boolean[]{false,true}) {
            NativeMachineDrawTest.mc.gameSettings.fancyGraphics=fancy;reset();renderer.doRender(entity,0,0,0,0,0);
            reset();outer(41,false,fancy,0,0,0);check(Arrays.equals(matrix(),model.matrix),"custom gui3d wrapper retains flat ItemBlock base orientation");
        }
        entity.cacheForRender=null;reset();float[] prior=matrix();int calls=model.calls;renderer.doRender(entity,2,3,4,0,0);
        check(model.calls==calls&&Arrays.equals(prior,matrix()),"absent cache does not draw or leak matrix");
        entity.stack=ItemStack.EMPTY;reset();prior=matrix();renderer.doRender(entity,2,3,4,0,0);check(Arrays.equals(prior,matrix()),"empty stack and absent cache safe");
        entity.stack=plant;entity.cacheForRender=new EntityItem(null,0,0,0,plant);model.fail=true;reset();prior=matrix();int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);
        boolean failed=false;try {renderer.doRender(entity,2,3,4,0,0);}catch(IllegalStateException expected){failed=true;}
        check(failed&&Arrays.equals(prior,matrix())&&GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==depth,"throwing native frame unwinds whole consumer matrix");
    }
    static void nativeCustom() {
        RenderMovingItem renderer=new RenderMovingItem(NativeMachineDrawTest.mc.getRenderManager());
        ItemStack stack=new ItemStack(Items.DIAMOND_SWORD,37);stack.addEnchantment(Enchantments.SHARPNESS,2);
        NativeMachineDrawTest.Custom custom=new NativeMachineDrawTest.Custom();stack.getItem().setTileEntityItemStackRenderer(custom);
        NativeMachineDrawTest.model(stack.getItem(),new NativeMachineDrawTest.BaseOwner(NativeMachineDrawTest.icon));
        EntityMovingItem entity=new EntityMovingItem(stack);entity.setEntityId(201);entity.cacheForRender.hoverStart=0;
        NBTTagCompound before=stack.writeToNBT(new NBTTagCompound());
        for(boolean fancy:new boolean[]{false,true}) {
            NativeMachineDrawTest.mc.gameSettings.fancyGraphics=fancy;
            reset();outer(201,false,fancy,.25,.4,.1);
            new NativeMachineItemRenderer.OneItemRenderer(NativeMachineDrawTest.mc.getRenderManager(),NativeMachineDrawTest.renderer,true).doRender(entity.cacheForRender,0,0,0,0,0);
            float[] expected=custom.matrix.clone();int calls=custom.calls;
            reset();float[] prior=matrix();renderer.doRender(entity,.25,.4,.1,70,.9F);
            check(custom.calls==calls+1&&custom.last==stack,"whole conveyor native TEISR custom path draws one original stack, not a generic icon");
            check(Arrays.equals(expected,custom.matrix),"native custom invoked matrix retains original outer and real native entity caller");
            check(Arrays.equals(prior,matrix())&&before.equals(stack.writeToNBT(new NBTTagCompound())),"native custom path restores outer matrix and preserves count/NBT");
        }
    }
    static void icons() {
        RenderMovingItem renderer=new RenderMovingItem(NativeMachineDrawTest.mc.getRenderManager());
        NativeMachineItemRenderer originalFrame=new NativeMachineItemRenderer();
        for(boolean fancy:new boolean[]{false,true}) for(boolean wrapped:new boolean[]{false,true}) for(int count:new int[]{1,2,6,16,21,32,41,64}) {
            ItemStack stack=new ItemStack(Blocks.SAPLING,count);
            IBakedModel model=wrapped?new NativeMachineDrawTest.SelectiveBaseOwner(NativeMachineDrawTest.icon):NativeMachineDrawTest.icon;
            NativeMachineDrawTest.model(stack.getItem(),model);
            EntityMovingItem entity=new EntityMovingItem(stack);entity.setEntityId(317);entity.cacheForRender.hoverStart=0;
            NativeMachineDrawTest.mc.gameSettings.fancyGraphics=fancy;
            reset();outer(317,false,fancy,.25,.4,.1);originalFrame.renderFrame(entity.cacheForRender,0,0,0,0,0,NativeMachineItemRenderer.Display.ENTITY_FRAME);byte[] expected=NativeMachineDrawTest.pixels();
            check(!Arrays.equals(expected,new byte[expected.length]),"real reference icon emits pixels");
            reset();float[] prior=matrix();renderer.doRender(entity,.25,.4,.1,123,.875F);
            check(Arrays.equals(expected,NativeMachineDrawTest.pixels()),"whole conveyor actual framebuffer = original outer + real entity frame "+fancy+wrapped+count);
            check(Arrays.equals(prior,matrix()),"ordinary icon frame restores whole matrix");
            if(wrapped)check(((NativeMachineDrawTest.SelectiveBaseOwner)model).selections>=3,"selective FrameBaseModel actually selected for orientation and draw");
            stack.addEnchantment(Enchantments.UNBREAKING,1);NBTTagCompound before=stack.writeToNBT(new NBTTagCompound());
            reset();renderer.doRender(entity,.25,.4,.1,0,0);
            check(NativeMachineDrawTest.textures.glints==(fancy?NativeMachineItemRenderer.iconCount(count):0),"actual fancy multipass copy/glint count, not decoration "+count+fancy);
            check(before.equals(stack.writeToNBT(new NBTTagCompound())),"icon count/NBT preserved");
        }
    }
    static float spread(Random random) { return (random.nextFloat()*2F-1F)*.2F/.25F; }
    static void blocks() throws Exception {
        RenderMovingItem renderer=new RenderMovingItem(NativeMachineDrawTest.mc.getRenderManager());
        for(boolean fancy:new boolean[]{false,true}) for(boolean wrapped:new boolean[]{false,true}) for(int count:new int[]{1,2,5,6,20,21,40,41,64}) for(int age:new int[]{0,71}) {
            ItemStack stack=new ItemStack(Blocks.STONE,count);stack.setTagInfo("unchanged",new net.minecraft.nbt.NBTTagString("block"));
            NativeMachineDrawTest.Cube cube=cube();IBakedModel model=wrapped?new NativeMachineDrawTest.SelectiveBaseOwner(cube):cube;NativeMachineDrawTest.model(stack.getItem(),model);
            EntityMovingItem entity=new EntityMovingItem(stack);entity.setEntityId(103);NativeMachineDrawTest.field(entity.cacheForRender,"age",age);entity.cacheForRender.hoverStart=2;
            NativeMachineDrawTest.mc.gameSettings.fancyGraphics=fancy;NBTTagCompound before=stack.writeToNBT(new NBTTagCompound());
            reset();float[] prior=matrix();renderer.doRender(entity,.25,.4,.1,123,.875F);
            int copies=NativeMachineItemRenderer.blockCount(count);check(cube.queries==7*copies,"actual native cube draw copy count "+count);
            check(Arrays.equals(prior,matrix()),"cube outer frame restored");
            for(int copy=0;copy<copies;copy++) {
                reset();outer(103,true,fancy,.25,.4,.1);GlStateManager.translate(0,net.minecraft.util.math.MathHelper.sin(age/10F)*.1F+.1F,0);
                // Independently replay the original RenderItem block frame, not a
                // disconnected copy of the consumer's new implementation.
                GlStateManager.rotate(age/20F*57.295776F,0,1,0);GlStateManager.scale(1.25F,1.25F,1.25F);GlStateManager.translate(0,.05F,0);GlStateManager.rotate(-90,0,1,0);GlStateManager.scale(.25F,.25F,.25F);GlStateManager.translate(-.5F,-.5F,-.5F);
                Random random=new Random(187);float dx=0,dy=0,dz=0;for(int n=1;n<=copy;n++){dx=spread(random);dy=spread(random);dz=spread(random);}if(copy>0)GlStateManager.translate(dx,dy,dz);
                for(int face=0;face<7;face++)check(Arrays.equals(matrix(),cube.matrices.get(copy*7+face)),"actual queried cube matrix includes original bob/rotation/count spread "+age+"/"+copy+"/"+face);
            }
            check(before.equals(stack.writeToNBT(new NBTTagCompound())),"native cube source stack/NBT unchanged");
        }
    }
    static final class StateBlock extends Block {
        static final PropertyInteger FORM=PropertyInteger.create("form",0,1);int metadata=-1;
        StateBlock(){super(Material.ROCK);}
        protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FORM);}
        public IBlockState getStateFromMeta(int meta){metadata=meta;return getDefaultState().withProperty(FORM,meta&1);}
        public int getMetaFromState(IBlockState state){return state.getValue(FORM);}
        public EnumBlockRenderType getRenderType(IBlockState state){return state.getValue(FORM)==1?EnumBlockRenderType.INVISIBLE:EnumBlockRenderType.MODEL;}
    }
    static void states() throws Exception {
        StateBlock block=new StateBlock();ItemBlock item=new ItemBlock(block){@Override public int getMetadata(int damage){return damage&1;}};
        // Controlled unregistered NTM ItemBlock fixture; no registry unlock or replacement.
        RenderMovingItem renderer=new RenderMovingItem(NativeMachineDrawTest.mc.getRenderManager());
        for(int damage:new int[]{2,3}) {
            ItemStack stack=new ItemStack(item,1,damage);Frame model=new Frame(cube());
            ModelResourceLocation location=new ModelResourceLocation("test:state"+damage,"inventory");NativeMachineDrawTest.models.putObject(location,model);NativeMachineDrawTest.renderer.getItemModelMesher().register(item,damage,location);NativeMachineDrawTest.renderer.getItemModelMesher().rebuildCache();
            EntityMovingItem entity=new EntityMovingItem(stack);entity.setEntityId(59);NativeMachineDrawTest.mc.gameSettings.fancyGraphics=true;reset();renderer.doRender(entity,0,0,0,0,0);
            reset();outer(59,damage==2,true,0,0,0);check(Arrays.equals(matrix(),model.matrix),"native metadata state prevents invisible custom block being blindly classified gui3d");
            check(block.metadata==(damage&1),"ItemBlock metadata translation used for native state");
        }
    }
    static void packages() throws Exception {
        RenderMovingPackage renderer=new RenderMovingPackage(NativeMachineDrawTest.mc.getRenderManager());
        EntityMovingItem entity=new EntityMovingItem(new ItemStack(Items.DIAMOND,64));
        Frame model=new Frame(cube());NativeMachineDrawTest.model(Item.getItemFromBlock(ModBlocks.crate),model);
        Field cached=RenderMovingPackage.class.getDeclaredField("dummy");cached.setAccessible(true);
        EntityItem previous=null;ItemStack source=null;
        for(int pass=0;pass<3;pass++) {
            reset();float[] prior=matrix();renderer.doRender(entity,.25,.4,.1,50,.75F);
            check(Arrays.equals(prior,matrix()),"package consumer restores matrix");
            reset();GL11.glTranslated(.25,.4-.0125,.1);GL11.glScaled(2,2,2);check(Arrays.equals(matrix(),model.matrix),"actual package crate frame receives original y offset and scale2");
            check(model.display==NativeMachineItemRenderer.Display.ENTITY_FRAME&&model.entity.hoverStart==0&&model.partial==0&&model.yaw==0,"package uses ordinary entity frame with hover reset");
            check(model.entity.world==entity.world&&model.entity!=previous,"package makes original per-render EntityItem in source world");
            ItemStack current=(ItemStack)cached.get(renderer);if(source==null)source=current;
            check(current==source&&model.entity.getItem()==source&&source.getCount()==1&&source.getItem()==Item.getItemFromBlock(ModBlocks.crate),"package reuses exact cached one-crate stack");previous=model.entity;
        }
        // Real ordinary crate branch, including original entity bob.
        NativeMachineDrawTest.Cube cube=cube();NativeMachineDrawTest.model(source.getItem(),cube);reset();renderer.doRender(entity,.25,.4,.1,0,0);
        check(cube.queries==7,"complete package ordinary crate really draws one native cube");
        reset();GL11.glTranslated(.25,.4-.0125,.1);GL11.glScaled(2,2,2);GlStateManager.translate(0,.1F,0);NativeMachineItemRenderer.applyBlockFrame(0,0,0);check(Arrays.equals(matrix(),cube.matrices.get(0)),"package cube includes original ordinary entity bob and frame scale");
        NativeMachineDrawTest.model(source.getItem(),model);model.fail=true;reset();float[] prior=matrix();int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);boolean failed=false;
        try{renderer.doRender(entity,1,2,3,0,0);}catch(IllegalStateException expected){failed=true;}
        check(failed&&Arrays.equals(prior,matrix())&&GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==depth,"package native callback failure restores outer matrix");
    }
    public static void main(String[] args) throws Exception {
        Constructor<?> constructor=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();constructor.setAccessible(true);
        try(AutoCloseable context=(AutoCloseable)constructor.newInstance()) {
            Method init=Class.forName("MaterialTextureAtlasSpriteNativeTest").getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);
            NativeMachineDrawTest.setup();
            callbacks();nativeCustom();icons();blocks();states();packages();
            check(GL11.glGetError()==GL11.GL_NO_ERROR,"all complete native conveyor draws leave no GL error");
            NativeMachineDrawTest.framebuffer.deleteFramebuffer();
            System.out.println("PASS "+checks+" whole native conveyor consumer checks: invoked matrices, actual framebuffers, entity-frame counts/bob/spread, model ownership, metadata state, stack/NBT, crate cache and failure restoration. Controlled NTM entity/registry fixtures; not full game acceptance.");
        }
    }
}
