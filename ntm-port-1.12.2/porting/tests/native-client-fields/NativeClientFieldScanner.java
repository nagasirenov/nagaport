import java.nio.file.*;import java.util.*;import javax.tools.*;import javax.lang.model.element.*;import javax.lang.model.type.*;import com.sun.source.tree.*;import com.sun.source.util.*;
public class NativeClientFieldScanner {
 public static void main(String[] a)throws Exception{
 JavaCompiler c=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject>d=new DiagnosticCollector<>();StandardJavaFileManager f=c.getStandardFileManager(d,null,null);JavacTask t=(JavacTask)c.getTask(null,f,d,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,f.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));Trees tr=Trees.instance(t);List<CompilationUnitTree>us=new ArrayList<>();for(CompilationUnitTree u:t.parse())us.add(u);t.analyze();
 for(CompilationUnitTree u:us)new TreePathScanner<Void,Void>(){
 boolean sub(TypeMirror ty,String owner){TypeElement e=t.getElements().getTypeElement(owner);return ty!=null&&ty.getKind()==TypeKind.DECLARED&&e!=null&&t.getTypes().isSubtype(t.getTypes().erasure(ty),t.getTypes().erasure(e.asType()));}
 boolean shadow(TypeMirror ty,String name){if(ty==null||ty.getKind()!=TypeKind.DECLARED)return true;Element e=t.getTypes().asElement(ty);for(Element m:t.getElements().getAllMembers((TypeElement)e))if(m.getKind()==ElementKind.FIELD&&m.getSimpleName().contentEquals(name))return true;return false;}
 void check(Tree node,String name,ExpressionTree receiver){if(!Arrays.asList("fontRenderer","player","world").contains(name))return;Element e=tr.getElement(getCurrentPath());if(e instanceof VariableElement){long end=tr.getSourcePositions().getEndPosition(u,node),start=end-name.length();System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+start+"\t"+end+"\t"+name+"\t"+e.getEnclosingElement()+"\t"+e.asType()+"\t"+e.getModifiers());}}

 public Void visitMemberSelect(MemberSelectTree n,Void v){check(n,n.getIdentifier().toString(),n.getExpression());return super.visitMemberSelect(n,v);}
 public Void visitIdentifier(IdentifierTree n,Void v){check(n,n.getName().toString(),null);return super.visitIdentifier(n,v);}
 }.scan(u,null);System.err.println("Attribution diagnostics, not compile success: "+d.getDiagnostics().stream().filter(x->x.getKind()==Diagnostic.Kind.ERROR).count());
 }
}
