import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Reports only compiler-attributed DamageSource-family receivers and declarations. */
public final class DamageSourceScanner {
 public static void main(String[] a)throws Exception {
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
  DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(diagnostics,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1])),"UTF-8").trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));
  final Trees trees=Trees.instance(task);final SourcePositions positions=trees.getSourcePositions();
  List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
  for(Diagnostic<?> d:diagnostics.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("Parse failure: "+d);
  task.analyze();
  for(final CompilationUnitTree unit:units){final String source=unit.getSourceFile().getCharContent(true).toString();
   new TreePathScanner<Void,Void>(){
    boolean damage(TypeMirror type){
     Set<String> seen=new HashSet<>();
     while(type!=null&&type.getKind()==TypeKind.DECLARED){
      TypeElement e=(TypeElement)((DeclaredType)type).asElement();String name=e.getQualifiedName().toString();
      if(name.equals("net.minecraft.util.DamageSource"))return true;
      if(!seen.add(name))return false;type=e.getSuperclass();
     }return false;
    }
    boolean target(String name){return Arrays.asList("getEntity","getSourceOfDamage","getTrueSource","getImmediateSource").contains(name);}
    String context(){for(TreePath p=getCurrentPath();p!=null;p=p.getParentPath())if(p.getLeaf() instanceof MethodTree){Element e=trees.getElement(p);return e.getEnclosingElement()+"#"+e.toString();}return "<initializer>";}
    void emit(String kind,long start,long end,String name,String receiver,String owner){
     if(start<0||!source.substring((int)start,(int)end).equals(name))throw new AssertionError("Token span");
     System.out.println(Paths.get(unit.getSourceFile().toUri())+"\t"+kind+"\t"+start+"\t"+end+"\t"+name+"\t"+receiver+"\t"+owner+"\t"+context());
    }
    public Void visitClass(ClassTree c,Void v){Element e=trees.getElement(getCurrentPath());if(e!=null&&damage(e.asType()))System.err.println("SOURCE_DESCENDANT\t"+e+"\t"+((TypeElement)e).getSuperclass());return super.visitClass(c,v);}
    public Void visitMethod(MethodTree m,Void v){
     String name=m.getName().toString();Element e=trees.getElement(getCurrentPath());
     if(target(name)&&e!=null&&damage(e.getEnclosingElement().asType())){
      if(!m.getParameters().isEmpty()||!((ExecutableElement)e).getReturnType().toString().equals("net.minecraft.entity.Entity"))throw new AssertionError("Unexpected accessor signature: "+e);
      long start=positions.getEndPosition(unit,m.getReturnType());start=source.indexOf(name,(int)start);
      emit("DECL",start,start+name.length(),name,e.getEnclosingElement().toString(),e.getEnclosingElement().toString());
     }return super.visitMethod(m,v);
    }
    public Void visitMethodInvocation(MethodInvocationTree call,Void v){
     ExpressionTree select=call.getMethodSelect();String name;TypeMirror receiver;long end;
     if(select instanceof MemberSelectTree){MemberSelectTree m=(MemberSelectTree)select;name=m.getIdentifier().toString();TreePath mp=new TreePath(getCurrentPath(),m);receiver=trees.getTypeMirror(new TreePath(mp,m.getExpression()));end=positions.getEndPosition(unit,m);}
     else if(select instanceof IdentifierTree){name=((IdentifierTree)select).getName().toString();Element e=trees.getElement(getCurrentPath());receiver=e==null?null:e.getEnclosingElement().asType();end=positions.getEndPosition(unit,select);}
     else return super.visitMethodInvocation(call,v);
     if(target(name)&&damage(receiver)){
      if(!call.getArguments().isEmpty())throw new AssertionError("Unexpected accessor arguments");
      Element e=trees.getElement(getCurrentPath());String owner=e!=null&&e.getKind()==ElementKind.METHOD?e.getEnclosingElement().toString():"<unresolved-legacy>";
      emit("CALL",end-name.length(),end,name,receiver.toString(),owner);
     }return super.visitMethodInvocation(call,v);
    }
    public Void visitMemberReference(MemberReferenceTree m,Void v){if(target(m.getName().toString()))throw new AssertionError("Member reference needs separate audit: "+m);return super.visitMemberReference(m,v);}
   }.scan(unit,null);
  }
  System.err.println("Attribution errors in unfinished full source (NOT compile success): "+diagnostics.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
