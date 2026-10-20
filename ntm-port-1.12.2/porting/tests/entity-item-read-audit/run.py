#!/usr/bin/env python3
"""Standalone genuine native EntityItem boundary audit, no downloads or Gradle."""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile
p = argparse.ArgumentParser()
p.add_argument('--java-home', required=True)
p.add_argument('--classpath-file', required=True)
a = p.parse_args()
here = Path(__file__).resolve().parent
suffix = '.exe' if os.name == 'nt' else ''
cp = Path(a.classpath_file).read_text().strip()
with tempfile.TemporaryDirectory(prefix='entity-item-read-audit-') as tmp:
    subprocess.run([str(Path(a.java_home)/'bin'/('javac'+suffix)), '-encoding', 'UTF-8', '-cp', cp, '-d', tmp, str(here/'NativeEntityItemReadAudit.java')], check=True)
    subprocess.run([str(Path(a.java_home)/'bin'/('java'+suffix)), '-cp', tmp+os.pathsep+cp, 'NativeEntityItemReadAudit'], check=True, cwd=tmp)
