import java.nio.file.*;import java.util.*;import javax.tools.*;import javax.lang.model.element.*;import javax.lang.model.type.*;import com.sun.source.tree.*;import com.sun.source.util.*;
public class Scan {
 public static void main(String[] a)throws Exception {
 JavaCompiler c=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject>d=new DiagnosticCollector<>();StandardJavaFileManager f=c.getStandardFileManager(d,null,null);
 JavacTask t=(JavacTask)c.getTask(null,f,d,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,f.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));
 List<CompilationUnitTree>us=new ArrayList<>();for(CompilationUnitTree u:t.parse())us.add(u);t.analyze();Trees trees=Trees.instance(t);
 for(CompilationUnitTree u:us)new TreePathScanner<Void,Void>() {
 public Void visitMemberSelect(MemberSelectTree m,Void v){String n=m.getIdentifier().toString();if(n.equals("ridingEntity")||n.equals("riddenByEntity")){
 TreePath rp=new TreePath(getCurrentPath(),m.getExpression());TypeMirror typ=trees.getTypeMirror(rp);String receiver=String.valueOf(typ);boolean proven=false;
 Set<String>seen=new HashSet<>();while(typ!=null&&typ.getKind()==TypeKind.DECLARED){TypeElement e=(TypeElement)((DeclaredType)typ).asElement();String name=e.getQualifiedName().toString();if(!seen.add(name))break;if(name.equals("net.minecraft.entity.Entity")){proven=true;break;}boolean shadow=false;for(Element x:e.getEnclosedElements())if(x.getSimpleName().contentEquals(n)||x.getSimpleName().contentEquals("getRidingEntity")||x.getSimpleName().contentEquals("getPassengers")||x.getSimpleName().contentEquals("isBeingRidden"))shadow=true;if(shadow)break;typ=e.getSuperclass();}
 if(proven)System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+trees.getSourcePositions().getStartPosition(u,m)+"\t"+trees.getSourcePositions().getEndPosition(u,m)+"\t"+n+"\t"+receiver);
 }return super.visitMemberSelect(m,v);}
 }.scan(u,null);
 System.err.println("Unfinished full-source attribution diagnostics: "+d.getDiagnostics().stream().filter(x->x.getKind()==Diagnostic.Kind.ERROR).count());
 }
}
