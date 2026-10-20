import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Select only proven Slot descendants without either legacy or destination field shadows. */
public final class NativeFieldScanner {
 public static void main(String[] a)throws Exception {
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> errors=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(errors,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,errors,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));
  final Trees trees=Trees.instance(task);final SourcePositions pos=trees.getSourcePositions();
  List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
  for(Diagnostic<?> e:errors.getDiagnostics())if(e.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("parse failure: "+e);
  task.analyze();final Types types=task.getTypes();
  for(final CompilationUnitTree u:units){final String src=u.getSourceFile().getCharContent(true).toString();
   new TreePathScanner<Void,Void>(){
    String oldName, newName, owner, fieldType;
    boolean entity(TypeMirror type){
     if(type==null||type.getKind()!=TypeKind.DECLARED)return false;
     for(Element f:task.getElements().getAllMembers((TypeElement)((DeclaredType)type).asElement()))if(f.getKind()==ElementKind.FIELD){
      if(f.getSimpleName().contentEquals(oldName))return false;
      if(f.getSimpleName().contentEquals(newName)&&!((TypeElement)f.getEnclosingElement()).getQualifiedName().contentEquals(owner))return false;
     }
     Set<String> seen=new HashSet<>();
     while(type!=null&&type.getKind()==TypeKind.DECLARED){
      TypeElement e=(TypeElement)((DeclaredType)type).asElement();String name=e.getQualifiedName().toString();if(!seen.add(name))return false;
      if(name.equals(owner)){
       for(Element f:e.getEnclosedElements())if(f.getKind()==ElementKind.FIELD&&f.getSimpleName().contentEquals(newName))return f.asType().toString().equals(fieldType)&&f.getModifiers().contains(Modifier.PUBLIC);
       return false;
      }
      for(Element f:e.getEnclosedElements())if(f.getKind()==ElementKind.FIELD&&(f.getSimpleName().contentEquals(oldName)||f.getSimpleName().contentEquals(newName)))return false;
      type=e.getSuperclass();
     }return false;
    }
    boolean select(String name) {
      oldName=name;
      if(name.equals("xDisplayPosition")||name.equals("yDisplayPosition")){newName=name.startsWith("x")?"xPos":"yPos";owner="net.minecraft.inventory.Slot";fieldType="int";return true;} return false;
    }
    void emit(Tree t,String replacement,String receiver){long end=pos.getEndPosition(u,t),start=end-oldName.length();if(start>=0&&end<=src.length()&&src.substring((int)start,(int)end).equals(oldName))System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+start+"\t"+end+"\t"+replacement+"\t"+receiver+"\t"+oldName+"\t"+owner);}
    public Void visitImport(ImportTree t,Void v){return null;}
    public Void visitMemberSelect(MemberSelectTree t,Void v){if(select(t.getIdentifier().toString())){
     TreePath p=new TreePath(getCurrentPath(),t.getExpression());TypeMirror receiver=trees.getTypeMirror(p);
     // A type-name expression is not an instance receiver.
     Element e=trees.getElement(p);if(!(e instanceof TypeElement)&&entity(receiver))emit(t,newName,receiver.toString());
    }return super.visitMemberSelect(t,v);}
    public Void visitIdentifier(IdentifierTree t,Void v){if(select(t.getName().toString())){
     Element old=trees.getElement(getCurrentPath());if(old!=null&&(old.getKind()==ElementKind.FIELD||old.getKind()==ElementKind.LOCAL_VARIABLE||old.getKind()==ElementKind.PARAMETER))return null;
     TreePath p=getCurrentPath();while(p!=null&&!(p.getLeaf() instanceof ClassTree)){
      Tree node=p.getLeaf();
      if(node instanceof MethodTree&&((MethodTree)node).getModifiers().getFlags().contains(Modifier.STATIC))return null;
      if(node instanceof VariableTree&&((VariableTree)node).getModifiers().getFlags().contains(Modifier.STATIC))return null;
      if(node instanceof BlockTree&&((BlockTree)node).isStatic())return null;
      p=p.getParentPath();
     }
     if(p!=null){Element e=trees.getElement(p);if(e instanceof TypeElement&&entity(e.asType()))emit(t,"this."+newName,e.asType().toString());}
    }return super.visitIdentifier(t,v);}
   }.scan(u,null);
  }
  System.err.println("Attribution diagnostics (unfinished source, not compile success): "+errors.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
