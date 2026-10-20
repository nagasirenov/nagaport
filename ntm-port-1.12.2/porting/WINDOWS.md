# Windows build and checkpoint notes

Windows is a supported target for the intended 1.12.2 port; Linux is only the development workspace used for the recorded checks. No Windows execution, game startup, complete mod build or runnable JAR is claimed yet.

## Apply the checkpoint

Use the archive's `apply-checkpoint.py` and `README_RU.txt` from PowerShell, with Python 3 (`py -3`) and Git already installed. Choose the incremental patch only after its stated prior checkpoint. Back up your own changes. Run the helper's `--check` first; apply only after that succeeds. Do not use `git reset --hard`, remove unrelated files, or force a failed patch.

The helper requires exact baseline bytes for affected files. Automatic LF/CRLF worktree conversion (for example Git core.autocrlf) may cause a safe refusal even without logical edits. Do not force a reset or silently normalize the user tree: reconcile the actual snapshot and line endings before applying.

Case-only asset renames are intentional. Windows folder lookup may find an incorrectly cased file, but ZIP/JAR entries remain case-sensitive. Actual native Minecraft `FileResourcePack` tests reject all 43 historical mixed-case entries and accept byte-identical canonical entries. Keep filename casing when archiving/building. Audit scripts enumerate stored directory-entry names instead of relying on Windows case-insensitive `Path.exists()`.

The helper is tested with reconstructed sources in the Linux workspace, not on Windows. Its temporary-name approach avoids relying on a direct case-only filesystem rename. A conflict is a stop-and-inspect result, not permission to overwrite local edits.

## Build with Java 8

From the `ntm-port-1.12.2` project directory, set `JAVA_HOME` to your installed Java 8 JDK. Use the project wrapper, not a Linux toolchain path:

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk8'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat -I porting/compile-diagnostics.init.gradle compileJava --console=plain
```

The recorded cloud build used cached dependencies with `--offline`; omit that option for an initial dependency setup. A dependency/download problem is different from the currently known source migration failures. The current full compile fails with 1489 diagnostics; this command is not a promise of a working game build.

Checkpoint 9's focused tasks can be invoked as follows. These probes were executed on Linux; their Windows path handling is prepared but has not been executed on Windows.

```powershell
$python = py -3 -c "import sys; print(sys.executable)"
.\gradlew.bat "-PportPython=$python" -I porting/native-world-field-probe.init.gradle -I porting/tile-ticks-probe.init.gradle testPortNativeWorldFields testPortTileTicks --console=plain
$line = .\gradlew.bat -I porting/print-compile-classpath.init.gradle printPortCompileClasspath --console=plain | Select-String '^COMPILE_CLASSPATH=' | Select-Object -Last 1
$cp = $line.Line.Substring(18)
py -3 porting/tests/resource-case-followup-probe.py --classpath $cp --java-home $env:JAVA_HOME
py -3 porting/tests/check-resource-case.py
```

Saved cloud classpaths contain Linux absolute paths and must not be copied into a Windows invocation. The command above asks the real project for its local target classpath. Older checkpoint probe scripts have their own platform limitations; use their documented scope rather than assuming every historical standalone shell command is Windows-compatible.
