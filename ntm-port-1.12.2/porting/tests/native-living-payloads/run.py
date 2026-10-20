#!/usr/bin/env python3
"""Finite genuine-API, exact-slice, compiler-attribution and source-inverse proof."""
import argparse, collections, hashlib, json, re, subprocess, tempfile, zipfile
from pathlib import Path
from migrate import ROOT, HERE, parse_rows, apply

p = argparse.ArgumentParser()
p.add_argument('--java-home', default='/workspace/shared/nagaport-toolchain/java8')
p.add_argument('--classpath-file', default='/tmp/nagaport-ae2-native-classpath.txt')
p.add_argument('--forge1614', default='/tmp/nagaport-forge1614-userdev.jar')
p.add_argument('--verify-attribution', action='store_true')
a = p.parse_args()
java = Path(a.java_home) / 'bin/java'
javac = Path(a.java_home) / 'bin/javac'
javap = Path(a.java_home) / 'bin/javap'
cp = Path(a.classpath_file).read_text().strip()
tools = str(Path(a.java_home) / 'lib/tools.jar')

def run(command, **kwargs):
    return subprocess.run(list(map(str, command)), check=True, text=True, **kwargs)

def sha(data):
    return hashlib.sha256(data).hexdigest()

run(['python3', HERE / 'check_source.py'])
manifest = json.loads((HERE / 'source-manifest.json').read_text())
with tempfile.TemporaryDirectory(prefix='living-payload-proof-') as temporary:
    tmp = Path(temporary)
    run([javac, '-cp', cp + ':' + tools, '-d', tmp, HERE / 'PayloadScanner.java', HERE / 'NativePayloadTest.java'])
    run([java, '-cp', str(tmp) + ':' + cp, 'NativePayloadTest', a.classpath_file])

    def scan(path, post=False):
        source_list = tmp / 'sources.txt'
        source_list.write_text(str(path) + '\n')
        result = run([java, '-cp', str(tmp) + ':' + tools, 'PayloadScanner', source_list, a.classpath_file] + (['post'] if post else []), capture_output=True)
        return parse_rows(result.stdout)

    # Native/compiler rejection controls are isolated: one source override must not mask another fixture.
    cases = [
        ('hurt_amount_read', 'float f(LivingHurtEvent event){return event.ammount;}', 1),
        ('hurt_source_read', 'Object f(LivingHurtEvent event){return event.source;}', 1),
        ('attack_amount_read', 'float f(LivingAttackEvent event){return event.ammount;}', 1),
        ('attack_source_read', 'Object f(LivingAttackEvent event){return event.source;}', 1),
        ('simple_store', 'void f(LivingHurtEvent event){event.ammount=2F;}', 1),
        ('multiply', 'void f(LivingHurtEvent event){event.ammount*=2D;}', 1),
        ('subtract', 'void f(LivingHurtEvent event){event.ammount-=2D;}', 1),
        ('nested_rhs_read', 'void f(LivingHurtEvent event){event.ammount=event.ammount*2F;}', 2),
        ('unary_read', 'float f(LivingHurtEvent event){return -event.ammount;}', 1),
        ('attack_store', 'void f(LivingAttackEvent event){event.ammount=2F;}', 0),
        ('attack_compound', 'void f(LivingAttackEvent event){event.ammount*=2F;}', 0),
        ('source_store', 'void f(LivingHurtEvent event){event.source=null;}', 0),
        ('increment', 'void f(LivingHurtEvent event){event.ammount++;}', 0),
        ('parenthesized_increment', 'void f(LivingHurtEvent event){(event.ammount)++;}', 0),
        ('assignment_value', 'float f(LivingHurtEvent event){return event.ammount=2F;}', 0),
        ('compound_value', 'float f(LivingHurtEvent event){return event.ammount*=2F;}', 0),
        ('parenthesized_store', 'void f(LivingHurtEvent event){(event.ammount)=2F;}', 0),
        ('other_operator', 'void f(LivingHurtEvent event){event.ammount+=2F;}', 0),
        ('commented_prefix', 'void f(LivingHurtEvent event){event.ammount/*retain*/=2F;}', 0),
        ('nonlocal_receiver', 'LivingHurtEvent event; float f(){return event.ammount;}', 0),
        ('effectful_receiver', 'LivingHurtEvent event(){return null;} float f(){return event().ammount;}', 0),
        ('local_variable', 'float f(){LivingHurtEvent event=null;return event.ammount;}', 0),
        ('typename_receiver', 'float f(){return LivingHurtEvent.ammount;}', 0),
        ('unknown_receiver', 'float f(Unknown event){return event.ammount;}', 0),
        ('unrelated_receiver', 'static class Other{float ammount;} float f(Other event){return event.ammount;}', 0),
        ('subclass_shadow', 'static class Sub extends LivingHurtEvent{float ammount;Sub(){super(null,null,0);}} float f(Sub event){return event.ammount;}', 0),
        ('getter_override', 'static class Sub extends LivingHurtEvent{Sub(){super(null,null,0);} public float getAmount(){return 3;}} float f(LivingHurtEvent event){return event.ammount;}', 0),
        ('setter_override', 'static class Sub extends LivingHurtEvent{Sub(){super(null,null,0);} public void setAmount(float x){}} void f(LivingHurtEvent event){event.ammount=2F;}', 0),
        ('source_override', 'static class Sub extends LivingAttackEvent{Sub(){super(null,null,0);} public net.minecraft.util.DamageSource getSource(){return null;}} Object f(LivingAttackEvent event){return event.source;}', 0),
        ('unrelated_accessor', 'float getAmount(){return 3;} float f(LivingHurtEvent event){return event.ammount;}', 1),
        ('unqualified_shadow', 'float ammount; float f(LivingHurtEvent event){return ammount;}', 0),
    ]
    for name, body, expected in cases:
        fixture = tmp / 'Fixture.java'
        fixture.write_text('import net.minecraftforge.event.entity.living.*; class Fixture {' + body + '}')
        rows = scan(fixture)
        actual = sum(e['kind'] != 'CLOSE' for e in rows)
        assert actual == expected, (name, actual, expected, rows)
    print('PASS', len(cases), 'real compiler fixtures: reads/stores, unsupported writes, receiver identity, shadows and reachable overrides')

    # Every transformed production store is extracted using recorded compiler spans, not recreated.
    writes = []
    for file in manifest['files']:
        raw = (ROOT / file['path']).read_bytes()
        after = raw.decode().encode('utf-16-le')
        before = after
        for e in reversed(file['edits']):
            before = before[:e['after_start']*2] + e['old'].encode('utf-16-le') + before[e['after_end']*2:]
        for e in file['edits']:
            if e['kind'] not in ('SET', 'MULTIPLY', 'SUBTRACT'):
                continue
            close = next(c for c in file['edits'] if c['kind'] == 'CLOSE' and c['group'] == e['group'])
            actual = after[e['after_start']*2:close['after_end']*2].decode('utf-16-le') + ';'
            rhs = before[e['end']*2:close['start']*2].decode('utf-16-le')
            callback = e['kind'] == 'SET' and rhs.startswith('calculateDamage(')
            if callback:
                expected = 'expected = callbackResult;'
            else:
                assert rhs in ('0', '0F', '0.001F', '2F', '0.5F', 'damageMod', 'projectileMod', 'explosionMod', 'reduce'), rhs
                expected = 'expected ' + {'SET': '=', 'MULTIPLY': '*=', 'SUBTRACT': '-='}[e['kind']] + ' ' + rhs + ';'
            writes.append((actual, expected, callback, file['path'], e['method']))
    assert len(writes) == 20
    slice_source = '''import java.lang.reflect.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
public class ProductionSlices {
 static float currentPDT=17F, currentPDR=-0F, callbackResult, observedAmount, observedDT, observedDR;
 static Object observedEntity, observedSource; static int calls, checks; static LivingHurtEvent target;
 static float calculateDamage(EntityLivingBase entity,DamageSource source,float amount,float dt,float dr){
  calls++; observedEntity=entity; observedSource=source; observedAmount=amount; observedDT=dt; observedDR=dr;
  target.setAmount(900F); return callbackResult;
 }
 static void ok(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static void same(float a,float b,String s){ok(Float.floatToRawIntBits(a)==Float.floatToRawIntBits(b),s);}
'''
    for index, (actual, _, _, _, _) in enumerate(writes):
        slice_source += ' static void slice' + str(index) + '(LivingHurtEvent event,float reduce,float damageMod,float projectileMod,float explosionMod){' + actual + '}\n'
    slice_source += ''' public static void main(String[] args)throws Exception{
 net.minecraft.init.Bootstrap.register();
 DamageSource source=new DamageSource("production-slice-proof");
 Field canceled=Event.class.getDeclaredField("isCanceled");canceled.setAccessible(true);
 int[] bits={0,0x80000000,1,0x80000001,0x3f800000,0xc0a00000,0x7f7fffff,0xff7fffff,0x7f800000,0xff800000,0x7fc12345,0xffc54321};
 for(int input:bits)for(int factor:bits){float initial=Float.intBitsToFloat(input),rhs=Float.intBitsToFloat(factor);
 float reduce=rhs,damageMod=rhs,projectileMod=rhs,explosionMod=rhs;callbackResult=rhs;
'''
    for index, (_, expected, callback, _, _) in enumerate(writes):
        slice_source += '{LivingHurtEvent event=new LivingHurtEvent(null,source,initial);target=event;calls=0;canceled.setBoolean(event,true);float expected=initial;'
        slice_source += expected + 'slice' + str(index) + '(event,reduce,damageMod,projectileMod,explosionMod);same(event.getAmount(),expected,"slice ' + str(index) + ' raw result");ok(event.getSource()==source&&event.isCanceled(),"unrelated payload/state");'
        if callback:
            slice_source += 'ok(calls==1&&observedEntity==null&&observedSource==source,"exact callback arguments and once-only evaluation");same(observedAmount,initial,"callback original amount");same(observedDT,currentPDT,"callback DT");same(observedDR,currentPDR,"callback DR");'
        else:
            slice_source += 'ok(calls==0,"no callback added");'
        slice_source += '}\n'
    slice_source += ' }System.out.println("PASS "+checks+" checks of all 20 exact production store slices; calculation callback is a test boundary, not actual damage-formula execution");System.exit(0);}}'
    (tmp / 'ProductionSlices.java').write_text(slice_source)
    (HERE / 'extracted-slice-inventory.json').write_text(json.dumps([dict(index=i, expression=w[0], original_operator_oracle=w[1], callback_boundary=w[2], path=w[3], method=w[4]) for i, w in enumerate(writes)], indent=2) + '\n')
    run([javac, '-cp', cp, '-d', tmp, tmp / 'ProductionSlices.java'])
    run([java, '-cp', str(tmp) + ':' + cp, 'ProductionSlices'])

    # These fixture expressions are themselves migrated by the same attributed-span transformer.
    semantic = tmp / 'SemanticFixture.java'
    semantic.write_text('''import net.minecraftforge.event.entity.living.LivingHurtEvent;
public class SemanticFixture {
 static String trace="";static LivingHurtEvent saved,replacement;
 static float rhs(float x){trace+="R";saved.setAmount(900F);return x;}
 static double rhsDouble(double x){trace+="D";saved.setAmount(901F);return x;}
 static float fail(){trace+="X";throw new IllegalStateException("rhs");}
 static void plain(LivingHurtEvent event,float x){event.ammount=rhs(x);}
 static void multiply(LivingHurtEvent event,float x){event.ammount*=rhs(x);}
 static void subtractDouble(LivingHurtEvent event,double x){event.ammount-=rhsDouble(x);}
 static void overwriteReceiver(LivingHurtEvent event){event.ammount*=(event=replacement).getAmount();}
 static void throwPlain(LivingHurtEvent event){event.ammount=fail();}
 static void throwCompound(LivingHurtEvent event){event.ammount*=fail();}
}
''')
    edits = scan(semantic)
    assert sum(e['kind'] != 'CLOSE' for e in edits) == 6
    semantic.write_bytes(apply(semantic.read_bytes(), edits))
    (tmp / 'SemanticFixtureTest.java').write_text('''import net.minecraftforge.event.entity.living.LivingHurtEvent;
public class SemanticFixtureTest {
 static int checks;
 static void ok(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static void same(float a,float b,String s){ok(Float.floatToRawIntBits(a)==Float.floatToRawIntBits(b),s);}
 interface Action {void run();}
 static void throwsAs(Class<?> type,String trace,Action action){SemanticFixture.trace="";try{action.run();throw new AssertionError("no throw");}catch(Throwable e){ok(e.getClass()==type,"exact exception");ok(SemanticFixture.trace.equals(trace),"exception evaluation order");}}
 public static void main(String[] args){
 int[] bits={0,0x80000000,1,0x80000001,0x3f800000,0xc0a00000,0x7f7fffff,0xff7fffff,0x7f800000,0xff800000,0x7fc12345,0xffc54321};
 for(int a:bits)for(int b:bits){float input=Float.intBitsToFloat(a),rhs=Float.intBitsToFloat(b);
 LivingHurtEvent event=new LivingHurtEvent(null,null,input);SemanticFixture.saved=event;SemanticFixture.trace="";SemanticFixture.multiply(event,rhs);
 float expected=input;expected*=rhs;same(event.getAmount(),expected,"compound reads old field before RHS mutation");ok(SemanticFixture.trace.equals("R"),"RHS exactly once");
 event=new LivingHurtEvent(null,null,input);SemanticFixture.saved=event;SemanticFixture.trace="";SemanticFixture.plain(event,rhs);same(event.getAmount(),rhs,"simple store after RHS mutation");ok(SemanticFixture.trace.equals("R"),"plain RHS once");
 event=new LivingHurtEvent(null,null,input);SemanticFixture.saved=event;SemanticFixture.replacement=new LivingHurtEvent(null,null,rhs);SemanticFixture.overwriteReceiver(event);expected=input;expected*=rhs;same(event.getAmount(),expected,"outer target captured before RHS receiver reassignment");same(SemanticFixture.replacement.getAmount(),rhs,"replacement not written");
 }
 double[] doubles={0D,-0D,1D,-0.5D,Double.MAX_VALUE,-Double.MAX_VALUE,Double.MIN_VALUE,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NaN};
 for(int a:bits)for(double rhs:doubles){float input=Float.intBitsToFloat(a);LivingHurtEvent event=new LivingHurtEvent(null,null,input);SemanticFixture.saved=event;SemanticFixture.trace="";SemanticFixture.subtractDouble(event,rhs);float expected=input;expected-=rhs;same(event.getAmount(),expected,"compound implicit float narrowing");ok(SemanticFixture.trace.equals("D"),"double RHS once");}
 SemanticFixture.saved=new LivingHurtEvent(null,null,5F);
 throwsAs(NullPointerException.class,"R",()->SemanticFixture.plain(null,3F));
 throwsAs(NullPointerException.class,"",()->SemanticFixture.multiply(null,3F));
 throwsAs(NullPointerException.class,"",()->SemanticFixture.subtractDouble(null,3D));
 throwsAs(IllegalStateException.class,"X",()->SemanticFixture.throwPlain(null));
 throwsAs(NullPointerException.class,"",()->SemanticFixture.throwCompound(null));
 LivingHurtEvent event=new LivingHurtEvent(null,null,-0F);
 throwsAs(IllegalStateException.class,"X",()->SemanticFixture.throwPlain(event));same(event.getAmount(),-0F,"simple throwing RHS leaves amount");
 throwsAs(IllegalStateException.class,"X",()->SemanticFixture.throwCompound(event));same(event.getAmount(),-0F,"compound throwing RHS leaves amount");
 System.out.println("PASS "+checks+" compiler-transformed side-effect, receiver reassignment, cast, exception and null-order checks");System.exit(0);
 }}''')
    run([javac, '-cp', cp, '-d', tmp, semantic, tmp / 'SemanticFixtureTest.java'])
    run([java, '-cp', str(tmp) + ':' + cp, 'SemanticFixtureTest'])

    # Authentic original artifacts, no stand-in Forge classes.
    with zipfile.ZipFile(a.forge1614) as archive:
        legacy = tmp / 'legacy.jar'
        legacy.write_bytes(archive.read('binaries.jar'))
        for simple in ('LivingHurtEvent', 'LivingAttackEvent'):
            owner = 'net.minecraftforge.event.entity.living.' + simple
            source = archive.read('src/main/java/' + owner.replace('.', '/') + '.java').decode()
            assert 'public final DamageSource source;' in source
            assert ('public float ammount;' if simple == 'LivingHurtEvent' else 'public final float ammount;') in source
            assert 'this.source = source;' in source and 'this.ammount = ammount;' in source
            old = run([javap, '-p', '-c', '-classpath', legacy, owner], capture_output=True).stdout
            assert 'getAmount(' not in old and 'setAmount(' not in old and 'getSource(' not in old
            assert 'putfield' in old and 'ammount:F' in old
            native = run([javap, '-p', '-c', '-classpath', cp, owner], capture_output=True).stdout
            (HERE / ('original-' + simple + '.javap.txt')).write_text(old)
            (HERE / ('native-' + simple + '.javap.txt')).write_text(native)
    print('PASS original Forge 1614 source/binary payload contracts; original userdev SHA256', sha(Path(a.forge1614).read_bytes()))
    dependency_hashes = [{'path': x, 'sha256': sha(Path(x).read_bytes())} for x in cp.split(':') if Path(x).is_file()]
    (HERE / 'dependency-sha256.json').write_text(json.dumps(dependency_hashes, indent=2) + '\n')

    if a.verify_attribution:
        sources = tmp / 'all-sources.txt'
        sources.write_text('\n'.join(map(str, sorted((ROOT / 'src/main/java').rglob('*.java')))) + '\n')
        result = run([java, '-Xmx3g', '-cp', str(tmp) + ':' + tools, 'PayloadScanner', sources, a.classpath_file, 'post'], capture_output=True)
        print(result.stderr.strip())
        expected = collections.Counter()
        for file in manifest['files']:
            for e in file['edits']:
                for match in re.finditer(r'\b(getAmount|getSource|setAmount)\b', e['new']):
                    expected[(file['path'], e['after_start'] + match.start(), match.group(), e['owner'], e['receiver'], e['method'])] += 1
        actual = collections.Counter()
        unrelated = 0
        selected = {(key[0], key[1]) for key in expected}
        for e in parse_rows(result.stdout):
            relative = str(Path(e['path']).relative_to(ROOT))
            key = (relative, e['start'], e['new'], e['owner'], e['receiver'], e['method'])
            if (relative, e['start']) in selected:
                actual[key] += 1
            else:
                unrelated += 1
        assert actual == expected, (actual - expected, expected - actual)
        assert sum(actual.values()) == 62
        print('PASS complete native declaring-method attribution: 62 exact generated call sites from 52 accesses;', unrelated, 'pre-existing unrelated calls excluded')
print('PASS bounded native LivingHurtEvent/LivingAttackEvent payload proof; not whole-mod compilation, event dispatch, damage simulation or gameplay')
