#!/usr/bin/env python3
import pathlib,subprocess,tempfile,sys,os
here=pathlib.Path(__file__).resolve().parent;project=here.parents[2];java=pathlib.Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'));cp=pathlib.Path(sys.argv[1] if len(sys.argv)>1 else '/tmp/nagaport-ae2-native-classpath.txt').read_text().strip()
src=project/'src/main/java';s=(src/'com/hbm/inventory/gui/GUIMachineGasCent.java').read_text();a=s.index('protected void drawGuiContainerForegroundLayer(');a=s.rfind('@Override',0,a);b=s.index('\n\t}',a)+3;method=s[a:b]
with tempfile.TemporaryDirectory(prefix='client-field-native-') as temp:
 out=pathlib.Path(temp);test=out/'ClientFieldNativeTest.java';test.write_text((here/'ClientFieldNativeTest.java.in').read_text().replace('/*PRODUCTION_METHOD*/',method));packet=src/'com/hbm/packet/toclient/ExplosionKnockbackPacket.java'
 subprocess.run([str(java/'bin/javac'),'-cp',cp,'-d',str(out),str(test),str(packet)],check=True)
 subprocess.run([str(java/'bin/java'),'-Djava.awt.headless=true','-cp',str(out)+':'+cp,'ClientFieldNativeTest'],check=True)
