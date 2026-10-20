#!/usr/bin/env python3
"""CP86 AST inventory + sealed diagnostic types; no new full build or source changes."""
import argparse, collections, csv, hashlib, io, json, re, subprocess, tempfile, zipfile
from pathlib import Path
HERE = Path(__file__).resolve().parent
PROJECT = HERE.parents[2]
REPO = PROJECT.parent
BASE = 'fc2cd2dba34caba0dba74ed288f95107ab4dc979'
p = argparse.ArgumentParser()
p.add_argument('--java-home', type=Path, required=True)
p.add_argument('--classpath-file', type=Path, required=True)
p.add_argument('--legacy-jar', type=Path, required=True)
p.add_argument('--forge-userdev-jar', type=Path, required=True)
a = p.parse_args()

def git(*args):
    return subprocess.check_output(['git', '-C', str(REPO), *args])

def digest(data):
    return hashlib.sha256(data).hexdigest()

# Byte-exact no-op inverse for every production input, not merely the 113 selected lines.
paths = git('ls-tree', '-r', '--name-only', BASE, 'ntm-port-1.12.2/src/main/java').decode().splitlines()
for path in paths:
    assert (REPO / path).read_bytes() == git('show', BASE + ':' + path), path
print('PASS all %d tracked production files byte-identical to CP86; production patch is empty.' % len(paths), flush=True)
with tempfile.TemporaryDirectory(prefix='cp87-sound-audit-') as tmp:
    javac = str(a.java_home / 'bin/javac')
    java = str(a.java_home / 'bin/java')
    subprocess.run([javac, '-cp', str(a.java_home / 'lib/tools.jar'), '-d', tmp, str(HERE / 'SoundSiteInventory.java')], check=True)
    result = subprocess.run([java, '-cp', tmp + ':' + str(a.java_home / 'lib/tools.jar'), 'SoundSiteInventory', str(PROJECT)], check=True, text=True, capture_output=True)
    print(result.stderr.strip())
    (HERE / 'inventory.tsv').write_text(result.stdout)
    sites = list(csv.DictReader(io.StringIO(result.stdout), delimiter='\t'))
    assert len(sites) == 113 and {s['owner'] for s in sites} == {'ModBlocks'}
    assert all(s['method'] != '<init>' and s['receiver'] not in ('this', 'super', '<implicit-this>') for s in sites)
    print('PASS 113 external registry calls; zero constructor-owned this/implicit-this candidates.')

log_path = PROJECT / 'porting/checkpoint86/full-compile.log'
log = log_path.read_text()
pattern = r'(?m)^(/[^\n]+\.java):(\d+): error: [^\n]+\n((?:(?!^/[^\n]+\.java:\d+: error:)[\s\S])*?)(?=^/[^\n]+\.java:\d+: error:|\Z)'
typed = {}
for m in re.finditer(pattern, log):
    b = m.group(3)
    arg = re.search(r'symbol:   method setStepSound\(([^)]+)\)', b)
    if arg:
        key = ('src/main/java/' + m.group(1).split('/build/sources/main/java/')[1], m.group(2))
        assert key not in typed
        typed[key] = {'argument_type': arg[1], 'receiver_location': re.search(r'location: ([^\n]+)', b)[1]}
assert set(typed) == {(s['path'], s['line']) for s in sites}
for site in sites:
    site.update(typed[(site['path'], site['line'])])
args = dict(collections.Counter(s['argument_type'] for s in sites))
receivers = dict(collections.Counter(s['receiver_location'] for s in sites))
assert args == {'SoundType': 87, 'ModSoundType': 26}
assert receivers['class Block'] == 107
print('PASS exact 113-site match to sealed CP86 typed diagnostics: %s; %s' % (args, receivers))
cp = a.classpath_file.read_text().strip()
native = subprocess.check_output([str(a.java_home / 'bin/javap'), '-classpath', cp, '-c', '-p', 'net.minecraft.block.Block'], text=True)
legacy = subprocess.check_output([str(a.java_home / 'bin/javap'), '-classpath', str(a.legacy_jar), '-c', '-p', 'aji'], text=True)
nb = re.search(r'  protected net\.minecraft\.block\.Block setSoundType\(net\.minecraft\.block\.SoundType\);\n    Code:\n(?:[^\n]+\n){5}', native)[0]
lb = re.search(r'  protected aji a\(ajm\);\n    Code:\n(?:[^\n]+\n){5}', legacy)[0]
for b in [nb, lb]:
    assert re.search(r'0: aload_0\s+1: aload_1\s+2: putfield[^\n]+\s+5: aload_0\s+6: areturn', b)
(HERE / 'setter-bytecode.txt').write_text('Native 1.12.2:\n' + nb + '\nUntransformed vanilla 1.7.10 (Forge widens this setter; see forge-original-access.json):\n' + lb)
# The original Forge distribution widens the protected vanilla setter. Do not
# misrepresent untransformed vanilla visibility as the original mod environment.
archive_sha256 = digest(a.forge_userdev_jar.read_bytes())
assert archive_sha256 == '6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91'
member = 'src/main/resources/forge_at.cfg'
with zipfile.ZipFile(a.forge_userdev_jar) as archive:
    at_bytes = archive.read(member)
at_line = 'public net.minecraft.block.Block func_149672_a(Lnet.minecraft.block.Block$SoundType;)Lnet.minecraft.block.Block; #setStepSound'
lines = at_bytes.decode().splitlines()
assert lines.count(at_line) == 1
access_evidence = {'artifact': 'forge-1.7.10-10.13.4.1614-1.7.10-userdev.jar', 'archive_sha256': archive_sha256, 'member': member, 'member_sha256': digest(at_bytes), 'line_number': lines.index(at_line) + 1, 'exact_line': at_line, 'scope': 'Original Forge access configuration, not a newly launched transformed runtime; no cache regeneration.'}
(HERE / 'forge-original-access.json').write_text(json.dumps(access_evidence, indent=2) + '\n')
print('PASS original Forge 1614 explicitly exposes setStepSound publicly; protected original bytecode is untransformed vanilla only.')
report = {'base': BASE, 'production_files_unchanged': len(paths), 'production_edits': 0, 'original_forge_access': access_evidence, 'constructor_owned_candidates': 0, 'external_registry_calls': len(sites), 'argument_types': args, 'receiver_locations': receivers, 'log_sha256': digest(log_path.read_bytes()), 'legacy_jar_sha256': digest(a.legacy_jar.read_bytes()), 'type_evidence': 'Existing sealed full-compile.log, matched one-for-one by file and line to current AST inventory. No new attribution or full compile.', 'sites': sites}
(HERE / 'audit.json').write_text(json.dumps(report, indent=2) + '\n')
print('PASS native and untransformed vanilla setters assign the exact sound reference and return this; target native setter remains protected; no new access workaround introduced.')
