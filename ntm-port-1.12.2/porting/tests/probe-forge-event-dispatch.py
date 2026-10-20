#!/usr/bin/env python3
"""Run genuine Forge event transformation and actual event-bus dispatch, with no platform fixtures."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess
import tempfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--java-home', type=Path, required=True)
classpath = parser.add_mutually_exclusive_group(required=True)
classpath.add_argument('--classpath')
classpath.add_argument('--classpath-file', type=Path)
parser.add_argument('--report', type=Path, help='Write exact classpath, commands, native JAR hash and result as JSON.')
args = parser.parse_args()
if args.classpath_file:
    args.classpath = args.classpath_file.read_text(encoding='utf-8').strip()
root = Path(__file__).resolve().parents[2]
# Make paths independent of the temporary working directory used for native logs.
entries = [str(Path(path).resolve()) for path in args.classpath.split(os.pathsep) if path]
cp = os.pathsep.join(entries)
forge = [Path(path) for path in entries if Path(path).name.startswith('forgeBin-1.12.2-') and Path(path).suffix == '.jar']
if len(forge) != 1 or not forge[0].is_file():
    parser.error('Use exactly one cached native ForgeGradle forgeBin-1.12.2 JAR.')
java_home = args.java_home.resolve()
suffix = '.exe' if os.name == 'nt' else ''
java = str(java_home / 'bin' / ('java' + suffix))
javac = str(java_home / 'bin' / ('javac' + suffix))
report = {
    'native_forge_jar': str(forge[0]),
    'native_forge_sha256': hashlib.sha256(forge[0].read_bytes()).hexdigest(),
    'classpath': entries,
    'java_version': subprocess.run([java, '-version'], text=True, stdout=subprocess.PIPE,
                                   stderr=subprocess.STDOUT, check=True).stdout.strip(),
    'transformer': 'net.minecraftforge.fml.common.asm.transformers.EventSubscriptionTransformer',
    'result': 'not run',
}
try:
    with tempfile.TemporaryDirectory(prefix='forge-event-dispatch-') as temp:
        classes = Path(temp) / 'classes'
        classes.mkdir()
        report['javac_command'] = [javac, '-source', '8', '-target', '8', '-proc:none', '-implicit:none',
                                  '-encoding', 'UTF-8', '-cp', cp, '-d', str(classes)] + [
            str(root / 'porting/tests' / name) for name in ('ForgeEventNativeLauncher.java', 'ForgeEventNativeTest.java')]
        report['java_command'] = [java, '-cp', str(classes) + os.pathsep + cp,
                                  'ForgeEventNativeLauncher', 'ForgeEventNativeTest']
        subprocess.run(report['javac_command'], check=True)
        execution = subprocess.run(report['java_command'], cwd=temp, text=True,
                                   stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        report['runtime_output'] = execution.stdout
        print(execution.stdout, end='')
        execution.check_returncode()
        if 'PASS: 10 native Forge event assertions' not in execution.stdout:
            raise AssertionError('Native dispatch success marker missing')
        report['result'] = 'passed'
except BaseException:
    report['result'] = 'failed'
    raise
finally:
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
