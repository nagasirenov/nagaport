import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Attributed native class-owner scanner with conservative shadow rejection; never text substitution. */
public final class PotionAccessScanner {
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
    if(name.equals("net.minecraft.potion.Potion"))imports.put("Potion",name);
    if(name.equals("net.minecraft.potion.Potion"))imports.put("Potion",name);
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
     if((expression.equals("net.minecraft.potion.Potion")||expression.equals("net.minecraft.potion.Potion"))&&!shadows.contains("net"))owner=expression;
     Element receiver=trees.getElement(new TreePath(getCurrentPath(),t.getExpression()));
     if(!(receiver instanceof TypeElement)||!((TypeElement)receiver).getQualifiedName().contentEquals(owner==null?"":owner))owner=null;
     String mapped=owner==null?null:changes.get(owner+"."+name);
     if(mapped!=null&&!mapped.equals(name)) {
      Tree target=t; TreePath tp=getCurrentPath(); String mode="object";
      TreePath parent=tp.getParentPath();
      if(parent!=null && parent.getLeaf() instanceof MemberSelectTree) {
       MemberSelectTree up=(MemberSelectTree)parent.getLeaf();
       if(up.getExpression()==t && up.getIdentifier().contentEquals("id")){target=up;tp=parent;mode="id";}
       else if(up.getExpression()==t && up.getIdentifier().contentEquals("getId") && parent.getParentPath().getLeaf() instanceof MethodInvocationTree) {
        MethodInvocationTree call=(MethodInvocationTree)parent.getParentPath().getLeaf();
        if(call.getArguments().isEmpty()){target=call;tp=parent.getParentPath();mode="id";}
       } else return super.visitMemberSelect(t,v);
      }
      if(mode.equals("id")) {
       boolean allowed=false;Tree child=target;
       for(TreePath up=tp.getParentPath();up!=null;child=up.getLeaf(),up=up.getParentPath()) {
        Tree node=up.getLeaf();
        if(node instanceof NewClassTree){
         NewClassTree nc=(NewClassTree)node;
         Element type=trees.getElement(new TreePath(up,nc.getIdentifier()));
         allowed=type instanceof TypeElement && ((TypeElement)type).getQualifiedName().contentEquals("net.minecraft.potion.PotionEffect") &&  (nc.getArguments().size()==2 || nc.getArguments().size()==3) && nc.getArguments().get(0)==child;
         break;
        }
        if(node instanceof MethodInvocationTree){
         MethodInvocationTree call=(MethodInvocationTree)node;
         if(call.getArguments().size()==1 && call.getArguments().get(0)==child && call.getMethodSelect() instanceof MemberSelectTree){
          MemberSelectTree sel=(MemberSelectTree)call.getMethodSelect();String method=sel.getIdentifier().toString();
          javax.lang.model.type.TypeMirror receiverType=trees.getTypeMirror(new TreePath(new TreePath(up,sel),sel.getExpression()));
          TypeElement living=task.getElements().getTypeElement("net.minecraft.entity.EntityLivingBase");
          allowed=(method.equals("isPotionActive")||method.equals("removePotionEffect")||method.equals("getActivePotionEffect")) && receiverType!=null && task.getTypes().isSubtype(receiverType,living.asType());
         }
         break;
        }
        if(!(node instanceof ConditionalExpressionTree || node instanceof ParenthesizedTree))break;
       }
       if(!allowed)return super.visitMemberSelect(t,v);
      }
      long start=positions.getStartPosition(unit,target),end=positions.getEndPosition(unit,target);
      String old=source.substring((int)start,(int)end);
      if(old.matches("(?:net\\.minecraft\\.potion\\.)?Potion\\.[a-zA-Z0-9_]+(?:\\.id|\\.getId\\(\\))?"))
       System.out.println(Paths.get(unit.getSourceFile().toUri())+"\t"+start+"\t"+end+"\t"+owner+"\t"+old+"\t"+"net.minecraft.init.MobEffects."+mapped+"\t"+mode);
     }
     return super.visitMemberSelect(t,v);
    }
   }.scan(unit,null);
  }
  long n=errors.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count();System.err.println("Attribution diagnostics (not a successful compile): "+n);
  fm.close();
 }
}
