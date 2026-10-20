import java.util.Random;

import com.hbm.world.gen.LegacyStructureOrientation;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

/** Isolated real target API/NBT test, not a game, terrain or production component test. */
public final class StructureOrientationTest {
    private static void require(boolean value, String message) {
        if(!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        Bootstrap.register();
        EnumFacing[] expected = {EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST};
        MapGenStructureIO.registerStructureComponent(Fixture.class, "hbm_orientation_test");
        World world = new NbtOnlyWorld();
        for(int mode = -1; mode < 4; mode++) {
            EnumFacing facing = LegacyStructureOrientation.fromLegacy(mode);
            require(facing == (mode == -1 ? null : expected[mode]), "legacy horizontal order");
            require(LegacyStructureOrientation.toLegacy(facing) == mode, "mode roundtrip");
            Fixture before = new Fixture();
            require(LegacyStructureOrientation.toLegacy(before.getCoordBaseMode()) == -1, "target default/null orientation");
            before.setCoordBaseMode(facing);
            NBTTagCompound nbt = before.createStructureBaseNBT();
            require(nbt.getInteger("O") == mode, "target NBT O preserves legacy integer");
            Fixture restored = new Fixture();
            restored.poison();
            restored.readStructureBaseNBT(world, nbt);
            require(LegacyStructureOrientation.toLegacy(restored.getCoordBaseMode()) == mode, "actual target NBT load");
            require(restored.payload == 37, "component payload callback");
            StructureBoundingBox bounds = restored.getBoundingBox();
            require(bounds.minX == -21 && bounds.minY == 4 && bounds.minZ == 9 && bounds.maxX == 8 && bounds.maxY == 15 && bounds.maxZ == 43, "all six bounds restored");
        }
        for(int invalid : new int[] {-2, 4, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            try {
                LegacyStructureOrientation.fromLegacy(invalid);
                throw new AssertionError("accepted invalid mode " + invalid);
            } catch(IllegalArgumentException expectedFailure) { }
        }
        for(EnumFacing vertical : new EnumFacing[] {EnumFacing.UP, EnumFacing.DOWN}) {
            try {
                LegacyStructureOrientation.toLegacy(vertical);
                throw new AssertionError("accepted vertical facing");
            } catch(IllegalArgumentException expectedFailure) { }
        }
        Random random = new Random(0x48524f54415445L);
        int checks = 0;
        for(int i = 0; i < 2000; i++) {
            int minX = random.nextInt(2001) - 1000;
            int minZ = random.nextInt(2001) - 1000;
            int width = random.nextInt(64) + 1;
            int depth = random.nextInt(64) + 1;
            int x = random.nextInt(width);
            int z = random.nextInt(depth);
            for(int mode = -1; mode < 4; mode++) {
                StructureBoundingBox box = new StructureBoundingBox(minX, 0, minZ,
                    minX + ((mode & 1) == 1 ? depth : width) - 1, 12,
                    minZ + ((mode & 1) == 1 ? width : depth) - 1);
                int expectedX = x, expectedZ = z;
                switch(mode) {
                case 0: expectedX = minX + x; expectedZ = minZ + z; break;
                case 1: expectedX = minX + depth - 1 - z; expectedZ = minZ + x; break;
                case 2: expectedX = minX + width - 1 - x; expectedZ = minZ + depth - 1 - z; break;
                case 3: expectedX = minX + z; expectedZ = minZ + width - 1 - x; break;
                default: break;
                }
                require(LegacyStructureOrientation.offsetX(box, mode, x, z) == expectedX, "custom rotation X");
                require(LegacyStructureOrientation.offsetZ(box, mode, x, z) == expectedZ, "custom rotation Z");
                checks++;
            }
        }
        EnumFacing facing = EnumFacing.SOUTH;
        for(int i = 0; i < 4; i++) facing = facing.rotateY();
        require(facing == EnumFacing.SOUTH, "four turns");
        require(LegacyStructureOrientation.toLegacy(EnumFacing.SOUTH.getOpposite()) == 2, "opposite direction");
        System.out.println("PASS: five target StructureComponent NBT roundtrips, horizontal/null/invalid modes, " + checks + " coordinate pairs, four turns and opposite direction.");
        System.out.println("SCOPE: production LegacyStructureOrientation and real Forge/Minecraft APIs; complete NTM components and in-game persistence are NOT verified.");
    }

    public static final class Fixture extends StructureComponent {
        int payload = 37;
        public Fixture() { super(0); boundingBox = new StructureBoundingBox(-21, 4, 9, 8, 15, 43); }
        void poison() { payload = -999; boundingBox = new StructureBoundingBox(100, 101, 102, 103, 104, 105); }
        protected void writeStructureToNBT(NBTTagCompound nbt) { nbt.setInteger("payload", payload); }
        protected void readStructureFromNBT(NBTTagCompound nbt, TemplateManager templates) { payload = nbt.getInteger("payload"); }
        public boolean addComponentParts(World world, Random random, StructureBoundingBox box) { throw new UnsupportedOperationException("NBT-only fixture"); }
    }

    /** No chunks or gameplay: only supplies the real SaveHandlerMP for NBT loading. */
    private static final class NbtOnlyWorld extends World {
        NbtOnlyWorld() { super(new SaveHandlerMP(), new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), false); }
        protected IChunkProvider createChunkProvider() { throw new UnsupportedOperationException("NBT-only fixture"); }
        protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) { throw new UnsupportedOperationException("NBT-only fixture"); }
    }
}
