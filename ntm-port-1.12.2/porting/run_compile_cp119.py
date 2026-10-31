import subprocess
from pathlib import Path

cmd = [
    'cmd.exe', '/c',
    'gradlew.bat --offline clean -I porting/compile-diagnostics-expanded.init.gradle compileJava --console=plain'
]

print('Running compile...')
proc = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, errors='replace')
print('Finished with exit code:', proc.returncode)

out_dir = Path('porting/checkpoint119')
(out_dir / 'full-compile.log').write_text(proc.stdout, encoding='utf-8')
(out_dir / 'full-compile.exit').write_text(str(proc.returncode) + '\n', encoding='utf-8')
print('Saved full-compile.log and full-compile.exit')
