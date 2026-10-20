#!/usr/bin/env python3
"""Pinned 1.7.10 MCP/SRG and exact old/native getter bytecode comparison."""
import hashlib,io,re,subprocess,zipfile
from pathlib import Path

def digest(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
def method(dump,signature):
 start=dump.index('  '+signature+';');m=re.search(r'\n  (?:public|private|protected|static) ',dump[start+1:]);return dump[start:start+1+m.start()] if m else dump[start:]
def instructions(body):
 out=[]
 for line in body.splitlines():
  match=re.match(r'\s+\d+: (\w+)(?:\s+(.*))?$',line)
  if match:
   op,args=match.groups();out.append((op,(args or '').split('//')[-1].strip() if '//' in (args or '') else (args or '').strip()))
 return out

def audit(java_home,cp,client,forge,srg,native_methods,native_srg,output):
 assert digest(client)=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
 assert digest(forge)=='6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91'
 assert digest(srg)=='d49df5c445dcf00ada532ab3b0e4283bf4b05ac13f210647658dfd3cec8b34c2'
 with zipfile.ZipFile(forge) as z:old_csv=z.read('conf/methods.csv').decode()
 with zipfile.ZipFile(srg) as z:old_srg=z.read('joined.srg').decode()
 assert hashlib.sha256(old_srg.encode()).hexdigest()=='43d4376110b2638213d3096f80f8395c076eb05b58d6c0dbd22c0d999b37a357'
 new_csv=Path(native_methods).read_text();new_srg=Path(native_srg).read_text()
 evidence=[]
 for s,old,new,obf in [('func_76346_g','getEntity','getTrueSource','j'),('func_76364_f','getSourceOfDamage','getImmediateSource','i')]:
  old_line=next(l for l in old_csv.splitlines() if l.startswith(s+','));new_line=next(l for l in new_csv.splitlines() if l.startswith(s+','))
  assert old_line.split(',')[1]==old and new_line.split(',')[1]==new
  mapping='MD: ro/'+obf+' ()Lsa; net/minecraft/util/DamageSource/'+s+' ()Lnet/minecraft/entity/Entity;';assert mapping in old_srg
  assert 'net/minecraft/util/DamageSource/'+s+' ()Lnet/minecraft/entity/Entity; net/minecraft/util/DamageSource/'+new+' ()Lnet/minecraft/entity/Entity;' in new_srg
  evidence += [old_line,new_line,mapping]
 def javap(classpath,owner):return subprocess.check_output([str(Path(java_home)/'bin/javap'),'-classpath',str(classpath),'-p','-c',owner],text=True)
 old=[javap(client,c) for c in ('ro','rp','rq')];native=[javap(cp,'net.minecraft.util.'+c) for c in ('DamageSource','EntityDamageSource','EntityDamageSourceIndirect')]
 specs=[(0,'j','getTrueSource',[('aconst_null',''),('areturn','')],[('aconst_null',''),('areturn','')]),
 (0,'i','getImmediateSource',[('aload_0',''),('invokevirtual','Method j:()Lsa;'),('areturn','')],[('aload_0',''),('invokevirtual','Method getTrueSource:()Lnet/minecraft/entity/Entity;'),('areturn','')]),
 (1,'j','getTrueSource',[('aload_0',''),('getfield','Field p:Lsa;'),('areturn','')],[('aload_0',''),('getfield','Field damageSourceEntity:Lnet/minecraft/entity/Entity;'),('areturn','')]),
 (2,'i','getImmediateSource',[('aload_0',''),('getfield','Field p:Lsa;'),('areturn','')],[('aload_0',''),('getfield','Field damageSourceEntity:Lnet/minecraft/entity/Entity;'),('areturn','')]),
 (2,'j','getTrueSource',[('aload_0',''),('getfield','Field q:Lsa;'),('areturn','')],[('aload_0',''),('getfield','Field indirectEntity:Lnet/minecraft/entity/Entity;'),('areturn','')])]
 for index,obf,name,old_code,native_code in specs:
  before=method(old[index],'public sa '+obf+'()');after=method(native[index],'public net.minecraft.entity.Entity '+name+'()')
  assert instructions(before)==old_code,(name,instructions(before));assert instructions(after)==native_code,(name,instructions(after));evidence += [before,after]
 # Constructor storage ties the getter fields to direct entity, projectile, and shooter.
 for index,old_sig,new_sig in [(1,'public rp(java.lang.String, sa)','public net.minecraft.util.EntityDamageSource(java.lang.String, net.minecraft.entity.Entity)'),(2,'public rq(java.lang.String, sa, sa)','public net.minecraft.util.EntityDamageSourceIndirect(java.lang.String, net.minecraft.entity.Entity, net.minecraft.entity.Entity)')]:
  before=method(old[index],old_sig);after=method(native[index],new_sig)
  oc=instructions(before);nc=instructions(after)
  if index==1:
   assert oc[-4:]==[('aload_0',''),('aload_2',''),('putfield','Field p:Lsa;'),('return','')]
   assert nc[-4:]==[('aload_0',''),('aload_2',''),('putfield','Field damageSourceEntity:Lnet/minecraft/entity/Entity;'),('return','')]
  else:
   assert oc==[('aload_0',''),('aload_1',''),('aload_2',''),('invokespecial','Method rp."<init>":(Ljava/lang/String;Lsa;)V'),('aload_0',''),('aload_3',''),('putfield','Field q:Lsa;'),('return','')]
   assert nc==[('aload_0',''),('aload_1',''),('aload_2',''),('invokespecial','Method net/minecraft/util/EntityDamageSource."<init>":(Ljava/lang/String;Lnet/minecraft/entity/Entity;)V'),('aload_0',''),('aload_3',''),('putfield','Field indirectEntity:Lnet/minecraft/entity/Entity;'),('return','')]
  evidence += [before,after]
 Path(output).write_text('\n\n'.join(evidence)+'\n');print('PASS pinned old/native MCP+SRG mapping; exact 5 getter implementations and 2 entity-attribution constructors')
 return {str(p):digest(p) for p in (client,forge,srg,native_methods,native_srg)}
