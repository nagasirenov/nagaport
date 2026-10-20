#!/usr/bin/env python3
"""Check the offline installer using the supplied pinned JAR in a disposable checkout."""
import argparse
import hashlib
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--jar', type=Path, required=True)
args = parser.parse_args()
source = args.jar.resolve()
before = hashlib.sha256(source.read_bytes()).hexdigest()
script = Path(__file__).resolve().parents[1] / 'install-ae2-dependency.py'
with tempfile.TemporaryDirectory(prefix='ae2-installer-proof-') as directory:
    root = Path(directory)
    (root / 'porting').mkdir()
    copied = root / 'porting/install-ae2-dependency.py'
    shutil.copyfile(script, copied)
    def run(path):
        return subprocess.run([sys.executable, str(copied), str(path)], capture_output=True, text=True)
    result = run(source)
    assert result.returncode == 0, result.stderr
    assert run(source).returncode == 0, 'Repeated install must be idempotent.'
    target = root / '.local-dependencies/maven/appeng/appliedenergistics2/rv6-stable-7/appliedenergistics2-rv6-stable-7.jar'
    pom = target.with_suffix('.pom')
    target.write_bytes(b'conflicting jar')
    assert run(source).returncode != 0 and target.read_bytes() == b'conflicting jar'
    target.write_bytes(source.read_bytes())
    pom.write_bytes(b'conflicting pom')
    assert run(source).returncode != 0 and pom.read_bytes() == b'conflicting pom'
    wrong = root / 'wrong.jar'
    wrong.write_bytes(b'wrong artifact')
    assert run(wrong).returncode != 0
    assert hashlib.sha256(source.read_bytes()).hexdigest() == before
print('PASS installer: pinned artifact, idempotence, conflicting JAR refusal, conflicting POM refusal, wrong input refusal, source untouched.')
