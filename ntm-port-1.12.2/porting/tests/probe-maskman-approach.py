#!/usr/bin/env python3
"""Compile and exercise the complete production Maskman AI on the real target classpath.

reverse_source is an audit-only view of this exact class before the native API edit;
it never rewrites production files or supplies compilation shims.
"""
from pathlib import Path
import argparse
import hashlib
import os
import subprocess
import tempfile

RELATIVE = 'src/main/java/com/hbm/entity/mob/ai/EntityAIMaskmanCasualApproach.java'
BASELINE_SHA256 = 'b04641b1ecd841b6c24e8415509b084430129a2a2b19b0a3a9f1fd7be38e18e1'
# Every mapping is intentionally limited to this class and this migration slice.
REPLACEMENTS = [
    ('import net.minecraft.pathfinding.PathEntity;', 'import net.minecraft.pathfinding.Path;'),
    ('import net.minecraft.util.math.MathHelper;', 'import net.minecraft.util.math.MathHelper;\nimport net.minecraft.util.math.BlockPos;'),
    ('PathEntity entityPathEntity', 'Path entityPathEntity'),
    ('owner.worldObj', 'owner.getEntityWorld()'),
    ('continueExecuting()', 'shouldContinueExecuting()'),
    ('this.attacker.isWithinHomeDistance(MathHelper.floor_double(entitylivingbase.posX), MathHelper.floor_double(entitylivingbase.posY), MathHelper.floor_double(entitylivingbase.posZ))',
     'this.attacker.isWithinHomeDistanceFromPosition(new BlockPos(MathHelper.floor(entitylivingbase.posX), MathHelper.floor(entitylivingbase.posY), MathHelper.floor(entitylivingbase.posZ)))'),
    ('.clearPathEntity()', '.clearPath()'),
    ('entitylivingbase.boundingBox', 'entitylivingbase.getEntityBoundingBox()'),
    ('finalPathPoint.xCoord', 'finalPathPoint.x'),
    ('finalPathPoint.yCoord', 'finalPathPoint.y'),
    ('finalPathPoint.zCoord', 'finalPathPoint.z'),
]
for method in ['boolean shouldExecute', 'boolean shouldContinueExecuting', 'void startExecuting', 'void resetTask', 'void updateTask']:
    REPLACEMENTS.append(('\tpublic ' + method, '\t@Override\n\tpublic ' + method))


def reverse_source(rel, text):
    if Path(rel).as_posix() != RELATIVE:
        return text
    old = text
    for before, after in reversed(REPLACEMENTS):
        old = old.replace(after, before)
    assert hashlib.sha256(old.encode('utf-8')).hexdigest() == BASELINE_SHA256, 'Maskman full-class source parity changed beyond approved API mapping'
    restored = old
    for before, after in REPLACEMENTS:
        assert before in restored, 'Missing native migration input: ' + before
        restored = restored.replace(before, after)
    assert restored == text, 'Incomplete or unexpected native migration'
    return old


def check_source(root):
    reverse_source(RELATIVE, (root / RELATIVE).read_text(encoding='utf-8'))
    print('PASS: exact whole-class reverse-source SHA-256 parity, including ordering and disabled attack comment.', flush=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--java-home', type=Path)
    parser.add_argument('--classpath')
    parser.add_argument('--source-only', action='store_true')
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[2]
    check_source(root)
    if args.source_only:
        return
    if not args.java_home or not args.classpath:
        parser.error('--java-home and --classpath are required unless --source-only')
    suffix = '.exe' if os.name == 'nt' else ''
    with tempfile.TemporaryDirectory(prefix='hbm-maskman-approach-') as output:
        subprocess.run([str(args.java_home / 'bin' / ('javac' + suffix)), '-source', '8', '-target', '8',
                        '-encoding', 'UTF-8', '-cp', args.classpath, '-d', output,
                        str(root / RELATIVE), str(root / 'porting/tests/MaskmanApproachMinecraftTest.java')], check=True)
        subprocess.run([str(args.java_home / 'bin' / ('java' + suffix)), '-cp', output + os.pathsep + args.classpath,
                        'MaskmanApproachMinecraftTest'], check=True)


if __name__ == '__main__':
    main()
