import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Bounded, attributed native EntityLiving write census; not a successful mod build. */
public final class EquipmentWriteScanner {
 static String q(Object o){if(o==null)return "null";return "\""+o.toString().replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\"";}
 static boolean derives(TypeMirror type,String name){
  while(type!=null&&type.getKind()==TypeKind.DECLARED){TypeElement e=(TypeElement)((DeclaredType)type).asElement();if(e.getQualifiedName().contentEquals(name))return true;type=e.getSuperclass();}return false;
 }
 public static void main(String[] args)throws Exception {
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
  StandardJavaFileManager fm=compiler.getStandardFileManager(diagnostics,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none","-source","8","-encoding","UTF-8","-classpath",new String(Files.readAllBytes(Paths.get(args[1]))).trim(),"-sourcepath","","-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
  Trees trees=Trees.instance(task);SourcePositions pos=trees.getSourcePositions();List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
  for(Diagnostic<?> d:diagnostics.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("Parse failure: "+d);
  task.analyze();
  for(CompilationUnitTree u:units){String source=u.getSourceFile().getCharContent(true).toString();boolean[] shadowPackage={false};
   new TreeScanner<Void,Void>(){
    public Void visitVariable(VariableTree t,Void v){if(t.getName().contentEquals("net"))shadowPackage[0]=true;return super.visitVariable(t,v);}
    public Void visitClass(ClassTree t,Void v){if(t.getSimpleName().contentEquals("net"))shadowPackage[0]=true;return super.visitClass(t,v);}
    public Void visitTypeParameter(TypeParameterTree t,Void v){if(t.getName().contentEquals("net"))shadowPackage[0]=true;return super.visitTypeParameter(t,v);}
   }.scan(u,null);
   new TreePathScanner<Void,Void>(){
    public Void visitMethod(MethodTree t,Void v){String n=t.getName().toString();if(Arrays.asList("setCurrentItemOrArmor","setEquipmentDropChance","setItemStackToSlot","setDropChance","playEquipSound").contains(n))System.err.println("DECLARATION "+u.getSourceFile().getName()+" "+n);return super.visitMethod(t,v);}
    public Void visitMethodInvocation(MethodInvocationTree c,Void v){
     if(!(c.getMethodSelect() instanceof MemberSelectTree))return super.visitMethodInvocation(c,v);
     MemberSelectTree s=(MemberSelectTree)c.getMethodSelect();String name=s.getIdentifier().toString();boolean old=name.equals("setCurrentItemOrArmor")||name.equals("setEquipmentDropChance"),nativeCall=name.equals("setItemStackToSlot")||name.equals("setDropChance");
     if(!old&&!nativeCall)return super.visitMethodInvocation(c,v);
     TreePath sp=new TreePath(getCurrentPath(),s),rp=new TreePath(sp,s.getExpression());TypeMirror type=trees.getTypeMirror(rp);Element receiver=trees.getElement(rp),method=trees.getElement(sp);
     boolean accept=derives(type,"net.minecraft.entity.EntityLiving")&&!shadowPackage[0]&&!(receiver instanceof TypeElement)&&!s.getExpression().toString().endsWith("super")&&c.getArguments().size()==2;int slot=-1;
     if(old&&accept){ExpressionTree arg=c.getArguments().get(0);if(arg instanceof LiteralTree&&((LiteralTree)arg).getValue() instanceof Integer)slot=(Integer)((LiteralTree)arg).getValue();accept=slot>=0&&slot<=4;
      for(Element member:task.getElements().getAllMembers((TypeElement)((DeclaredType)type).asElement()))if(member instanceof ExecutableElement&&member.getSimpleName().contentEquals(name))accept=false;
     }
     if(nativeCall&&accept){
      accept=method instanceof ExecutableElement&&!method.getModifiers().contains(Modifier.STATIC)&&method.getEnclosingElement().toString().equals("net.minecraft.entity.EntityLiving");
      if(accept){ExecutableElement m=(ExecutableElement)method;accept=m.getParameters().size()==2&&m.getReturnType().toString().equals("void")&&m.getParameters().get(0).asType().toString().equals("net.minecraft.inventory.EntityEquipmentSlot")&&m.getParameters().get(1).asType().toString().equals(name.equals("setDropChance")?"float":"net.minecraft.item.ItemStack");}
      Element arg=trees.getElement(new TreePath(getCurrentPath(),c.getArguments().get(0)));accept=accept&&arg!=null&&arg.getKind()==ElementKind.ENUM_CONSTANT&&arg.getEnclosingElement().toString().equals("net.minecraft.inventory.EntityEquipmentSlot");
      if(arg!=null)slot=Arrays.asList("MAINHAND","FEET","LEGS","CHEST","HEAD").indexOf(arg.getSimpleName().toString());accept=accept&&slot>=0;
     }
     long start=pos.getStartPosition(u,c),end=pos.getEndPosition(u,c);String raw=source.substring((int)start,(int)end);if(raw.contains("\\"))accept=false;
     StringBuilder out=new StringBuilder("{\"path\":").append(q(Paths.get(u.getSourceFile().toUri()))).append(",\"mode\":").append(q(old?"old":"native")).append(",\"accept\":").append(accept).append(",\"type\":").append(q(type)).append(",\"method\":").append(q(name)).append(",\"owner\":").append(q(method==null?null:method.getEnclosingElement())).append(",\"slot\":").append(slot).append(",\"start\":").append(start).append(",\"end\":").append(end).append(",\"rs\":").append(pos.getStartPosition(u,s.getExpression())).append(",\"re\":").append(pos.getEndPosition(u,s.getExpression()));
     if(c.getArguments().size()==2)out.append(",\"a0s\":").append(pos.getStartPosition(u,c.getArguments().get(0))).append(",\"a0e\":").append(pos.getEndPosition(u,c.getArguments().get(0))).append(",\"a1s\":").append(pos.getStartPosition(u,c.getArguments().get(1))).append(",\"a1e\":").append(pos.getEndPosition(u,c.getArguments().get(1)));
     System.out.println(out.append('}'));return super.visitMethodInvocation(c,v);
    }
   }.scan(u,null);
  }
  System.err.println("Expected incomplete-port attribution diagnostics: "+diagnostics.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
