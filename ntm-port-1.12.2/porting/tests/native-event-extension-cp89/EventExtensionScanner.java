import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** CP89 exact native owner/type read attribution. No source API stubs. */
public final class EventExtensionScanner {
 static final class Rule {
  final String owner, field, getter, type, setter, storage;
  final List<TypeMirror> overrides = new ArrayList<>();
  Rule(String line) { String[] x=line.split("\\t"); owner=x[0]; field=x[1]; getter=x[2]; type=x[3]; setter=x[4]; storage=x[5]; }
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
    for(Rule r:rules) if((m.getParameters().isEmpty()&&m.getName().contentEquals(r.getter))||(m.getParameters().size()==1&&m.getName().contentEquals(r.setter))) {
     Element e=trees.getElement(getCurrentPath()); if(e!=null) r.overrides.add(e.getEnclosingElement().asType());
    } return super.visitMethod(m,v);
   }
  }.scan(unit,null);
  for(final CompilationUnitTree unit:units) { final String src=unit.getSourceFile().getCharContent(true).toString();
   new TreePathScanner<Void,Void>() {
    boolean transparent(String name,Rule r) { return name.equals("net.minecraftforge.event.entity.player.ItemTooltipEvent")&&r.owner.equals("net.minecraftforge.event.entity.player.PlayerEvent")&&r.getter.equals("getEntityPlayer"); }
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
        if(child.getKind()==ElementKind.FIELD&&child.getSimpleName().contentEquals(r.field)) field=types.erasure(child.asType()).toString().equals(r.type)&&child.getModifiers().contains(Modifier.PRIVATE)&&(r.storage.equals("mutable")||child.getModifiers().contains(Modifier.FINAL));
        if(child.getKind()==ElementKind.METHOD&&child.getSimpleName().contentEquals(r.getter)) {
         ExecutableElement method=(ExecutableElement)child;
         getter=method.getParameters().isEmpty()&&method.getModifiers().contains(Modifier.PUBLIC)&&types.erasure(method.getReturnType()).toString().equals(r.type);
        }
       } return field&&getter;
      }
      for(Element child:e.getEnclosedElements()) if((child.getKind()==ElementKind.FIELD&&child.getSimpleName().contentEquals(r.field))||(child.getKind()==ElementKind.METHOD&&((child.getSimpleName().contentEquals(r.getter)&&((ExecutableElement)child).getParameters().isEmpty()&&!transparent(name,r))||(child.getSimpleName().contentEquals(r.setter)&&((ExecutableElement)child).getParameters().size()==1)))) return false;
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
    String source(Tree t) {return src.substring((int)positions.getStartPosition(unit,t),(int)positions.getEndPosition(unit,t));}
    void write(Tree whole,ExpressionTree target,ExpressionTree rhs,boolean compound) {
     if(args.length<=3||!args[3].equals("writes")||!(getCurrentPath().getParentPath().getLeaf() instanceof ExpressionStatementTree)||!(target instanceof MemberSelectTree)) return;
     MemberSelectTree select=(MemberSelectTree)target; if(!(select.getExpression() instanceof IdentifierTree)) return;
     TreePath tp=new TreePath(getCurrentPath(),target),rp=new TreePath(tp,select.getExpression());
     final Element local=trees.getElement(rp);TypeMirror receiver=trees.getTypeMirror(rp);
     if(local==null||(local.getKind()!=ElementKind.PARAMETER&&local.getKind()!=ElementKind.LOCAL_VARIABLE)) return;
     final boolean[] changed={false};new TreePathScanner<Void,Void>() {
      void mark(ExpressionTree t) {Element e=trees.getElement(new TreePath(getCurrentPath(),t));if(local.equals(e))changed[0]=true;}
      public Void visitAssignment(AssignmentTree t,Void v){mark(t.getVariable());return super.visitAssignment(t,v);}
      public Void visitCompoundAssignment(CompoundAssignmentTree t,Void v){mark(t.getVariable());return super.visitCompoundAssignment(t,v);}
      public Void visitUnary(UnaryTree t,Void v){if(t.getKind()==Tree.Kind.PREFIX_INCREMENT||t.getKind()==Tree.Kind.PREFIX_DECREMENT||t.getKind()==Tree.Kind.POSTFIX_INCREMENT||t.getKind()==Tree.Kind.POSTFIX_DECREMENT)mark(t.getExpression());return super.visitUnary(t,v);}
     }.scan(new TreePath(getCurrentPath(),rhs),null);if(changed[0])return;
     for(Rule r:rules)if(!r.setter.equals("-")&&select.getIdentifier().contentEquals(r.field)&&eligible(receiver,r)) {
      TypeElement owner=task.getElements().getTypeElement(r.owner);boolean setter=false;
      for(Element child:owner.getEnclosedElements())if(child.getKind()==ElementKind.METHOD&&child.getSimpleName().contentEquals(r.setter)) {
       ExecutableElement m=(ExecutableElement)child;setter=m.getModifiers().contains(Modifier.PUBLIC)&&m.getReturnType().getKind()==TypeKind.VOID&&m.getParameters().size()==1&&types.erasure(m.getParameters().get(0).asType()).toString().equals(r.type);
      }if(!setter)return;
      TypeMirror valueType=trees.getTypeMirror(new TreePath(getCurrentPath(),rhs));
      if(valueType==null||valueType.getKind()==TypeKind.ERROR)return;
      if(!compound) {
       TypeMirror fieldType=null;for(Element child:owner.getEnclosedElements())if(child.getKind()==ElementKind.FIELD&&child.getSimpleName().contentEquals(r.field))fieldType=child.asType();
       if(fieldType==null||!types.isAssignable(valueType,fieldType))return;
      } else if(!(valueType.getKind().isPrimitive())||valueType.getKind()==TypeKind.BOOLEAN)return;
      String object=source(select.getExpression()),value=source(rhs);
      if(compound) {if(!r.type.equals("float")||whole.getKind()!=Tree.Kind.MULTIPLY_ASSIGNMENT)return;value="(float) ("+object+"."+r.getter+"() * ("+value+"))";}
      String replacement=object+"."+r.setter+"("+value+")";
      System.out.println(Paths.get(unit.getSourceFile().toUri())+"\t"+positions.getStartPosition(unit,whole)+"\t"+positions.getEndPosition(unit,whole)+"\t"+r.field+"\t"+r.setter+"\t"+receiver+"\t"+r.owner+"\t"+(compound?"multiply":"assign")+"\t"+Base64.getEncoder().encodeToString(replacement.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
     }
    }
    public Void visitAssignment(AssignmentTree t,Void v) {write(t,t.getVariable(),t.getExpression(),false);return super.visitAssignment(t,v);}
    public Void visitCompoundAssignment(CompoundAssignmentTree t,Void v) {write(t,t.getVariable(),t.getExpression(),true);return super.visitCompoundAssignment(t,v);}
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
     if(args.length>3&&args[3].equals("post")&&t.getMethodSelect() instanceof MemberSelectTree&&t.getArguments().isEmpty()) {
      MemberSelectTree m=(MemberSelectTree)t.getMethodSelect(); Element selected=trees.getElement(getCurrentPath()); TreePath p=new TreePath(getCurrentPath(),m); TypeMirror receiver=trees.getTypeMirror(new TreePath(p,m.getExpression()));
      for(Rule r:rules) if(m.getIdentifier().contentEquals(r.getter)&&selected!=null&&selected.getKind()==ElementKind.METHOD&&(selected.getEnclosingElement().toString().equals(r.owner)||transparent(selected.getEnclosingElement().toString(),r))&&eligible(receiver,r)) {
       long end=positions.getEndPosition(unit,m); emit(end-r.getter.length(),end+2,r,receiver);
      }
     } return super.visitMethodInvocation(t,v);
    }
   }.scan(unit,null);
  }
  System.err.println("Attribution diagnostics (unfinished module, not build success): "+diagnostics.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count()); fm.close();
 }
}
