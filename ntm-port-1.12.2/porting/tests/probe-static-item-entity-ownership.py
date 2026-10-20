#!/usr/bin/env python3
"""Complete production ownership seam against actual Forge/Minecraft, without GL startup."""
import argparse
import os
import re
import pathlib
import subprocess
import tempfile

parser = argparse.ArgumentParser()
parser.add_argument('--classpath', required=True)
parser.add_argument('--java-home', type=pathlib.Path, required=True)
args = parser.parse_args()
ext = '.exe' if os.name == 'nt' else ''
root = pathlib.Path(__file__).resolve().parents[2]
source = root / 'src/main/java/com/hbm/render/item/nativeitem/staticfamily'
files = [source / 'SingleMeshEntityItemRenderer.java', source / 'StaticItemEntityOwnership.java',
         root / 'porting/tests/StaticItemEntityOwnershipTest.java']
with tempfile.TemporaryDirectory(prefix='static-item-entity-ownership-') as temporary:
    subprocess.run([str(args.java_home / ('bin/javac' + ext)), '-source', '8', '-target', '8', '-encoding', 'UTF-8',
                    '-cp', args.classpath, '-d', temporary] + [str(path) for path in files], check=True)
    subprocess.run([str(args.java_home / ('bin/java' + ext)), '-cp', temporary + os.pathsep + args.classpath,
                    'com.hbm.render.item.nativeitem.staticfamily.StaticItemEntityOwnershipTest'],
                   cwd=temporary, check=True)

# Exact pinned native dispatch audit; this is bytecode evidence, not transformed event-bus execution.
bytecode = subprocess.check_output([str(args.java_home / ('bin/javap' + ext)), '-c', '-p',
    '-classpath', args.classpath, 'net.minecraftforge.fml.common.FMLCommonHandler'], universal_newlines=True)
assert re.search(r'MinecraftForge\.EVENT_BUS.*?putfield[^\n]*eventBus', bytecode, re.S)
bus = bytecode.split('public net.minecraftforge.fml.common.eventhandler.EventBus bus();', 1)[1].split('\n  public ', 1)[0]
assert 'getfield' in bus and 'eventBus' in bus
render = bytecode.split('public void onRenderTickStart(float);', 1)[1].split('\n  public ', 1)[0]
assert 'bus:()' in render and 'TickEvent$RenderTickEvent' in render and 'Phase.START' in render and 'EventBus.post' in render
print('PASS 3 separately labeled actual-native tick dispatch/bus alias bytecode checks; no transformed event-bus execution claim.')
