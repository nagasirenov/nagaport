import argparse,os,subprocess,tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--source-root',type=Path);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args();t=Path(__file__).resolve().parent;s=a.source_root or a.module
with tempfile.TemporaryDirectory(prefix='native-tracker-') as tmp:
 files=[s/'src/main/java/com/hbm/util/TrackerUtil.java',a.module/'src/main/java/com/hbm/interfaces/NotableComments.java',t/'TrackerNativeTest.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-cp',a.classpath,'-d',tmp]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',tmp+os.pathsep+a.classpath,'TrackerNativeTest'],cwd=tmp,check=True)
