#!/usr/bin/env python3
import os
import pathlib
import subprocess
import sys
import tempfile

def main():
    root = pathlib.Path(__file__).resolve().parents[2]
    java_home = pathlib.Path(r"C:\Program Files\Eclipse Adoptium\jdk-8.0.504.1-hotspot")
    javac = str(java_home / "bin" / ("javac.exe" if os.name == "nt" else "javac"))
    java = str(java_home / "bin" / ("java.exe" if os.name == "nt" else "java"))

    # Resolve compile classpath from task log or gradlew
    log_path = pathlib.Path(r"C:\Users\nagas\.gemini\antigravity\brain\eddf35bb-f4a8-490a-a99a-30d7bb1c6022\.system_generated\tasks\task-103.log")
    cp = None
    if log_path.exists():
        for line in log_path.read_text(encoding="utf-8", errors="replace").splitlines():
            if line.startswith("COMPILE_CLASSPATH="):
                cp = line.split("=", 1)[1].strip()
                break

    if not cp:
        print("Obtaining classpath via gradlew...")
        gradlew = str(root / ("gradlew.bat" if os.name == "nt" else "gradlew"))
        proc = subprocess.run([gradlew, "--no-daemon", "--offline", "--console=plain",
                               "-I", str(root / "porting/print-compile-classpath.init.gradle"),
                               "printPortCompileClasspath"],
                              cwd=str(root), stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, check=True)
        for line in proc.stdout.splitlines():
            if line.startswith("COMPILE_CLASSPATH="):
                cp = line.split("=", 1)[1].strip()
                break

    assert cp, "Could not resolve COMPILE_CLASSPATH"

    prod_classes = str(root / "build" / "classes" / "java" / "main")
    full_cp = prod_classes + os.pathsep + cp

    # Run probe-container-clicks
    print("--- 1. Running probe-container-clicks.py ---")
    subprocess.run([sys.executable, str(root / "porting/tests/probe-container-clicks.py"),
                    "--java-home", str(java_home), "--classpath", cp],
                   cwd=str(root), check=True)

    # Run InventoryBoundaryMinecraftTest
    print("\n--- 2. Running InventoryBoundaryMinecraftTest ---")
    with tempfile.TemporaryDirectory(prefix="hbm-inv-boundary-") as tmp:
        subprocess.run([javac, "-encoding", "UTF-8", "-cp", full_cp, "-d", tmp,
                        str(root / "src/main/java/com/hbm/tileentity/TileEntityInventoryBase.java"),
                        str(root / "porting/tests/InventoryBoundaryMinecraftTest.java")],
                       check=True)
        subprocess.run([java, "-cp", tmp + os.pathsep + full_cp, "InventoryBoundaryMinecraftTest"],
                       cwd=tmp, check=True)

    # Run Session2VerificationMinecraftTest
    print("\n--- 3. Running Session2VerificationMinecraftTest ---")
    with tempfile.TemporaryDirectory(prefix="hbm-session2-verify-") as tmp:
        subprocess.run([javac, "-encoding", "UTF-8", "-cp", full_cp, "-d", tmp,
                        str(root / "porting/tests/Session2VerificationMinecraftTest.java")],
                       check=True)
        subprocess.run([java, "-cp", tmp + os.pathsep + full_cp, "Session2VerificationMinecraftTest"],
                       cwd=tmp, check=True)

    print("\nAll Session 2 headless test suites PASSED!")

if __name__ == "__main__":
    main()
