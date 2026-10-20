#!/usr/bin/env python3
"""Validate finite transformation and execute extracted production bodies on actual engines."""
import argparse,hashlib,os,pathlib,re,subprocess,tempfile,zipfile,json
import check_source as source
P=pathlib.Path
p=argparse.ArgumentParser();p.add_argument('--source-only',action='store_true');p.add_argument('--java-home',type=P);p.add_argument('--classpath');p.add_argument('--legacy-client',type=P);a=p.parse_args()
source.validate()
for rel,digest in json.loads((source.HERE/'support-hashes.json').read_text()).items():assert hashlib.sha256((source.ROOT/rel).read_bytes()).hexdigest()==digest,('support changed',rel)
if a.source_only:raise SystemExit(0)
assert a.java_home and a.classpath and a.legacy_client
expected=json.loads((source.HERE/'engine-hashes.json').read_text());actual={}
for jar in a.classpath.split(os.pathsep):
 if not zipfile.is_zipfile(jar):continue
 with zipfile.ZipFile(jar) as z:
  for name,digest in expected.items():
   if name in z.namelist():
    assert name not in actual,('duplicate engine class',name);actual[name]=hashlib.sha256(z.read(name)).hexdigest()
assert actual==expected,'Native engine differs from retained proof'
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
fixtures={
 'RenderCrucible':'double level=new double[]{0,.125,.875,-1,3.75,1e20}[seed];',
 'RenderStrandCaster':'double level=new double[]{0,.125,.675,-1,3.75,1e20}[seed];int r=seed*43,g=255-seed*31,b=seed*51;',
 'GUIScreenGuide':'int x=seed*31-70,y=23-seed*47,dimX=seed*128-256,dimY=1024-seed*256;',
 'GUIMachineRadarNT':'double x=-.125+seed*51,y=13.75-seed*17;int sourceX=15-seed*8,sourceY=-17+seed*51,sizeX=3+seed*5,sizeY=19-seed*6;float f=0.00390625F,f1=0.00390625F;',
 'SmallBlockPronter':'float pixel=new float[]{1F/16F,0F,-.125F,Float.MIN_VALUE,1F,3F}[seed];',
 'SkyProviderThatmo':'float var14=new float[]{0,.125F,-1F,1F,47.5F,20000F}[seed];',
}
celestial={
 'renderStation':'float size=.8F;',
 'renderDigamma':'float var12=new float[]{0,.125F,-1F,1F,47.5F,20000F}[seed];double dist=100D-seed*23.5;',
 'renderRings':'float ringSize=new float[]{0,.125F,-1F,1F,47.5F,20000F}[seed];double offset=-20D;',
 'renderCelestials':'double size=new double[]{0,.125,-1,1,47.5,20000}[seed],ringSize=size*1.5,uvOffset=seed*.125,uMin=-.25+seed*.1,vMin=.375,uMax=1.25,vMax=.875,interpe=size*.3,destroyedProgressClientInterpolation=size*3,impactSize=size*.035,flareSize=size*1.5;',
}
methods=[];calls=[]
for rel,r in source.FILES.items():
 native=(source.ROOT/rel).read_text();old=source.reverse_source(rel,native);stem=P(rel).stem
 for method,record in r['scopes'].items():
  name=record['name'];buffer=name+'Buffer'
  aa,bb=source.transform.method_span(old,method);ob=old[aa:bb]
  aa,bb=source.transform.method_span(native,method);nb=native[aa:bb]
  before=re.findall(name+r'\.startDrawingQuads\(\);.*?'+name+r'\.draw\(\);',ob,re.S)
  after=re.findall(buffer+r'\.begin\(7, DefaultVertexFormats.POSITION_TEX\);.*?'+name+r'\.draw\(\);',nb,re.S)
  assert len(before)==len(after)==record['batches']
  prefix_old='\n'.join(record['reset_setters'])
  # Exact production typed argument-evaluation declarations, not independently generated equivalents.
  prefix_native='\n'.join(line for line in nb.splitlines() if re.match(r'\s*(?:float|int) '+name+'Reset',line))
  env=celestial[method] if stem=='SkyProviderCelestial' else fixtures[stem]
  for index,(old_body,native_body) in enumerate(zip(before,after)):
   number=len(methods);label=stem+'.'+method+':'+str(index)
   old_body=(prefix_old+'\n'+old_body).replace(name+'.draw();','captureOld(old, oldRecords);').replace(name+'.','old.')
   native_body=(prefix_native+'\n'+native_body).replace(name+'.draw();','captureNative(owner, nativeRecords);').replace(buffer+'.','buffer.')
   rng=old_body.count('random.nextDouble()');assert rng==native_body.count('random.nextDouble()')
   body='''void batch%d(int seed,double translation)throws Exception {
 List<Record> nativeRecords=new ArrayList<>(),oldRecords=new ArrayList<>();int nc,oc;
 reset(seed);{CountRandom random=new CountRandom(12+seed);%s
 Tessellator owner=Tessellator.getInstance();BufferBuilder buffer=owner.getBuffer();buffer.setTranslation(translation,-translation,translation*.25);
 %s
 nc=random.calls;}
 reset(seed);{CountRandom random=new CountRandom(12+seed);%s
 Legacy old=new Legacy(translation);
 %s
 oc=random.calls;}
 compare(nativeRecords,oldRecords,nc,oc,%d,"%s");
}'''%(number,env,native_body,env,old_body,rng,label)
   methods.append(body);calls.append('test.batch%d(seed,translation);'%number)
assert len(methods)==29
java=(source.HERE/'NativeTexturedTest.java.in').read_text().replace('/*METHODS*/','\n'.join(methods)).replace('/*CALLS*/','\n'.join(calls))
with tempfile.TemporaryDirectory(prefix='remaining-textured-native-') as td:
 tmp=P(td);file=tmp/'NativeTexturedTest.java';file.write_text(java)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(tmp/name).write_bytes(z.read(name))
 support=[source.ROOT/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java',source.ROOT/'src/main/java/com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(tmp),str(file)]+list(map(str,support)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,'NativeTexturedTest',str(a.legacy_client)],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
