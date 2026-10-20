import net.minecraft.block.Block;import net.minecraft.init.Blocks;
public class ExtractedNukeMaterial {
public static float getNukeResistance(Block b) {
		if (b.getDefaultState().getMaterial().isLiquid()) return 0.1F;
		if (b == Blocks.SANDSTONE) return Blocks.STONE.getExplosionResistance(null);
		if (b == Blocks.OBSIDIAN) return Blocks.STONE.getExplosionResistance(null) * 3.0F;
		return b.getExplosionResistance(null);
	}
}
