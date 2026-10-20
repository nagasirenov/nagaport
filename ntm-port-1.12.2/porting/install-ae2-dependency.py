#!/usr/bin/env python3
"""Install the user-supplied AE2 rv6-stable-7 JAR into this checkout's ignored local Maven repository.

No network requests. The digest pins the user-supplied artifact, not independent publisher authenticity.
"""
import argparse
import hashlib
import json
from pathlib import Path
import zipfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('jar', type=Path)
args = parser.parse_args()
expected = 'e7f7fbf6caaf6206dbd958fb8f9185f874a405289e62ae3e97209b455a89fb1a'
data = args.jar.read_bytes()
if hashlib.sha256(data).hexdigest() != expected:
    raise SystemExit('Unexpected SHA-256; do not install a different artifact under the pinned version.')
with zipfile.ZipFile(args.jar) as jar:
    if jar.testzip() is not None:
        raise SystemExit('AE2 JAR failed ZIP CRC verification.')
    meta = json.loads(jar.read('mcmod.info'))[0]
    if (meta['modid'], meta['version'], meta['mcversion']) != ('appliedenergistics2', 'rv6-stable-7', '1.12.2'):
        raise SystemExit('Unexpected AE2 version metadata.')
root = Path(__file__).resolve().parents[1]
destination = root / '.local-dependencies/maven/appeng/appliedenergistics2/rv6-stable-7'
destination.mkdir(parents=True, exist_ok=True)
target = destination / 'appliedenergistics2-rv6-stable-7.jar'
pom = destination / 'appliedenergistics2-rv6-stable-7.pom'
pom_data = b'''<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>appeng</groupId><artifactId>appliedenergistics2</artifactId><version>rv6-stable-7</version>
  <packaging>jar</packaging><description>User-supplied pinned official-release AE2 dependency; local packaging metadata only.</description>
</project>
'''
# Validate both destinations before writing either; never replace pre-existing files.
for path, expected_data in ((target, data), (pom, pom_data)):
    if path.exists() and path.read_bytes() != expected_data:
        raise SystemExit('Existing local dependency differs; refusing to overwrite: ' + str(path))
for path, expected_data in ((target, data), (pom, pom_data)):
    if not path.exists():
        with path.open('xb') as stream:
            stream.write(expected_data)
print('Installed AE2 rv6-stable-7 locally; SHA-256 ' + expected)
