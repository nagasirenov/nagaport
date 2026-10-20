#!/usr/bin/env python3
"""Compile and execute complete production RecipesCommon against real native APIs.
Only three NTM identity/config/display collaborators are supplied. No Minecraft or
Forge classes, source rewriting, Git object or original checkout are required.
"""
import argparse
import hashlib
import importlib.util
import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
COLLABORATORS = {
    'com/hbm/config/GeneralConfig.java': '''package com.hbm.config;
public class GeneralConfig { public static boolean enableSilentCompStackErrors = true; }
''',
    'com/hbm/items/ModItems.java': '''package com.hbm.items;
public class ModItems { public static net.minecraft.item.Item nothing; }
''',
    'com/hbm/main/MainRegistry.java': '''package com.hbm.main;
public class MainRegistry {
 public static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger("recipe-key-probe");
 public static final ProbeProxy proxy = new ProbeProxy();
 public static class ProbeProxy {
  public int calls; public net.minecraft.item.ItemStack received;
  public java.util.List<net.minecraft.item.ItemStack> result = new java.util.ArrayList<>();
  public java.util.List<net.minecraft.item.ItemStack> getSubItems(net.minecraft.item.ItemStack stack) { calls++; received=stack; return result; }
 }
}
''',
}

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--java-home', type=Path, required=True)
    parser.add_argument('--classpath', required=True)
    args = parser.parse_args()
    spec = importlib.util.spec_from_file_location('recipe_key_source', ROOT / 'porting/tests/check-recipe-key-source.py')
    audit = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(audit)
    audit.main()
    exe = '.exe' if os.name == 'nt' else ''
    java_home = args.java_home
    if not (java_home / 'bin' / ('javac' + exe)).is_file() and (java_home.parent / 'bin' / ('javac' + exe)).is_file():
        java_home = java_home.parent
    with tempfile.TemporaryDirectory(prefix='hbm-recipe-key-native-') as temporary:
        out = Path(temporary)
        sources = []
        for relative, text in COLLABORATORS.items():
            path = out / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(text, encoding='utf-8')
            sources.append(path)
        for relative in audit.FILES:
            path = ROOT / relative
            print('PRODUCTION', relative, hashlib.sha256(path.read_bytes()).hexdigest(), flush=True)
            sources.append(path)
        sources.append(ROOT / 'porting/tests/RecipeKeyNativeTest.java')
        subprocess.run([str(java_home / 'bin' / ('javac' + exe)), '-encoding', 'UTF-8', '-source', '8', '-target', '8',
                        '-cp', args.classpath, '-d', temporary] + [str(path) for path in sources], check=True)
        subprocess.run([str(java_home / 'bin' / ('java' + exe)), '-cp', os.pathsep.join([temporary, args.classpath]),
                        'RecipeKeyNativeTest'], check=True, cwd=temporary)
    print('PASS complete production RecipesCommon native compile/runtime; not complete recipe registration, FuelHandler, client display or gameplay.', flush=True)

if __name__ == '__main__':
    main()
