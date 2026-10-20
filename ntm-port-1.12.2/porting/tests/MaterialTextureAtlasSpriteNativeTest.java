import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;
import java.util.*;
import javax.imageio.ImageIO;
import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.mojang.authlib.properties.PropertyMap;
import com.hbm.render.item.nativeitem.MaterialTextureAtlasSprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

/** Real Forge 2768 resource load, mip generation, stitch, GL upload and readback.
 * Uses Mesa surfaceless EGL, not a game window or a whole-client parity test.
 * All Minecraft/Forge/LWJGL classes come unchanged from the actual native jars.
 */
public final class MaterialTextureAtlasSpriteNativeTest {
    private static int checks;
    private static void check(boolean condition, String label) {
        checks++;
        if (!condition) throw new AssertionError(label);
    }
    public interface EGL extends Library {
        Pointer eglGetPlatformDisplay(int platform, Pointer display, Pointer attributes);
        int eglInitialize(Pointer display, IntByReference major, IntByReference minor);
        int eglBindAPI(int api);
        int eglChooseConfig(Pointer display, int[] attributes, Pointer[] config, int count, IntByReference found);
        Pointer eglCreatePbufferSurface(Pointer display, Pointer config, int[] attributes);
        Pointer eglCreateContext(Pointer display, Pointer config, Pointer shared, int[] attributes);
        int eglMakeCurrent(Pointer display, Pointer draw, Pointer read, Pointer context);
        int eglDestroyContext(Pointer display, Pointer context);
        int eglDestroySurface(Pointer display, Pointer surface);
        int eglTerminate(Pointer display);
    }
    private static final class Context implements AutoCloseable {
        final EGL egl = (EGL) Native.loadLibrary("EGL", EGL.class);
        final Pointer display, surface, context;
        Context() throws Exception {
            display = egl.eglGetPlatformDisplay(0x31DD, null, null); // EGL_PLATFORM_SURFACELESS_MESA
            check(egl.eglInitialize(display, new IntByReference(), new IntByReference()) != 0, "real EGL initialized");
            check(egl.eglBindAPI(0x30A2) != 0, "desktop OpenGL API");
            Pointer[] config = new Pointer[1];
            IntByReference count = new IntByReference();
            check(egl.eglChooseConfig(display, new int[]{0x3033,1,0x3040,8,0x3024,8,0x3023,8,0x3022,8,0x3038}, config, 1, count) != 0
                    && count.getValue() == 1, "real EGL pbuffer configuration");
            surface = egl.eglCreatePbufferSurface(display, config[0], new int[]{0x3057,1,0x3056,1,0x3038});
            context = egl.eglCreateContext(display, config[0], null, new int[]{0x3038});
            check(surface != null && context != null, "real offscreen surface and context");
            check(egl.eglMakeCurrent(display, surface, surface, context) != 0, "real GL context current");
            GLContext.useContext(context);
            System.out.println("Actual OpenGL: " + GL11.glGetString(GL11.GL_VERSION));
        }
        public void close() throws Exception {
            GLContext.useContext(null);
            egl.eglMakeCurrent(display, null, null, null);
            egl.eglDestroyContext(display, context);
            egl.eglDestroySurface(display, surface);
            egl.eglTerminate(display);
        }
    }
    private static void initializeNativeClientSettings() {
        File directory = new File(".");
        GameConfiguration config = new GameConfiguration(
                new GameConfiguration.UserInformation(new Session("AtlasProbe", "00000000-0000-0000-0000-000000000000", "", "legacy"),
                        new PropertyMap(), new PropertyMap(), java.net.Proxy.NO_PROXY),
                new GameConfiguration.DisplayInformation(1, 1, false, false),
                new GameConfiguration.FolderInformation(directory, directory, directory, null),
                new GameConfiguration.GameInformation(false, "1.12.2", "native material atlas probe"),
                new GameConfiguration.ServerInformation(null, 0));
        // Real constructors, no Unsafe, transformed classes, stubs or game loop.
        Minecraft minecraft = new Minecraft(config);
        minecraft.gameSettings = new GameSettings();
    }
    public static void main(String[] args) throws Exception {
        File resourceRoot = new File(args[0]);
        List<ResourceLocation> textures = new ArrayList<>();
        for (int i = 1; i < args.length; i++) textures.add(new ResourceLocation(args[i]));
        check(!textures.isEmpty(), "actual repository materials supplied");
        SimpleReloadableResourceManager resources = new SimpleReloadableResourceManager(new MetadataSerializer());
        resources.reloadResourcePack(new FolderResourcePack(resourceRoot));
        try (Context context = new Context()) {
            initializeNativeClientSettings();
            // The regular launcher supplies this actual jar location before the
            // native splash helper initializes its resource pack/max-size query.
            net.minecraftforge.fml.common.asm.FMLSanityChecker.fmlLocation =
                    new File(TextureMap.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            TextureMap collisionProbe = new TextureMap("textures");
            TextureAtlasSprite ordinary = collisionProbe.registerSprite(new ResourceLocation("hbm", "entity/bfa_body"));
            boolean rectangularRejected = false;
            try (IResource resource = resources.getResource(new ResourceLocation("hbm", "textures/entity/bfa_body.png"))) {
                try { ordinary.loadSprite(PngSizeInfo.makeFromResource(resource), false); }
                catch (RuntimeException expected) { rectangularRejected = expected.getMessage().contains("broken aspect ratio"); }
            }
            check(rectangularRejected, "native ordinary loader reproduces original rectangular PNG defect");
            MaterialTextureAtlasSprite isolated = MaterialTextureAtlasSprite.register(collisionProbe,
                    new ResourceLocation("hbm", "textures/entity/bfa_body.png"));
            check(collisionProbe.getTextureExtry("hbm:entity/bfa_body") == ordinary
                    && collisionProbe.getTextureExtry(isolated.getIconName()) == isolated, "dedicated material key preserves ordinary sprite ownership");
            for (int mip = 0; mip <= 4; mip++) {
                TextureMap map = new TextureMap("textures");
                map.setMipmapLevels(mip);
                List<MaterialTextureAtlasSprite> sprites = new ArrayList<>();
                // Actual map loader controls custom-loader return handling, inherited
                // native mip generation, native Stitcher allocations, and GL upload.
                for (ResourceLocation texture : textures) {
                    MaterialTextureAtlasSprite sprite = MaterialTextureAtlasSprite.register(map, texture);
                    check(MaterialTextureAtlasSprite.register(map, texture) == sprite, "deduplicated material");
                    sprites.add(sprite);
                }
                // Invoke the real missing-image initialization without the global
                // pre-stitch subscribers, which need a fully initialized game.
                java.lang.reflect.Method init = TextureMap.class.getDeclaredMethod("initMissingImage");
                init.setAccessible(true);
                init.invoke(map);
                map.loadTextureAtlas(resources);
                check(map.getMipmapLevels() == mip, "native atlas did not reduce configured mip level");
                GlStateManager.bindTexture(map.getGlTextureId());
                for (int level = 0; level <= mip; level++) {
                    int atlasWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, level, GL11.GL_TEXTURE_WIDTH);
                    int atlasHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, level, GL11.GL_TEXTURE_HEIGHT);
                    IntBuffer readback = BufferUtils.createIntBuffer(atlasWidth * atlasHeight);
                    GL11.glGetTexImage(GL11.GL_TEXTURE_2D, level, 32993, 33639, readback);
                    for (int i = 0; i < sprites.size(); i++) {
                        MaterialTextureAtlasSprite sprite = sprites.get(i);
                        check(map.getAtlasSprite(sprite.getIconName()) == sprite, "native false-return path actually stitched material");
                        int side = sprite.getIconWidth() >> level;
                        int[] frame = sprite.getFrameTextureData(0)[level];
                        check(sprite.getIconWidth() == sprite.getIconHeight(), "rotation-safe square canvas");
                        check(frame.length == side * side, "native mip dimensions exact");
                        int ox = sprite.getOriginX() >> level, oy = sprite.getOriginY() >> level;
                        check(ox >= 0 && oy >= 0 && ox + side <= atlasWidth && oy + side <= atlasHeight, "upload rectangle inside real atlas");
                        for (int y = 0; y < side; y++) for (int x = 0; x < side; x++)
                            check(frame[y * side + x] == readback.get((oy + y) * atlasWidth + ox + x), "native uploaded mip equals generated pixels");
                        if (level == 0) {
                            ResourceLocation texture = textures.get(i);
                            BufferedImage original = ImageIO.read(new File(resourceRoot, "assets/" + texture.getNamespace() + "/" + texture.getPath()));
                            check(sprite.getSourceWidth() == original.getWidth() && sprite.getSourceHeight() == original.getHeight(), "original source dimensions retained");
                            for (int y = 0; y < side; y++) for (int x = 0; x < side; x++)
                                check(frame[y * side + x] == original.getRGB(Math.min(x, original.getWidth()-1), Math.min(y, original.getHeight()-1)), "original rectangle unchanged and padding edge-extended");
                            int[][] oldFrame = sprite.getFrameTextureData(0);
                            check(!sprite.load(resources, texture, ignored -> null), "reload returns continue-stitch flag");
                            check(sprite.getFrameCount() == 1 && sprite.getFrameTextureData(0) != oldFrame, "reload replaces frame rather than appending");
                            sprite.generateMipmaps(mip);
                        }
                    }
                }
                for (int i = 0; i < sprites.size(); i++) for (int j = i + 1; j < sprites.size(); j++) {
                    TextureAtlasSprite a = sprites.get(i), b = sprites.get(j);
                    check(a.getOriginX()+a.getIconWidth() <= b.getOriginX() || b.getOriginX()+b.getIconWidth() <= a.getOriginX()
                            || a.getOriginY()+a.getIconHeight() <= b.getOriginY() || b.getOriginY()+b.getIconHeight() <= a.getOriginY(), "real stitch slots do not overlap");
                }
                check(GL11.glGetError() == GL11.GL_NO_ERROR, "native atlas GL upload without errors");
                map.deleteGlTexture();
                System.out.println("PASS native material atlas at mip level " + mip + ": " + textures.size() + " actual PNGs");
            }
        }
        System.out.println("PASS " + checks + " actual native material pixel/mipmap/stitch/upload assertions; isolated offscreen atlas only, no whole-client or visual parity claim.");
    }
}
