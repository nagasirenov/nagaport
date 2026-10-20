#!/usr/bin/env python3
"""Real native material atlas integration using existing Mesa EGL and native jars.
No substitute Minecraft/Forge classes, bytecode patches, or fabricated images.
"""
import argparse, os, pathlib, subprocess, tempfile, zipfile
p = argparse.ArgumentParser()
p.add_argument('--classpath', required=True)
p.add_argument('--java-home', type=pathlib.Path, required=True)
p.add_argument('--material', action='append')
a = p.parse_args()
root = pathlib.Path(__file__).resolve().parents[2]
materials = a.material or [
    'hbm:textures/entity/bfa_body.png',
    'hbm:textures/models/machines/oilburner.png',
    'hbm:textures/models/machines/furnace_steel.png',
    'hbm:textures/models/machines/heater_heatex.png',
    'hbm:textures/models/machines/heating_oven.png',
    'hbm:textures/models/machines/combination_oven.png',
    'hbm:textures/blocks/geiger.png',
    'hbm:textures/blocks/solar_mirror.png',
]
files = [root/'src/main/java/com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java',
         root/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java']
with tempfile.TemporaryDirectory(prefix='material-atlas-native-') as directory:
    tmp = pathlib.Path(directory)
    for jar in a.classpath.split(os.pathsep):
        if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
            with zipfile.ZipFile(jar) as archive:
                for member in archive.namelist():
                    if member.endswith('.so') and '/' not in member:
                        (tmp/member).write_bytes(archive.read(member))
    if not (tmp/'liblwjgl64.so').is_file():
        raise SystemExit('Native Linux LWJGL jar is required for the genuine offscreen GL atlas probe.')
    subprocess.run([str(a.java_home/'bin/javac'), '-source', '8', '-target', '8', '-encoding', 'UTF-8',
                    '-cp', a.classpath, '-d', directory]+list(map(str, files)), check=True)
    env = dict(os.environ, MESA_SHADER_CACHE_DISABLE='true')
    subprocess.run([str(a.java_home/'bin/java'), '-Djava.awt.headless=true', '-Djava.library.path='+directory,
                    '-cp', directory+os.pathsep+a.classpath, 'MaterialTextureAtlasSpriteNativeTest',
                    str(root/'src/main/resources')]+materials, cwd=directory, env=env, check=True)
