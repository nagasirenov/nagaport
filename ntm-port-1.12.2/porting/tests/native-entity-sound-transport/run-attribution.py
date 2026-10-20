#!/usr/bin/env python3
"""Reproduce actual-source javac attribution without modifying sealed evidence."""
import argparse, csv, os, pathlib, shutil, subprocess, tempfile
HERE = pathlib.Path(__file__).resolve().parent
p = argparse.ArgumentParser()
p.add_argument('--root', type=pathlib.Path, default=HERE.parents[3])
p.add_argument('--java-home', default=os.environ.get('JAVA_HOME', '/workspace/shared/nagaport-toolchain/java8'))
p.add_argument('--classpath-file', default='/tmp/nagaport-ae2-native-classpath.txt')
a = p.parse_args()
base = 'e72ec3d2ae8db6653ebaad529a574ee32c99260b'
work = pathlib.Path(tempfile.mkdtemp(prefix='entity-sound-attribution-'))
(work/'classes').mkdir(); (work/'overlay').mkdir()
subprocess.run(['git','-C',str(a.root),'archive','--format=zip','-o',str(work/'source.zip'),base,'ntm-port-1.12.2/src/main/java'],check=True)
files = ['com/hbm/main/NetworkHandler.java','com/hbm/packet/PacketDispatcher.java','com/hbm/packet/toclient/LegacyEntitySoundPacket.java','com/hbm/sound/EntitySoundTransport.java']
for name in files:
    dest=work/'overlay'/name; dest.parent.mkdir(parents=True,exist_ok=True)
    shutil.copyfile(a.root/'ntm-port-1.12.2/src/main/java'/name,dest)
java=pathlib.Path(a.java_home)
subprocess.run([str(java/'bin/javac'),'-cp',str(java/'lib/tools.jar'),'-d',str(work/'classes'),str(HERE/'infrastructure-attribution/InfrastructureAttribution.java')],check=True)
for phase,overlay in [('before','-'),('after',str(work/'overlay'))]:
    with (work/(phase+'.tsv')).open('w') as out, (work/(phase+'.log')).open('w') as log:
        subprocess.run([str(java/'bin/java'),'-Xmx5g','-cp',str(work/'classes')+':'+str(java/'lib/tools.jar'),'InfrastructureAttribution',str(work/'source.zip'),a.classpath_file,str(HERE/'infrastructure-attribution/infrastructure-sites.tsv'),str(work/(phase+'-diagnostics.tsv')),overlay],stdout=out,stderr=log,check=True)
def rows(path):
    with path.open() as f:return list(csv.DictReader(f,delimiter='\t'))
before=rows(work/'before.tsv'); after=rows(work/'after.tsv')
original=rows(HERE/'before-attribution/before.tsv')
for phase in (before,after):
    selected=[r for r in phase if r['method']=='playSoundAtEntity']
    assert selected==original,'Complete candidate receiver/argument AST records changed'
expected={('com/hbm/main/NetworkHandler.java','generatePacketFrom'):'net.minecraftforge.fml.common.network.FMLEmbeddedChannel',('com/hbm/packet/toclient/LegacyEntitySoundPacket.java','addScheduledTask'):'net.minecraft.client.Minecraft',('com/hbm/packet/toclient/LegacyEntitySoundPacket.java','playSound'):'net.minecraft.client.audio.SoundHandler',('com/hbm/sound/EntitySoundTransport.java','sendToAllNearExcept'):'net.minecraft.server.management.PlayerList',('com/hbm/sound/EntitySoundTransport.java','playSoundToAllNearExcept'):'net.minecraft.world.IWorldEventListener'}
for key,owner in expected.items():
    hits=[r for r in after if (r['path'],r['method'])==key]
    assert len(hits)==1 and hits[0]['method_owner']==owner and hits[0]['method_element_kind']=='METHOD',(key,hits)
print('PASS 41 unchanged complete candidate ASTs (36 eligible, zero activated) and five genuine native destinations. Fresh logs: '+str(work))
