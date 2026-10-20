import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Attributed native class-owner scanner with conservative shadow rejection; never text substitution. */
public final class ConstantAccessScanner {
 public static void main(String[] args)throws Exception {
  final Map<String,String> changes=new HashMap<>();
  for(String row:Files.readAllLines(Paths.get(args[0]))) {String[] r=row.split("\t");changes.put(r[0]+"."+r[1],r[2]);}
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> errors=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(errors,null,null);
  List<String> paths=Files.readAllLines(Paths.get(args[1]));
  JavacTask task=(JavacTask)compiler.getTask(null,fm,errors,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(args[2]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(paths));
  final Trees trees=Trees.instance(task);final SourcePositions positions=trees.getSourcePositions();
  List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
  for(Diagnostic<?> e:errors.getDiagnostics())if(e.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("parse failure: "+e.toString());
  task.analyze(); // Attribution errors are expected in the unfinished port; unresolved receivers are never accepted.
  for(final CompilationUnitTree unit:units) {
   final Map<String,String> imports=new HashMap<>();final Set<String> shadows=new HashSet<>();
   for(ImportTree imp:unit.getImports())if(!imp.isStatic()) {
    String name=imp.getQualifiedIdentifier().toString();
    if(name.equals("net.minecraft.init.Blocks"))imports.put("Blocks",name);
    if(name.equals("net.minecraft.init.Items"))imports.put("Items",name);
   }
   new TreeScanner<Void,Void>() {
    public Void visitVariable(VariableTree t,Void v){shadows.add(t.getName().toString());return super.visitVariable(t,v);}
    public Void visitClass(ClassTree t,Void v){shadows.add(t.getSimpleName().toString());return super.visitClass(t,v);}
    public Void visitTypeParameter(TypeParameterTree t,Void v){shadows.add(t.getName().toString());return super.visitTypeParameter(t,v);}
   }.scan(unit,null);
   final String source=unit.getSourceFile().getCharContent(true).toString();
   new TreePathScanner<Void,Void>() {
    public Void visitImport(ImportTree t,Void v){return null;}
    public Void visitMemberSelect(MemberSelectTree t,Void v) {
     String expression=t.getExpression().toString(),owner=null,name=t.getIdentifier().toString();
     if(imports.containsKey(expression)&&!shadows.contains(expression))owner=imports.get(expression);
     if((expression.equals("net.minecraft.init.Blocks")||expression.equals("net.minecraft.init.Items"))&&!shadows.contains("net"))owner=expression;
     Element receiver=trees.getElement(new TreePath(getCurrentPath(),t.getExpression()));
     if(!(receiver instanceof TypeElement)||!((TypeElement)receiver).getQualifiedName().contentEquals(owner==null?"":owner))owner=null;
     String mapped=owner==null?null:changes.get(owner+"."+name);
     if(mapped!=null&&!mapped.equals(name)) {
      long end=positions.getEndPosition(unit,t),start=end-name.length();
      if(start>=0&&end<=source.length()&&source.substring((int)start,(int)end).equals(name))
       System.out.println(Paths.get(unit.getSourceFile().toUri())+"\t"+start+"\t"+end+"\t"+owner+"\t"+name+"\t"+mapped);
     }
     return super.visitMemberSelect(t,v);
    }
   }.scan(unit,null);
  }
  long n=errors.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count();System.err.println("Attribution diagnostics (not a successful compile): "+n);
  fm.close();
 }
}
