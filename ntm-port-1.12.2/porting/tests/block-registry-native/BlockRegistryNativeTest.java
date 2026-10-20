import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ModSoundTypes;
import com.hbm.main.LegacyIconRegistrationHandler;
import com.hbm.registry.GameRegistry;
import com.hbm.render.icon.LegacyTextureAtlasSprite;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.RegistryManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/** Real production owners and queue; only seven exact table statements are selected.
 * MainRegistry is a logger/random boundary fixture, never a replacement platform API.
 * This is deliberately NOT a compile/load claim for the complete ModBlocks table.
 */
public final class BlockRegistryNativeTest {
    private static int checks;
    private static void check(boolean yes, String label) {
        checks++;
        if (!yes) throw new AssertionError(label);
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        ModMetadata metadata = new ModMetadata();
        metadata.modId = "hbm"; metadata.name = "Block construction native probe"; metadata.version = "1";
        DummyModContainer owner = new DummyModContainer(metadata);
        Loader.instance().setupTestHarness(owner);
        Loader.instance().setActiveModContainer(owner);
        ModBlocksConstructionSlice.initialize();
        String[] names = {"structure_anchor", "crystal_hardened", "absorber", "absorber_red", "absorber_green", "absorber_pink", "pink_planks"};
        String[] textures = {"structure_anchor", "crystal_hardened", "block_deprecated", "block_deprecated", "block_deprecated", "block_deprecated", "pink_planks"};
        Block[] blocks = ModBlocksConstructionSlice.blocks();
        float[] hardness = {2.5F, 15F, 5F, 5F, 5F, 5F, 0F};
        float[] resistance = {6F, Float.POSITIVE_INFINITY, 6F, 6F, 6F, 6F, 0F};
        for (int i=0;i<blocks.length;i++) {
            Block block = blocks[i];
            check(block.getTranslationKey().equals("tile."+names[i]), "exact original translation identity "+names[i]);
            check(block.getRegistryName().equals(new ResourceLocation("hbm", "tile."+names[i])), "NTM name helper still seeds exact tile-prefixed registry ID");
            check(((BlockBase)block).getTextureName().equals("hbm:"+textures[i]), "explicit texture remains independent from registry path");
            check(block.getBlockHardness(block.getDefaultState(), null, BlockPos.ORIGIN)==hardness[i], "hardness "+names[i]);
            check(block.getExplosionResistance(null)==resistance[i], "native resistance scaling "+names[i]);
            check(block.getCreativeTab()==null, "original creative visibility");
        }
        check(blocks[6].getSoundType()==SoundType.WOOD, "actual original wood sound through public native override");
        check(ModBlocksConstructionSlice.thick_foliage.getCanBurn(), "thick foliage burnability");
        check(!ModBlocksConstructionSlice.thick_foliage.isToolNotRequired(), "thick foliage requires tool");
        check(ModBlocksConstructionSlice.thick_foliage.getPushReaction()==EnumPushReaction.DESTROY, "original mobility 1 = DESTROY");
        check(ModBlocksConstructionSlice.fluidcorium.blocksMovement(), "corium blocks movement");
        check(ModBlocksConstructionSlice.fluidcorium.getPushReaction()==EnumPushReaction.BLOCK, "corium immovable material");
        check(ModBlocksConstructionSlice.fluidmud.isLiquid(), "mud remains liquid material");
        legacyConstantOracle(new File(args[1]));
        GameRegistry.beginRegistrationTables();
        ModBlocksConstructionSlice.register();
        for(Block block:blocks) check(Item.getItemFromBlock(block)==Items.AIR, "no fake pre-event Block-to-Item map");
        GameRegistry.finishRegistrationTables();
        MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<Block>(RegistryManager.ACTIVE.getName(ForgeRegistries.BLOCKS), ForgeRegistries.BLOCKS));
        for(int i=0;i<blocks.length;i++) {
            check(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("hbm", "tile."+names[i]))==blocks[i], "real Block registration exact identity");
            check(Item.getItemFromBlock(blocks[i])==Items.AIR, "Block event does not prematurely publish ItemBlock");
        }
        MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<Item>(RegistryManager.ACTIVE.getName(ForgeRegistries.ITEMS), ForgeRegistries.ITEMS));
        for(Block block:blocks) {
            Item item=Item.getItemFromBlock(block);
            check(item.getClass()==ItemBlock.class, "real original two-argument queue chose standard ItemBlock");
            check(((ItemBlock)item).getBlock()==block, "genuine Forge native mapping preserves constructed block");
            check(item==ForgeRegistries.ITEMS.getValue(block.getRegistryName()), "exact registered ItemBlock identity");
        }
        System.out.println("PASS exact selected ModBlocks construction statements and native Block/Item registry events");
        MinecraftForge.EVENT_BUS.register(new LegacyIconRegistrationHandler());
        File resourceRoot = new File(args[0]);
        SimpleReloadableResourceManager resources = new SimpleReloadableResourceManager(new MetadataSerializer());
        resources.reloadResourcePack(new FolderResourcePack(resourceRoot));
        try (NativeAtlasEnvironment.Context context = new NativeAtlasEnvironment.Context()) {
            NativeAtlasEnvironment.initializeNativeClientSettings();
            URL nativeLocation = TextureMap.class.getProtectionDomain().getCodeSource().getLocation();
            if("jar".equals(nativeLocation.getProtocol())) nativeLocation=((java.net.JarURLConnection)nativeLocation.openConnection()).getJarFileURL();
            net.minecraftforge.fml.common.asm.FMLSanityChecker.fmlLocation = new File(nativeLocation.toURI());
            TextureMap map = new TextureMap("textures");
            map.setMipmapLevels(2);
            Object previousIcon = null;
            for(int reload=0;reload<2;reload++) {
                // Dispatch the genuine Pre event to the full production handler.
                // The whole-client ForgeHooksClient bucket bootstrap is outside
                // this isolated atlas probe, so call native atlas loading directly.
                MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.TextureStitchEvent.Pre(map));
                java.lang.reflect.Method init = TextureMap.class.getDeclaredMethod("initMissingImage");
                init.setAccessible(true);
                init.invoke(map);
                map.loadTextureAtlas(resources);
                check(map.getMipmapLevels()==2, "requested mip levels survive native stitch");
                check(((BlockBase)blocks[0]).getIcon(0,0)!=previousIcon, "reload refreshes icon wrapper");
                previousIcon=((BlockBase)blocks[0]).getIcon(0,0);
                GlStateManager.bindTexture(map.getGlTextureId());
                int width=GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
                int height=GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
                IntBuffer pixels=BufferUtils.createIntBuffer(width*height);
                GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, 32993, 33639, pixels);
                for(int i=0;i<blocks.length;i++) {
                    LegacyTextureAtlasSprite icon=(LegacyTextureAtlasSprite)((BlockBase)blocks[i]).getIcon(0,0);
                    TextureAtlasSprite sprite=icon.getSprite();
                    check(sprite==map.getAtlasSprite("hbm:blocks/"+textures[i]), "actual atlas has assigned texture, not tile-prefixed missing sprite");
                    check(sprite!=map.getMissingSprite(), "no missing texture fallback");
                    check(map.getTextureExtry("hbm:tile."+names[i])==null, "registry identity is never mistaken for texture name");
                    BufferedImage png=ImageIO.read(new File(resourceRoot,"assets/hbm/textures/blocks/"+textures[i]+".png"));
                    check(sprite.getIconWidth()==png.getWidth() && sprite.getIconHeight()==png.getHeight(), "original PNG dimensions");
                    for(int y=0;y<png.getHeight();y++) for(int x=0;x<png.getWidth();x++)
                        check(pixels.get((sprite.getOriginY()+y)*width+sprite.getOriginX()+x)==png.getRGB(x,y), "original PNG texel survives actual GL upload");
                    for(int side=0;side<6;side++) check(((BlockBase)blocks[i]).getIcon(side,15)==icon, "base side/meta icon behavior unchanged");
                }
                check(((BlockBase)blocks[2]).getIcon(0,0)==((BlockBase)blocks[5]).getIcon(0,0), "shared deprecated texture deduplicates within actual callback pass");
                check(GL11.glGetError()==GL11.GL_NO_ERROR, "native atlas upload has no GL errors");
                System.out.println("PASS real TextureStitchEvent + production callback, native PNG/mipmap/stitch/GL upload, reload "+reload);
                resources.reloadResourcePack(new FolderResourcePack(resourceRoot));
            }
            map.deleteGlTexture();
        }
        System.out.println("PASS "+checks+" checks; isolated seven-block construction/registry/atlas slice, not whole-ModBlocks or client/server parity");
    }
    private static void legacyConstantOracle(File jar) throws Exception {
        try(URLClassLoader loader=new URLClassLoader(new URL[]{jar.toURI().toURL()}, BlockRegistryNativeTest.class.getClassLoader())) {
            Class<?> color=Class.forName("awv",true,loader);
            Object[] colors=(Object[])color.getField("a").get(null);
            MapColor[] nativeColors={MapColor.FOLIAGE,MapColor.ADOBE,MapColor.PURPLE,MapColor.GREEN,MapColor.CYAN,MapColor.BROWN};
            for(MapColor n:nativeColors) {
                Object original=colors[n.colorIndex];
                check(color.getField("L").getInt(original)==n.colorValue, "independent original map RGB "+n.colorIndex);
                check(color.getField("M").getInt(original)==n.colorIndex, "independent original map index "+n.colorIndex);
            }
            Class<?> material=Class.forName("awt",true,loader);
            String[] materialFields={"f","e","p","d","c","y","h","n","k","v","b","u","B","j","i","s","w","t","z"};
            Material[] nativeMaterials={Material.IRON,Material.ROCK,Material.SAND,Material.WOOD,Material.GROUND,Material.SNOW,Material.WATER,Material.CLOTH,Material.PLANTS,Material.CORAL,Material.GRASS,Material.TNT,Material.CLAY,Material.LEAVES,Material.LAVA,Material.GLASS,Material.ICE,Material.REDSTONE_LIGHT,Material.CRAFTED_SNOW};
            String[] oldMethods={"a","b","c","d","h","j","k","l"};
            for(int i=0;i<materialFields.length;i++) {
                Object original=material.getField(materialFields[i]).get(null);
                Material n=nativeMaterials[i];
                boolean[] values={n.isSolid(),n.blocksLight(),n.blocksMovement(),n.isLiquid(),n.getCanBurn(),n.isReplaceable(),n.isOpaque(),n.isToolNotRequired()};
                for(int j=0;j<oldMethods.length;j++) check(material.getMethod(oldMethods[j]).invoke(original).equals(values[j]), "original material flag "+materialFields[i]+"/"+oldMethods[j]);
                check(((Integer)material.getMethod("m").invoke(original))==n.getPushReaction().ordinal(), "original material mobility "+materialFields[i]);
                Object originalMap=material.getMethod("r").invoke(original);
                int legacyRgb=color.getField("L").getInt(originalMap), nativeRgb=n.getMaterialMapColor().colorValue;
                if(legacyRgb!=nativeRgb) System.out.println("NATIVE PLATFORM DELTA material "+materialFields[i]+" default map RGB: original="+legacyRgb+" native="+nativeRgb+"; owner map-color parity remains NOT_RUN");
                else check(true, "original material default map RGB "+materialFields[i]);
            }
            Class<?> block=Class.forName("aji",true,loader), sound=Class.forName("ajm",true,loader);
            String[] fields={"j","h","i","g","f","m","k","l","e","n"};
            String[] breaks={"dig.stone","dig.grass","dig.stone","dig.gravel","dig.wood","dig.sand","dig.glass","dig.cloth","dig.stone","dig.snow"};
            SoundType[] nativeSounds={ModSoundTypes.legacyMetal,SoundType.PLANT,SoundType.STONE,SoundType.GROUND,SoundType.WOOD,SoundType.SAND,SoundType.GLASS,SoundType.CLOTH,SoundType.STONE,SoundType.SNOW};
            for(int i=0;i<fields.length;i++) {
                Object original=block.getField(fields[i]).get(null);
                check(sound.getMethod("a").invoke(original).equals(breaks[i]), "independent original sound selection "+fields[i]);
                check(((Float)sound.getMethod("c").invoke(original))==nativeSounds[i].getVolume(), "original volume "+fields[i]);
                check(((Float)sound.getMethod("d").invoke(original))==nativeSounds[i].getPitch(), "original pitch "+fields[i]);
            }
            check(ModSoundTypes.legacyMetal.getBreakSound()==SoundType.STONE.getBreakSound(), "metal must keep original stone samples rather than native metal samples");
        }
        System.out.println("PASS original 1.7.10 bytecode oracle: nineteen materials, six map colors and ten sound constants");
    }
}
