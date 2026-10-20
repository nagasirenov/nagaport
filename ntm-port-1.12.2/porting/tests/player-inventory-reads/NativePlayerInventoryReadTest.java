import com.hbm.util.PlayerInventoryRead;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

/** Actual native player/inventory constructors; no Minecraft/Forge surrogate classes. */
public final class NativePlayerInventoryReadTest {
    static int checks;
    static final StringBuilder trace = new StringBuilder();
    static Player owner;
    static InventoryPlayer replacement;
    static ItemStack marker;
    static int indexValue;
    static final class TestWorld extends World {
        TestWorld() {
            super(new SaveHandlerMP(), new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), false);
            provider.setWorld(this);
        }
        @Override protected IChunkProvider createChunkProvider() { return null; }
        @Override protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) { return false; }
    }
    static final class Player extends EntityPlayer {
        Player(World world) { super(world, new GameProfile(new UUID(0, 1), "InventoryReadTest")); }
        @Override public boolean isSpectator() { return false; }
        @Override public boolean isCreative() { return false; }
    }
    static void check(boolean ok, String label) {
        checks++;
        if (!ok) throw new AssertionError(label);
    }
    static Player receiver() { trace.append('R'); return owner; }
    static int index() { trace.append('I'); return indexValue; }
    static int replaceIndex() { trace.append('I'); owner.inventory = replacement; return indexValue; }
    static int mutateIndex() { trace.append('I'); owner.inventory.mainInventory.set(indexValue, marker); return indexValue; }
    static int throwIndex() { trace.append('I'); throw new IllegalStateException("index"); }
    static NonNullList<ItemStack> nullList() { trace.append('L'); return null; }
    static ItemStack[] nullArray() { trace.append('L'); return null; }
    interface Action { void run(); }
    static Throwable thrown(Action action) {
        try { action.run(); } catch (Throwable failure) { return failure; }
        throw new AssertionError("expected failure");
    }
    static void expressions(Player player) {
        InventoryPlayer inv = player.inventory;
        inv.currentItem = 7;
        ProductionExpressions.LegacyPlayer original = new ProductionExpressions.LegacyPlayer();
        original.inventory.currentItem = 7;
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.mainInventory.get(i);
            original.inventory.mainInventory[i] = s == ItemStack.EMPTY ? null : s;
        }
        for (int i = 0; i < 4; i++) {
            ItemStack s = inv.armorInventory.get(i);
            original.inventory.armorInventory[i] = s == ItemStack.EMPTY ? null : s;
        }
        for (int i = 0; i < ProductionExpressions.COUNT; i++) {
            Object expected = ProductionExpressions.original(i, original, original, original, original, original.inventory, original.inventory.armorInventory, 1, 2, 2);
            Object actual = ProductionExpressions.current(i, player, player, player, player, inv, inv.armorInventory, 1, 2, 2);
            check(actual instanceof Integer ? actual.equals(expected) : actual == expected, "exact migrated expression " + i);
        }
    }
    static void fill(NonNullList<ItemStack> inventory, ItemStack value) {
        for (int i = 0; i < inventory.size(); i++) inventory.set(i, value);
    }
    static void resetFaraday() {
        ProductionExpressions.predicateCalls = 0;
        ProductionExpressions.rejectAt = -1;
        ProductionExpressions.seen.clear();
        ProductionExpressions.replacement = null;
    }
    public static void main(String[] args) {
        Bootstrap.register();
        Player player = new Player(new TestWorld());
        InventoryPlayer inv = player.inventory;
        check(inv.getClass() == InventoryPlayer.class, "real native InventoryPlayer constructor");
        check(inv.mainInventory.size() == 36 && inv.armorInventory.size() == 4 && inv.offHandInventory.size() == 1, "native 36 main / 4 armor / 1 excluded offhand");
        check(inv.getSizeInventory() == 41, "not replaced with combined native inventory API");
        ItemStack offhand = new ItemStack(Items.DIAMOND);
        inv.offHandInventory.set(0, offhand);
        for (NonNullList<ItemStack> list : java.util.Arrays.asList(inv.mainInventory, inv.armorInventory)) {
            for (int i = 0; i < list.size(); i++) {
                check(list.get(i) == ItemStack.EMPTY && PlayerInventoryRead.get(list, i) == null, "canonical vacancy only maps to null");
                check(list.get(i) == ItemStack.EMPTY, "empty read does not mutate backing list");
            }
        }
        expressions(player);
        ItemStack zero = new ItemStack(Items.STICK, 0);
        NBTTagCompound tag = new NBTTagCompound(); tag.setString("payload", "retained"); zero.setTagCompound(tag);
        ItemStack negative = new ItemStack(Items.STICK, -1);
        ItemStack air = new ItemStack(Items.AIR, 1);
        ItemStack invalid = new ItemStack((Item)null, 1);
        ItemStack invalidNbt = new ItemStack(new NBTTagCompound());
        ItemStack full = new ItemStack(Items.STICK, 5);
        ItemStack[] cases = { full, zero, negative, air, invalid, invalidNbt };
        for (ItemStack s : cases) {
            check(s != ItemStack.EMPTY, "noncanonical fixture identity");
            int count = s.getCount(); NBTTagCompound payload = s.getTagCompound();
            fill(inv.mainInventory, s); fill(inv.armorInventory, s);
            for (NonNullList<ItemStack> list : java.util.Arrays.asList(inv.mainInventory, inv.armorInventory)) {
                for (int i = 0; i < list.size(); i++) check(PlayerInventoryRead.get(list, i) == s, "all noncanonical identities retained");
            }
            expressions(player);
            check(s.getCount() == count && s.getTagCompound() == payload, "read does not normalize or mutate stack/payload");
        }
        check(zero.isEmpty() && negative.isEmpty() && air.isEmpty() && invalid.isEmpty() && invalidNbt.isEmpty(), "native malformed fixtures really are empty");
        check(zero.getTagCompound() == tag && zero.getItem() == Items.AIR, "payload preserved; native zero getItem AIR explicitly not legacy emulated");
        for (int i = 0; i < 36; i++) inv.mainInventory.set(i, new ItemStack(Items.STICK, i + 1));
        for (int i = 0; i < 4; i++) inv.armorInventory.set(i, new ItemStack(Items.DIAMOND, i + 1));
        expressions(player);
        check(inv.offHandInventory.get(0) == offhand, "offhand untouched by reads and size migrations");
        ItemStack alias = PlayerInventoryRead.get(inv.mainInventory, 0); alias.setCount(17);
        check(inv.mainInventory.get(0) == alias && inv.mainInventory.get(0).getCount() == 17, "nonnull alias mutation reaches original stack");
        // Field access is evaluated before the index, and a replacement owner in
        // the index cannot retarget the already-captured list.
        owner = player; replacement = new InventoryPlayer(player); marker = new ItemStack(Items.IRON_INGOT);
        replacement.mainInventory.set(0, marker); indexValue = 0; trace.setLength(0);
        ItemStack captured = PlayerInventoryRead.get(receiver().inventory.mainInventory, replaceIndex());
        check(captured == alias && owner.inventory == replacement && trace.toString().equals("RI"), "receiver field captured before side-effecting index");
        owner.inventory = inv; trace.setLength(0);
        check(PlayerInventoryRead.get(receiver().inventory.mainInventory, mutateIndex()) == marker && trace.toString().equals("RI"), "slot value read after index mutation");
        owner = null; trace.setLength(0);
        check(thrown(() -> PlayerInventoryRead.get(receiver().inventory.mainInventory, index())) instanceof NullPointerException && trace.toString().equals("R"), "null owner fails before index evaluation");
        owner = player; player.inventory = null; trace.setLength(0);
        check(thrown(() -> PlayerInventoryRead.get(receiver().inventory.mainInventory, index())) instanceof NullPointerException && trace.toString().equals("R"), "null inventory owner fails before index evaluation");
        player.inventory = inv;
        trace.setLength(0);
        check(thrown(() -> PlayerInventoryRead.get(nullList(), index())) instanceof NullPointerException && trace.toString().equals("LI"), "null captured list evaluates index then fails");
        trace.setLength(0);
        check(thrown(() -> { ItemStack s = nullArray()[index()]; }) instanceof NullPointerException && trace.toString().equals("LI"), "original null array has same index timing");
        trace.setLength(0);
        check(thrown(() -> PlayerInventoryRead.get(nullList(), throwIndex())) instanceof IllegalStateException && trace.toString().equals("LI"), "throwing index precedes null list failure");
        trace.setLength(0);
        check(thrown(() -> { ItemStack s = nullArray()[throwIndex()]; }) instanceof IllegalStateException && trace.toString().equals("LI"), "original null array also propagates index failure first");
        for (NonNullList<ItemStack> list : java.util.Arrays.asList(inv.mainInventory, inv.armorInventory)) {
            for (int bad : new int[]{-1, list.size(), Integer.MIN_VALUE, Integer.MAX_VALUE}) {
                indexValue = bad; trace.setLength(0);
                Throwable nativeFailure = thrown(() -> PlayerInventoryRead.get(list, index()));
                check(nativeFailure instanceof IndexOutOfBoundsException && trace.toString().equals("I"), "native invalid bound after index, no fallback");
                ItemStack[] original = new ItemStack[list.size()]; trace.setLength(0);
                Throwable oldFailure = thrown(() -> { ItemStack s = original[index()]; });
                check(oldFailure instanceof ArrayIndexOutOfBoundsException && trace.toString().equals("I"), "old invalid bound after index");
                check(nativeFailure.getClass() == oldFailure.getClass(), "actual fixed-size native backing keeps array bound exception class");
            }
        }
        check(thrown(() -> PlayerInventoryRead.get(NonNullList.<ItemStack>create(), 0)) instanceof IndexOutOfBoundsException, "non-domain dynamic list uses native bounds contract");
        fill(inv.armorInventory, full); resetFaraday();
        check(ProductionExpressions.checkForFaraday(player) && ProductionExpressions.predicateCalls == 4, "complete Faraday body all four predicates");
        for (int empty = 0; empty < 4; empty++) {
            fill(inv.armorInventory, full); inv.armorInventory.set(empty, ItemStack.EMPTY); resetFaraday();
            check(!ProductionExpressions.checkForFaraday(player) && ProductionExpressions.predicateCalls == 0, "Faraday any native vacancy skips all predicates");
        }
        for (int rejected = 0; rejected < 4; rejected++) {
            fill(inv.armorInventory, zero); resetFaraday(); ProductionExpressions.rejectAt = rejected;
            check(!ProductionExpressions.checkForFaraday(player) && ProductionExpressions.predicateCalls == rejected + 1, "Faraday noncanonical empty retained and predicate short circuit");
            for (ItemStack seen : ProductionExpressions.seen) check(seen == zero, "Faraday predicate preserves noncanonical identity");
        }
        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < 4; i++) { armor[i] = new ItemStack(Items.DIAMOND, i + 1); inv.armorInventory.set(i, armor[i]); }
        resetFaraday(); ProductionExpressions.replaceOwner = player; ProductionExpressions.replacement = new InventoryPlayer(player);
        check(ProductionExpressions.checkForFaraday(player), "Faraday captures list before predicate changes owner");
        for (int i = 0; i < 4; i++) check(ProductionExpressions.seen.get(i) == armor[i], "Faraday original captured list and predicate order");
        player.inventory = inv; resetFaraday();
        check(thrown(() -> ProductionExpressions.checkForFaraday(null)) instanceof NullPointerException, "Faraday null owner retained");
        System.out.println("PASS " + checks + " native InventoryPlayer/ItemStack and exact-source expression/Faraday checks.");
        System.out.println("LIMIT: this proves indexed and size reads only; foreach, mutable whole-array consumers, native stack semantics and gameplay remain separate.");
    }
}
