#!/usr/bin/env python3
import gzip, subprocess, tempfile
from pathlib import Path
HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
REPO = ROOT.parent
BASE = 'fc2cd2dba34caba0dba74ed288f95107ab4dc979'
paths = ['src/main/java/com/hbm/blocks/ModBlocks.java', 'src/main/java/com/hbm/blocks/generic/BlockCrate.java', 'src/main/java/com/hbm/blocks/generic/BlockStorageCrate.java']
old = {p: subprocess.check_output(['git', '-C', str(REPO), 'show', BASE+':ntm-port-1.12.2/'+p]) for p in paths}
new = {p: (ROOT/p).read_bytes() for p in paths}
patch_data = gzip.decompress((HERE/'production.patch.gz').read_bytes())
with tempfile.TemporaryDirectory(prefix='crate-sound-replay-') as d:
    t = Path(d)
    patch = t/'production.patch'
    patch.write_bytes(patch_data)
    for p, data in old.items():
        target = t/'ntm-port-1.12.2'/p
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
    for flags in [['--check'], []]:
        subprocess.run(['git', '-C', d, '-c', 'core.whitespace=cr-at-eol', 'apply'] + flags + [str(patch)], check=True)
    assert all((t/'ntm-port-1.12.2'/p).read_bytes() == new[p] for p in paths), 'Exact forward replay'
    result = subprocess.run(['git', '-C', d, 'apply', '--check', str(patch)], stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    assert result.returncode != 0, 'Duplicate application must be rejected'
    for flags in [['--reverse', '--check'], ['--reverse']]:
        subprocess.run(['git', '-C', d, '-c', 'core.whitespace=cr-at-eol', 'apply'] + flags + [str(patch)], check=True)
    assert all((t/'ntm-port-1.12.2'/p).read_bytes() == old[p] for p in paths), 'Exact whole-file reverse replay'
print('PASS production patch dry-run/apply/exact forward, duplicate-apply rejection, reverse dry-run/exact whole-file reverse against CP86.')
