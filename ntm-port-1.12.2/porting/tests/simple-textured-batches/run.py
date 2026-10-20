#!/usr/bin/env python3
"""Run pinned source guards and actual old/native buffer byte comparison.
Extracted batches only: surrounding class linkage and GL are NOT simulated.
"""
import argparse,hashlib,os,pathlib,re,subprocess,tempfile
import check_source as source
P=pathlib.Path
parser=argparse.ArgumentParser()
parser.add_argument('--java-home',type=P)
parser.add_argument('--classpath')
parser.add_argument('--legacy-client',type=P)
parser.add_argument('--source-only',action='store_true')
a=parser.parse_args()
source.validate()
if a.source_only:raise SystemExit(0)
assert a.java_home and a.classpath and a.legacy_client,'Supply --java-home --classpath --legacy-client'
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce','Wrong official 1.7.10 client artifact'
# Values are controlled batch-boundary fixtures, not fake world/renderer behavior.
fixtures={
 'GUIMachineGasCent':'double minX=-31.125+seed,maxX=18.75+seed,minY=-7.5,maxY=63.25,z=-90.125,minU=-.25,maxU=2.5,minV=1,maxV=.375;',
 'GUIMachineStardar':'int guiLeft=13+seed,guiTop=-9;float starX=-27.125F+seed,starY=43.75F;',
 'GUIMachineTurbineGas':'int xPos=-13+seed,yPos=37,width=2,height=64,maxTemp=800,temp=seed*313-200,squareSideLenght=48;double uMin=176D/256,uMax=178D/256,vMin=(64D-64*temp/maxTemp)/256,vMax=64D/256;',
 'GUIScreenPreview':'double x=-.125+seed,y=13.75,f=.00390625D,f1=.00390625D;int sourceX=15,sourceY=-17,sizeX=3+seed*5,sizeY=19;',
 'DiamondPronter':'float p=1F/256F,s=1F/139F,width=10F*s,height=14F*s,oY=(seed-1)*33*s,oZ=(2-seed)*33*s,symSize=59F/2F*s;int x=5+seed*24,y=5;',
 'RenderNTMSkyboxChainloader':'float var12=.5F+seed*.25F;double dist=100D-seed*2.5;',
 'Satellite':'float size=.5F;',
 'FogRenderer':'', 'RenderMeteor':''}
methods=[];calls=[]
for relative,record in source.FILES.items():
 current=(source.ROOT/relative).read_text();old=source.reverse_source(relative,current)
 stem=P(relative).stem
 env='Resolution resolution=new Resolution(seed);' if stem.startswith('Armor') else fixtures[stem]
 for name in record['names']:
  before=re.findall(name+r'\.startDrawingQuads\(\);.*?'+name+r'\.draw\(\);',old,re.S)
  after=re.findall(name+r'Buffer.begin\(7, DefaultVertexFormats.POSITION_TEX\);.*?'+name+r'\.draw\(\);',current,re.S)
  for index,(legacy,native) in enumerate(zip(before,after)):
   number=len(methods)
   # Source text is copied unchanged except for local endpoint names and terminal
   # draw removal; compare() captures both buffers before any driver submission.
   legacy=legacy.replace(name+'.draw();','').replace(name+'.','old.')
   native=native.replace(name+'.draw();','').replace(name+'Buffer.','buffer.')
   body='void batch%d(int seed,double offset)throws Exception{\n%s\nBufferBuilder buffer=new BufferBuilder(128);buffer.setTranslation(offset,-offset,offset*.25);Legacy old=new Legacy(offset);\n{\n%s\n}\n{\n%s\n}\ncompare(buffer,old,"%s:%d");\n}'%(number,env,native,legacy,relative,index)
   methods.append(body);calls.append('test.batch%d(seed,offset);'%number)
assert len(methods)==22
java=(source.HERE/'NativeBatchTest.java.in').read_text().replace('/*GENERATED_METHODS*/','\n'.join(methods)).replace('/*GENERATED_CALLS*/','\n'.join(calls))
with tempfile.TemporaryDirectory(prefix='simple-textured-native-') as td:
 tmp=P(td);file=tmp/'NativeBatchTest.java';file.write_text(java)
 exe='.exe' if os.name=='nt' else ''
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(tmp),str(file)],check=True)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-cp',str(tmp)+os.pathsep+a.classpath,'NativeBatchTest',str(a.legacy_client)],check=True)
