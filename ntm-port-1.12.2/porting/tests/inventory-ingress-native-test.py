#!/usr/bin/env python3
"""Test 35 verbatim production setters and 34 native removals against real native ItemStack.
Controlled callback/storage fixtures, not full machine or entity compilation.
Requires an existing native classpath and JDK8; never invokes Gradle or Git.
"""
import argparse
import importlib.util
import os
from pathlib import Path
import subprocess
import tempfile
ROOT = Path(__file__).resolve().parents[2]

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--java-home', required=True, type=Path)
    parser.add_argument('--classpath', required=True)
    args = parser.parse_args()
    spec = importlib.util.spec_from_file_location('ingress_source_check', ROOT / 'porting/tests/check-inventory-ingress-source.py')
    check = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(check)
    check.main()
    classes, tests = [], []
    for index, entry in enumerate(check.MANIFEST['files']):
        # The source guard verifies the exact frozen method is still in production.
        text = (ROOT / entry['path']).read_bytes().decode('utf-8')
        body = '\n'.join(edit['new'] for edit in entry['edits'] if edit['kind'] in ('setter', 'removal'))
        assert all(edit['new'] in text for edit in entry['edits'])
        classes.append('static class F%d extends Fixture { %s }' % (index, body))
        dirty = int('markDirty();' in entry['old'])
        updates = int('markBlockForUpdate(' in entry['old'])
        tests.append('test(new F%d(),%d,%d,"%s");' % (index, dirty, updates, entry['path']))
        if any(edit['kind'] == 'removal' for edit in entry['edits']):
            removal_dirty = dirty if 'setInventorySlotContents' in entry['edits'][1]['new'] else 0
            tests.append('testRemoval(new F%d(),%d,"%s");' % (index, removal_dirty, entry['path']))
    template = (ROOT / 'porting/tests/inventory-ingress-native-test.java.in').read_text(encoding='utf-8')
    template = template.replace('@CLASSES@', '\n'.join(classes)).replace('@TESTS@', '\n'.join(tests))
    exe = '.exe' if os.name == 'nt' else ''
    with tempfile.TemporaryDirectory(prefix='hbm-inventory-ingress-') as temp:
        source = Path(temp) / 'IngressNativeTest.java'
        source.write_bytes(template.encode('utf-8'))
        subprocess.run([str(args.java_home / 'bin' / ('javac' + exe)), '-source', '8', '-target', '8', '-encoding', 'UTF-8', '-cp', args.classpath, '-d', temp, str(source)], check=True)
        subprocess.run([str(args.java_home / 'bin' / ('java' + exe)), '-cp', os.pathsep.join([temp, args.classpath]), 'IngressNativeTest'], cwd=temp, check=True)
    print('Scope: native ItemStack and verbatim setters/removals; controlled arrays/callback counters. No full classes, gameplay, or Windows runtime acceptance.')

if __name__ == '__main__':
    main()
