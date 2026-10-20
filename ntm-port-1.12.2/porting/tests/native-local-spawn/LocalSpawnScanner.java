import java.nio.file.*;import java.util.*;import javax.tools.*;import javax.lang.model.element.*;import javax.lang.model.type.*;import com.sun.source.tree.*;import com.sun.source.util.*;
/** Single fresh local identity; only primitive native motion/position preparation; no escape/reassignment/loop reuse. */
public final class LocalSpawnScanner {
 static List<Tree> execution(TreePath p){List<Tree> r=new ArrayList<>();for(;p!=null;p=p.getParentPath()){Tree t=p.getLeaf();if(t instanceof MethodTree||t instanceof LambdaExpressionTree||t instanceof ForLoopTree||t instanceof EnhancedForLoopTree||t instanceof WhileLoopTree||t instanceof DoWhileLoopTree||t instanceof ClassTree)r.add(t);}return r;}
 public static void main(String[] a)throws Exception{
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> errors=new DiagnosticCollector<>();StandardJavaFileManager fm=compiler.getStandardFileManager(errors,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,errors,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));Trees trees=Trees.instance(task);SourcePositions pos=trees.getSourcePositions();List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);for(Diagnostic<?> e:errors.getDiagnostics())if(e.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError(e);task.analyze();String name=a.length>2?"spawnEntity":"spawnEntityInWorld";
  Set<String> fields=new HashSet<>(Arrays.asList("motionX","motionY","motionZ","posX","posY","posZ","rotationYaw","rotationPitch","velocityChanged"));
  for(CompilationUnitTree u:units){Map<Element,TreePath> vars=new HashMap<>();Map<Element,List<TreePath>> uses=new HashMap<>();List<TreePath> calls=new ArrayList<>();
   new TreePathScanner<Void,Void>(){
    public Void visitVariable(VariableTree t,Void v){Element e=trees.getElement(getCurrentPath());if(e!=null&&e.getKind()==ElementKind.LOCAL_VARIABLE&&t.getInitializer() instanceof NewClassTree){NewClassTree n=(NewClassTree)t.getInitializer();Element c=trees.getElement(new TreePath(getCurrentPath(),n));if(n.getClassBody()==null&&c instanceof ExecutableElement&&c.getEnclosingElement().toString().equals("net.minecraft.entity.item.EntityItem")&&c.toString().equals("EntityItem(net.minecraft.world.World,double,double,double,net.minecraft.item.ItemStack)"))vars.put(e,getCurrentPath());}return super.visitVariable(t,v);}
    public Void visitIdentifier(IdentifierTree t,Void v){Element e=trees.getElement(getCurrentPath());if(e!=null)uses.computeIfAbsent(e,k->new ArrayList<>()).add(getCurrentPath());return super.visitIdentifier(t,v);}
    public Void visitMethodInvocation(MethodInvocationTree t,Void v){if(t.getMethodSelect() instanceof MemberSelectTree&&((MemberSelectTree)t.getMethodSelect()).getIdentifier().contentEquals(name)&&t.getArguments().size()==1&&t.getArguments().get(0) instanceof IdentifierTree)calls.add(getCurrentPath());return super.visitMethodInvocation(t,v);}
   }.scan(u,null);
   for(TreePath call:calls){MethodInvocationTree t=(MethodInvocationTree)call.getLeaf();TreePath arg=new TreePath(call,t.getArguments().get(0));Element e=trees.getElement(arg);TreePath decl=vars.get(e);if(decl==null||!execution(decl).equals(execution(call)))continue;MemberSelectTree m=(MemberSelectTree)t.getMethodSelect();TreePath mp=new TreePath(call,m),rp=new TreePath(mp,m.getExpression());TypeMirror r=trees.getTypeMirror(rp);boolean ok=r!=null&&r.getKind()==TypeKind.DECLARED&&r.toString().equals("net.minecraft.world.World")&&trees.getElement(rp)instanceof VariableElement;
    if(a.length>2){Element selected=trees.getElement(mp);ok &= selected instanceof ExecutableElement&&selected.getEnclosingElement().toString().equals("net.minecraft.world.World");}
    long d=pos.getStartPosition(u,decl.getLeaf()),start=pos.getStartPosition(u,t);ok &= d<start;
    for(TreePath use:uses.getOrDefault(e,Collections.emptyList())){
     if(use.getLeaf()==arg.getLeaf())continue;
     if(!execution(use).equals(execution(decl))){ok=false;break;}
     TreePath parent=use.getParentPath();if(!(parent.getLeaf()instanceof MemberSelectTree)){ok=false;break;}
     MemberSelectTree member=(MemberSelectTree)parent.getLeaf();Element field=trees.getElement(parent);
     if(member.getExpression()!=use.getLeaf()||!fields.contains(member.getIdentifier().toString())||!(field instanceof VariableElement)||!field.getEnclosingElement().toString().equals("net.minecraft.entity.Entity")){ok=false;break;}
    }
    if(ok){long end=pos.getEndPosition(u,m);System.out.println(Paths.get(u.getSourceFile().toUri())+"\t"+(end-name.length())+"\t"+end+"\t"+pos.getStartPosition(u,m.getExpression())+"\t"+pos.getEndPosition(u,m.getExpression())+"\t"+d+"\t"+pos.getEndPosition(u,t)+"\t"+e+"\t"+m.getExpression());}
   }
  }
  System.err.println("Attribution diagnostics, not full compile: "+errors.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
