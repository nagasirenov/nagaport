import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Reconstructed after workspace loss. Native attribution must be rerun before claiming current results. */
public final class RenderEventScanner {
 static final class Rule {
  final String owner, field, getter, type;
  final List<TypeMirror> overrides = new ArrayList<>();
  Rule(String line) { String[] x=line.split("\\t"); owner=x[0]; field=x[1]; getter=x[2]; type=x[3]; }
 }
 public static void main(String[] args) throws Exception {
  final List<Rule> rules=new ArrayList<>(); for(String line:Files.readAllLines(Paths.get(args[2]))) if(!line.trim().isEmpty()) rules.add(new Rule(line));
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler(); DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(diagnostics,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(args[1]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
  final Trees trees=Trees.instance(task); final SourcePositions positions=trees.getSourcePositions();
  List<CompilationUnitTree> units=new ArrayList<>(); for(CompilationUnitTree u:task.parse()) units.add(u);
  for(Diagnostic<?> d:diagnostics.getDiagnostics()) if(d.getKind()==Diagnostic.Kind.ERROR) throw new AssertionError("parse failure: "+d);
  task.analyze(); final Types types=task.getTypes();
  for(CompilationUnitTree unit:units) new TreePathScanner<Void,Void>() {
   public Void visitMethod(MethodTree m,Void v) {
    if(m.getParameters().isEmpty()) for(Rule r:rules) if(m.getName().contentEquals(r.getter)) {
     Element e=trees.getElement(getCurrentPath()); if(e!=null) r.overrides.add(e.getEnclosingElement().asType());
    } return super.visitMethod(m,v);
   }
  }.scan(unit,null);
  for(final CompilationUnitTree unit:units) { final String src=unit.getSourceFile().getCharContent(true).toString();
   new TreePathScanner<Void,Void>() {
    boolean eligible(TypeMirror receiver,Rule r) {
     if(receiver==null||receiver.getKind()!=TypeKind.DECLARED) return false;
     for(TypeMirror o:r.overrides) if(types.isSubtype(types.erasure(o),types.erasure(receiver))||types.isSubtype(types.erasure(receiver),types.erasure(o))) return false;
     for(Element member:task.getElements().getAllMembers((TypeElement)((DeclaredType)receiver).asElement()))
      if(member.getKind()==ElementKind.FIELD&&member.getSimpleName().contentEquals(r.field)&&!member.getEnclosingElement().toString().equals(r.owner)) return false;
     TypeMirror walk=receiver; Set<String> seen=new HashSet<>();
     while(walk!=null&&walk.getKind()==TypeKind.DECLARED) {
      TypeElement e=(TypeElement)((DeclaredType)walk).asElement(); String name=e.getQualifiedName().toString(); if(!seen.add(name)) return false;
      if(name.equals(r.owner)) {
       boolean field=false,getter=false;
       for(Element child:e.getEnclosedElements()) {
        if(child.getKind()==ElementKind.FIELD&&child.getSimpleName().contentEquals(r.field)) field=types.erasure(child.asType()).toString().equals(r.type)&&child.getModifiers().containsAll(Arrays.asList(Modifier.PRIVATE,Modifier.FINAL));
        if(child.getKind()==ElementKind.METHOD&&child.getSimpleName().contentEquals(r.getter)) {
         ExecutableElement method=(ExecutableElement)child;
         getter=method.getParameters().isEmpty()&&method.getModifiers().contains(Modifier.PUBLIC)&&types.erasure(method.getReturnType()).toString().equals(r.type);
        }
       } return field&&getter;
      }
      for(Element child:e.getEnclosedElements()) if((child.getKind()==ElementKind.FIELD&&child.getSimpleName().contentEquals(r.field))||(child.getKind()==ElementKind.METHOD&&child.getSimpleName().contentEquals(r.getter)&&((ExecutableElement)child).getParameters().isEmpty())) return false;
      walk=e.getSuperclass();
     } return false;
    }
    boolean readOnly(Tree t) {
     Tree written=t; TreePath parent=getCurrentPath().getParentPath();
     while(parent!=null&&parent.getLeaf() instanceof ParenthesizedTree) {written=parent.getLeaf();parent=parent.getParentPath();}
     Tree p=parent==null?null:parent.getLeaf();
     if(p instanceof AssignmentTree&&((AssignmentTree)p).getVariable()==written) return false;
     if(p instanceof CompoundAssignmentTree&&((CompoundAssignmentTree)p).getVariable()==written) return false;
     if(p instanceof UnaryTree) {Tree.Kind k=p.getKind();if(k==Tree.Kind.PREFIX_INCREMENT||k==Tree.Kind.PREFIX_DECREMENT||k==Tree.Kind.POSTFIX_INCREMENT||k==Tree.Kind.POSTFIX_DECREMENT) return false;}
     return true;
    }
    void emit(long start,long end,Rule r,TypeMirror receiver) {System.out.println(Paths.get(unit.getSourceFile().toUri())+"\t"+start+"\t"+end+"\t"+r.field+"\t"+r.getter+"()\t"+receiver+"\t"+r.owner);}
    public Void visitImport(ImportTree t,Void v) {return null;}
    public Void visitMemberSelect(MemberSelectTree t,Void v) {
     if(args.length==3) for(Rule r:rules) if(t.getIdentifier().contentEquals(r.field)) {
      TreePath p=new TreePath(getCurrentPath(),t.getExpression());TypeMirror receiver=trees.getTypeMirror(p);
      if(!(trees.getElement(p) instanceof TypeElement)&&eligible(receiver,r)&&readOnly(t)) {
       long end=positions.getEndPosition(unit,t),start=end-r.field.length();
       if(start>=0&&end<=src.length()&&src.substring((int)start,(int)end).equals(r.field)) emit(start,end,r,receiver);
      }
     } return super.visitMemberSelect(t,v);
    }
    public Void visitMethodInvocation(MethodInvocationTree t,Void v) {
     if(args.length>3&&t.getMethodSelect() instanceof MemberSelectTree&&t.getArguments().isEmpty()) {
      MemberSelectTree m=(MemberSelectTree)t.getMethodSelect(); Element selected=trees.getElement(getCurrentPath()); TreePath p=new TreePath(getCurrentPath(),m); TypeMirror receiver=trees.getTypeMirror(new TreePath(p,m.getExpression()));
      for(Rule r:rules) if(m.getIdentifier().contentEquals(r.getter)&&selected!=null&&selected.getKind()==ElementKind.METHOD&&selected.getEnclosingElement().toString().equals(r.owner)&&eligible(receiver,r)) {
       long end=positions.getEndPosition(unit,m); emit(end-r.getter.length(),end+2,r,receiver);
      }
     } return super.visitMethodInvocation(t,v);
    }
   }.scan(unit,null);
  }
  System.err.println("Attribution diagnostics (unfinished module, not build success): "+diagnostics.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count()); fm.close();
 }
}
