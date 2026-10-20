#!/usr/bin/env python3
"""Actual native processKeyBinds + real Forge event dispatch and full production listener.
Controlled Minecraft/player allocation and input-state buffer; no platform substitutes.
The unchanged native Keyboard buffer is set all-up and its created flag is set so
Forge modifier lookups can run without a window. No physical input acceptance is
claimed. Native onTick, binding hash/rebinding, and isPressed consumption execute.
All GuiOpenEvents are canceled before GUI initialization/GL; creative coverage is
its actual key attempt before native redirection, not a rendered creative screen.
Multiple queued attempts are exercised under cancellation, which leaves the queue
available to the native loop. Controller horse/reset and Player hand-active/horse
send methods are explicitly controlled native subclasses; no packets/game startup.
Source audit found only NTM NORMAL-priority main-menu splash and anvil bookkeeping
GuiOpen subscribers, neither replacing inventory events. The external earlier
same-priority replacement limitation is deliberately reproduced by the fixture.
"""
import argparse,hashlib,os,pathlib,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--java-home',required=True,type=pathlib.Path);p.add_argument('--classpath',required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2]
production=root/'src/main/java/com/hbm/advancement/client/LegacyInventoryActionHandler.java'
files=[production,root/'porting/tests/ForgeEventNativeLauncher.java',root/'porting/tests/InventoryActionNativeTest.java']
with tempfile.TemporaryDirectory(prefix='native-inventory-action-') as tmp:
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for n in z.namelist():
     if n.endswith('.so') and '/' not in n:(pathlib.Path(tmp)/n).write_bytes(z.read(n))
 print('FULL PRODUCTION',production.relative_to(root),hashlib.sha256(production.read_bytes()).hexdigest(),flush=True)
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+tmp,'-cp',tmp+os.pathsep+a.classpath,'ForgeEventNativeLauncher','InventoryActionNativeTest'],cwd=tmp,check=True)
