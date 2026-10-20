import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Strict read-only attribution for one event owner/field/accessor per run. */
public final class EventAccessorScanner {
 public static void main(String[] a)throws Exception {
  final String owner=a[2], field=a[3], getter=a[4], fieldType=a[5];
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> errors=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(errors,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,errors,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));
  final Trees trees=Trees.instance(task);final SourcePositions pos=trees.getSourcePositions();
  List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
  for(Diagnostic<?> e:errors.getDiagnostics())if(e.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("parse failure: "+e);
  task.analyze();final Types types=task.getTypes();
  final List<TypeMirror> overrides=new ArrayList<>();
  for(final CompilationUnitTree unit:units)new TreePathScanner<Void,Void>(){
   public Void visitMethod(MethodTree m,Void v){if(m.getName().contentEquals(getter)){Element e=trees.getElement(getCurrentPath());if(e!=null)overrides.add(e.getEnclosingElement().asType());}return super.visitMethod(m,v);}
  }.scan(unit,null);
  for(final CompilationUnitTree u:units){final String src=u.getSourceFile().getCharContent(true).toString();
   new TreePathScanner<Void,Void>(){
    boolean entity(TypeMirror type){
     if(type==null||type.getKind()!=TypeKind.DECLARED)return false;
     for(TypeMirror o:overrides)if(types.isSubtype(o,type)||types.isSubtype(type,o))return false;
     for(Element f:task.getElements().getAllMembers((TypeElement)((DeclaredType)type).asElement()))if(f.getKind()==ElementKind.FIELD&&f.getSimpleName().contentEquals(field)&&!f.getEnclosingElement().toString().equals(owner))return false;
     Set<String> seen=new HashSet<>();
     while(type!=null&&type.getKind()==TypeKind.DECLARED){
      TypeElement e=(TypeElement)((DeclaredType)type).asElement();String name=e.getQualifiedName().toString();if(!seen.add(name))return false;
      if(name.equals(owner)){
       for(Element f:e.getEnclosedElements())if(f.getKind()==ElementKind.FIELD&&f.getSimpleName().contentEquals(field))return f.asType().toString().equals(fieldType)&&f.getModifiers().contains(Modifier.PRIVATE);
       return false;
      }
      for(Element f:e.getEnclosedElements()) {
       if(f.getKind()==ElementKind.FIELD&&f.getSimpleName().contentEquals(field))return false;
       if(f.getKind()==ElementKind.METHOD&&f.getSimpleName().contentEquals(getter))return false;
      }
      type=e.getSuperclass();
     }return false;
    }
    void emit(Tree t,String replacement,String receiver){
     Tree written=t;TreePath ancestor=getCurrentPath().getParentPath();
     while(ancestor!=null&&ancestor.getLeaf() instanceof ParenthesizedTree){written=ancestor.getLeaf();ancestor=ancestor.getParentPath();}
     Tree parent=ancestor==null?null:ancestor.getLeaf();
     if(parent instanceof AssignmentTree && ((AssignmentTree)parent).getVariable()==written)return;
     if(parent instanceof CompoundAssignmentTree || parent instanceof UnaryTree)return;
long end=pos.getEndPosition(u,t),start=end-field.length();if(start>=0&&end<=src.length()&&src.substring((int)start,(int)end).equals(field))System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+start+"\t"+end+"\t"+replacement+"\t"+receiver);}
    public Void visitMethodInvocation(MethodInvocationTree t,Void v){
     if(a.length>6&&t.getMethodSelect() instanceof MemberSelectTree){
      MemberSelectTree m=(MemberSelectTree)t.getMethodSelect();Element e=trees.getElement(getCurrentPath());
      TreePath mp=new TreePath(getCurrentPath(),m);TypeMirror r=trees.getTypeMirror(new TreePath(mp,m.getExpression()));
      if(m.getIdentifier().contentEquals(getter)&&e!=null&&e.getKind()==ElementKind.METHOD&&e.getEnclosingElement().toString().equals(owner)&&t.getArguments().isEmpty()&&entity(r)){
       long end=pos.getEndPosition(u,m);System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+(end-getter.length())+"\t"+end+"\t"+getter+"()\t"+r.toString());
      }
     }return super.visitMethodInvocation(t,v);
    }
    public Void visitImport(ImportTree t,Void v){return null;}
    public Void visitMemberSelect(MemberSelectTree t,Void v){if(t.getIdentifier().contentEquals(field)){
     TreePath p=new TreePath(getCurrentPath(),t.getExpression());TypeMirror receiver=trees.getTypeMirror(p);
     // A type-name expression is not an instance receiver.
     Element e=trees.getElement(p);if(!(e instanceof TypeElement)&&entity(receiver))emit(t,getter+"()",receiver.toString());
    }return super.visitMemberSelect(t,v);}

   }.scan(u,null);
  }
  System.err.println("Attribution diagnostics (unfinished source, not compile success): "+errors.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
