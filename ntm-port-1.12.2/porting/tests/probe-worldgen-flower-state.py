#!/usr/bin/env python3
"""Compile extracted production flower-state methods against real target Minecraft.
Not a compile of BlockNTMFlower or its unported base/rendering interfaces.
Usage: python3 ... --java-home JDK8 --classpath EXACT_TARGET_CLASSPATH
"""
import argparse
from pathlib import Path
import re
import subprocess
import tempfile

parser = argparse.ArgumentParser()
parser.add_argument('--java-home', type=Path, required=True)
parser.add_argument('--classpath', required=True)
a = parser.parse_args()
project = Path(__file__).resolve().parents[2]
source = (project / 'src/main/java/com/hbm/blocks/generic/BlockNTMFlower.java').read_text()

def method(signature):
    start = source.index(signature)
    body = source.index('{', start)
    end, depth = body + 1, 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]

field = re.search(r'public static final PropertyInteger VARIANT = .*?;', source).group()
methods = '\n'.join(method(signature) for signature in [
    'protected BlockStateContainer createBlockState()',
    'public IBlockState getStateFromMeta(int meta)',
    'public int getMetaFromState(IBlockState state)'])
java = '''import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldGenMinable;
public class WorldgenFlowerStateProbe extends Block {
%s
%s
private WorldgenFlowerStateProbe() { super(Material.PLANTS); }
public static void main(String[] args) throws Exception {
    Bootstrap.register();
    WorldgenFlowerStateProbe flower = new WorldgenFlowerStateProbe();
    for(int meta = -32; meta < 48; meta++) {
        if(flower.getMetaFromState(flower.getStateFromMeta(meta)) != (meta & 15))
            throw new AssertionError("metadata " + meta);
    }
    for(int meta = 0; meta <= 6; meta++) {
        IBlockState stone = Blocks.STONE.getStateFromMeta(meta);
        if(!BlockMatcher.forBlock(Blocks.STONE).apply(stone)) throw new AssertionError("stone variant");
        if(!stone.getBlock().isReplaceableOreGen(stone, null, BlockPos.ORIGIN, BlockMatcher.forBlock(Blocks.STONE)))
            throw new AssertionError("Forge ore hook");
    }
    if(BlockMatcher.forBlock(Blocks.STONE).apply(Blocks.DIRT.getDefaultState())) throw new AssertionError("dirt");
    WorldGenMinable generator = new WorldGenMinable(flower.getStateFromMeta(7), 6, BlockMatcher.forBlock(Blocks.NETHERRACK));
    java.lang.reflect.Field field = WorldGenMinable.class.getDeclaredField("oreBlock");
    field.setAccessible(true);
    if(flower.getMetaFromState((IBlockState) field.get(generator)) != 7) throw new AssertionError("ore metadata");
    System.out.println("PASS: extracted production flower state methods, all nibble values/invalid inputs, exact-target BlockMatcher and Forge ore hook; no full-class or in-game claim");
}
}
''' % (field, methods)
with tempfile.TemporaryDirectory(prefix='hbm-worldgen-state-') as out:
    path = Path(out) / 'WorldgenFlowerStateProbe.java'
    path.write_text(java)
    subprocess.run([str(a.java_home/'bin/javac'), '-cp', a.classpath, '-d', out, str(path)], check=True)
    subprocess.run([str(a.java_home/'bin/java'), '-cp', out + ':' + a.classpath, 'WorldgenFlowerStateProbe'], check=True, cwd=out)
