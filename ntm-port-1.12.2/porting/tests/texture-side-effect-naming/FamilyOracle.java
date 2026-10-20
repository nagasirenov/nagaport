import com.hbm.items.tool.*;
import com.hbm.render.item.*;
import com.hbm.render.icon.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import javax.imageio.ImageIO;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Exact production METHOD slices on genuine native platform classes, not whole-owner acceptance. */
public final class FamilyOracle {
 static int checks;
 static List<String> events = new ArrayList<>();
 static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
 public static class InterceptBlowtorch extends ItemBlowtorch {
  public Item setTranslationKey(String key) { events.add("override:" + key); Item result = super.setTranslationKey(key); events.add("after:" + NativeItemTextureRegistry.texture(this)); return result; }
 }
 public static class InterceptBoltgun extends ItemBoltgun {
  public Item setTranslationKey(String key) { events.add("override:" + key); Item result = super.setTranslationKey(key); events.add("after:" + NativeItemTextureRegistry.texture(this)); return result; }
 }
 public static class InterceptMinecart extends ItemModMinecart {
  public Item setTranslationKey(String key) { events.add("override:" + key); Item result = super.setTranslationKey(key); events.add("after:" + NativeItemTextureRegistry.texture(this)); return result; }
 }
 public static class InterceptDegradation extends ItemCraftingDegradation {
  public Item setTranslationKey(String key) { events.add("override:" + key); Item result = super.setTranslationKey(key); events.add("after:" + NativeItemTextureRegistry.texture(this)); return result; }
 }
 static Item receiver(Item item) { events.add("receiver"); return item; }
 static String key() { events.add("key"); return "sequenced"; }
 static String explicit(Item item) { events.add("texture:" + NativeItemTextureRegistry.texture(item)); return "hbm:explicit"; }
 public static class ThrowBefore extends ItemCraftingDegradation {
  public Item setTranslationKey(String key) { throw new IllegalArgumentException("before"); }
 }
 static final class RecordingIcons implements IIconRegister {
  final List<String> names = new ArrayList<>();
  public IIcon registerIcon(String name) { names.add(name); return null; }
 }
 public static void main(String[] args) throws Exception {
  Bootstrap.register();
  net.minecraftforge.fml.common.ModMetadata metadata = new net.minecraftforge.fml.common.ModMetadata(); metadata.modId = "hbm"; metadata.name = "Texture naming method proof"; metadata.version = "probe";
  net.minecraftforge.fml.common.DummyModContainer container = new net.minecraftforge.fml.common.DummyModContainer(metadata);
  net.minecraftforge.fml.common.Loader.instance().setupTestHarness(container);
  net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(container);
  Field raw = Item.class.getDeclaredField("translationKey"); raw.setAccessible(true);
  Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); field.setAccessible(true);
  sun.misc.Unsafe unsafe = (sun.misc.Unsafe)field.get(null);
  URLClassLoader legacy = new URLClassLoader(new URL[] { new File(args[0]).toURI().toURL() }, FamilyOracle.class.getClassLoader());
  Class<?> oldClass = legacy.loadClass("adb"); legacy.loadClass("aji").getMethod("p").invoke(null); oldClass.getMethod("l").invoke(null);
  Method oldName = oldClass.getMethod("c", String.class), oldGetName = oldClass.getMethod("a"), oldTexture = oldClass.getDeclaredMethod("f", String.class), oldGetTexture = oldClass.getDeclaredMethod("w");
  oldTexture.setAccessible(true); oldGetTexture.setAccessible(true);
  String[] keys = { null, "", "blowtorch", "item.cart", "tile.x", "hbm:x", "UPPER.Mixed", "spaces are literal", "ñ漢字", "foo.name", "foo.desc.P11", "%s\\n" };
  Class<?>[] types = { ItemBlowtorch.class, ItemBoltgun.class, ItemModMinecart.class, ItemCraftingDegradation.class, ItemTooling.class, ItemToolingWeapon.class, ItemDefuser.class };
  for (Class<?> type : types) {
   Item item = (Item)type.newInstance(); Object old = unsafe.allocateInstance(oldClass);
   for (String input : keys) {
    String key = input == null ? null : new String(input);
    oldName.invoke(old, key); oldTexture.invoke(old, "hbm:" + key);
    check(item.setTranslationKey(key) == item, "native fluent identity " + type);
    check(raw.get(item) == key, "native raw name parameter identity " + type);
    check(item.getTranslationKey().equals(oldGetName.invoke(old)), "genuine old/native translation behavior " + type);
    check(NativeItemTextureRegistry.texture(item).equals(oldGetTexture.invoke(old)), "genuine old texture value " + type);
    check(type.getMethod("setTranslationKey", String.class).getReturnType() == Item.class, "unchanged fluent return type " + type);
   }
   check(type.getMethod("setTranslationKey", String.class).getDeclaringClass() != Item.class, "custom family override, not base bypass");
   NativeItemTextureRegistry.texture(item, "hbm:explicit");
   check(NativeItemTextureRegistry.texture(item).equals("hbm:explicit"), "later explicit texture wins");
   item.setTranslationKey("renamed"); check(NativeItemTextureRegistry.texture(item).equals("hbm:renamed"), "later naming repeats original side effect");
   item.setRegistryName(new ResourceLocation("hbm", "proof_" + type.getSimpleName().toLowerCase(Locale.ROOT))); ForgeRegistries.ITEMS.register(item);
   Method eligible = NativeItemTextureModelHandler.class.getDeclaredMethod("eligible", Item.class); eligible.setAccessible(true);
   check(((Boolean)eligible.invoke(null, item)) == (type != ItemModMinecart.class), "real model eligibility preserves layered minecart owner " + type);
  }
  for (Item item : new Item[] { new InterceptBlowtorch(), new InterceptBoltgun(), new InterceptMinecart(), new InterceptDegradation() }) {
   events.clear(); Item result = NativeItemTextureRegistry.texture(receiver(item).setTranslationKey(key()).setMaxStackSize(3), explicit(item));
   check(result == item && item.getItemStackLimit() == 3, "fluent chain retains receiver and following effects");
   check(events.equals(Arrays.asList("receiver", "key", "override:sequenced", "after:hbm:sequenced", "texture:hbm:sequenced")), "virtual interception and left-to-right sequence " + events);
   check(NativeItemTextureRegistry.texture(item).equals("hbm:explicit"), "last-write explicit texture ordering");
  }
  Item throwing = new ThrowBefore(); boolean threw = false;
  try { throwing.setTranslationKey("must_not_apply"); } catch (IllegalArgumentException expected) { threw = true; }
  check(threw && raw.get(throwing) == null && NativeItemTextureRegistry.texture(throwing) == null, "subclass may intercept before base mutation");
  ItemModMinecart cart = new ItemModMinecart(); cart.setTranslationKey("cart");
  RecordingIcons icons = new RecordingIcons(); cart.registerIcons(icons);
  List<String> expected = Arrays.asList("hbm:cart.vanilla", "hbm:cart.wood", "hbm:cart.steel", "hbm:cart.painted", "hbm:cart_overlay.empty", "hbm:cart_overlay.crate", "hbm:cart_overlay.destroyer", "hbm:cart_overlay.powder", "hbm:cart_overlay.semtex");
  check(icons.names.equals(expected), "exact original layered cart names and order");
  for (String name : icons.names) {
   ResourceLocation atlas = NativeItemTextureRegistry.atlasLocation(name);
   check(atlas.toString().equals("hbm:items/" + name.substring(4)), "native atlas prefix for original cart layer");
   check(ImageIO.read(new File(args[1], "assets/hbm/textures/" + atlas.getPath() + ".png")) != null, "actual cart layer PNG decodes " + name);
  }
  for (String name : Arrays.asList("defuser", "screwdriver", "screwdriver_desh", "hand_drill", "hand_drill_desh", "wrench_archineer_hd", "chemistry_set", "chemistry_set_boron", "blowtorch", "acetylene_torch", "boltgun"))
   check(ImageIO.read(new File(args[1], "assets/hbm/textures/items/" + name + ".png")) != null, "actual final item PNG decodes " + name);
  legacy.close();
  System.out.println("PASS " + checks + " actual native method-slice assertions: all 7 family classes, 12 raw keys, old platform parity, virtual dispatch, last-write and fluent order, native model eligibility, 20 original PNGs; not whole owners or client launch");
 }
}
