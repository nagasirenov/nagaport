#!/usr/bin/env python3
import json, subprocess, sys
from pathlib import Path
HERE = Path(__file__).resolve().parent
records = []
for owner in ['BlockCrate', 'BlockStorageCrate']:
    for mutant, expected in [('drop-assignment', 'Caused by: java.lang.AssertionError: exact original sound argument identity'), ('wrong-argument', 'Caused by: java.lang.AssertionError: exact original sound argument identity'), ('null-return', 'Caused by: java.lang.NullPointerException')]:
        result = subprocess.run([sys.executable, str(HERE/'run-native.py')] + sys.argv[1:] + ['--mutant', mutant, '--mutant-owner', owner], stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        assert 'COMPILE_PASS:' in result.stdout and 'error:' not in result.stdout, (owner, mutant, 'Not a runtime kill', result.stdout)
        assert result.returncode != 0 and expected in result.stdout, (owner, mutant, result.stdout)
        records.append(dict(owner=owner, mutant=mutant, compiled=True, exit=result.returncode, killed_by=expected))
        print('PASS runtime negative control:', owner, mutant, 'compiled then rejected by', expected, flush=True)
(HERE/'mutation-results.json').write_text(json.dumps(records, indent=2)+'\n')
