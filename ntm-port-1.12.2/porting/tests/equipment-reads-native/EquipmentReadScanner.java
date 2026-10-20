import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Targeted javac ownership and declaration census for two distinct equipment read APIs. */
public final class EquipmentReadScanner {
  static String q(Object value) {
    if(value==null)return "null";
    return "\""+value.toString().replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\"";
  }
  public static void main(String[] args)throws Exception {
    JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
    StandardJavaFileManager fm=compiler.getStandardFileManager(diagnostics,null,null);
    List<String> paths=Files.readAllLines(Paths.get(args[0]));
    JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none","-source","8","-encoding","UTF-8","-classpath",new String(Files.readAllBytes(Paths.get(args[1]))).trim(),"-sourcepath","","-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(paths));
    final Trees trees=Trees.instance(task);final SourcePositions positions=trees.getSourcePositions();
    List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);
    for(Diagnostic<?> d:diagnostics.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("Parse failure: "+d);
    if(args.length<3 || !args[2].equals("--audit-only"))task.analyze(); // Expected unresolved-port diagnostics; not a successful compile, no class emission.
    for(final CompilationUnitTree unit:units) {
      final String source=unit.getSourceFile().getCharContent(true).toString();
      final boolean[] packageShadow={false};
      new TreeScanner<Void,Void>() {
        public Void visitVariable(VariableTree t,Void v){if(t.getName().contentEquals("com"))packageShadow[0]=true;return super.visitVariable(t,v);}
        public Void visitClass(ClassTree t,Void v){if(t.getSimpleName().contentEquals("com"))packageShadow[0]=true;return super.visitClass(t,v);}
        public Void visitTypeParameter(TypeParameterTree t,Void v){if(t.getName().contentEquals("com"))packageShadow[0]=true;return super.visitTypeParameter(t,v);}
      }.scan(unit,null);
      new TreePathScanner<Void,Void>() {
        public Void visitMethod(MethodTree t,Void v) {
          String n=t.getName().toString();
          if(n.equals("getCurrentArmor")||n.equals("armorItemInSlot")||n.equals("getItemStackFromSlot")||n.equals("getEquipmentInSlot")||n.equals("getCurrentItemOrArmor")||n.equals("getLastActiveItems")||n.equals("setCurrentItemOrArmor"))
            System.err.println("Override declaration audit: "+unit.getSourceFile().getName()+" "+t.getName()+" "+t.getParameters());
          return super.visitMethod(t,v);
        }
        public Void visitMethodInvocation(MethodInvocationTree call,Void v) {
          if(call.getMethodSelect() instanceof MemberSelectTree && (((MemberSelectTree)call.getMethodSelect()).getIdentifier().contentEquals("getEquipmentNullable") || ((MemberSelectTree)call.getMethodSelect()).getIdentifier().contentEquals("getArmorNullable"))) {
            String name=((MemberSelectTree)call.getMethodSelect()).getIdentifier().toString();
            Element method=trees.getElement(new TreePath(getCurrentPath(),call.getMethodSelect()));
            boolean resolved=method instanceof ExecutableElement && method.getModifiers().contains(Modifier.STATIC) && method.getEnclosingElement().toString().equals("com.hbm.util.LivingEquipmentRead") && ((ExecutableElement)method).getReturnType().toString().equals("net.minecraft.item.ItemStack") && ((ExecutableElement)method).getParameters().size()==2 && ((ExecutableElement)method).getParameters().get(0).asType().toString().equals(name.equals("getArmorNullable")?"net.minecraft.entity.player.EntityPlayer":"net.minecraft.entity.EntityLivingBase") && ((ExecutableElement)method).getParameters().get(1).asType().toString().equals("int");
            long start=positions.getStartPosition(unit,call),end=positions.getEndPosition(unit,call);
            System.out.println("{\"path\":"+q(Paths.get(unit.getSourceFile().toUri()))+",\"mode\":\"native\",\"accept\":"+resolved+",\"start\":"+start+",\"end\":"+end+"}");
            return super.visitMethodInvocation(call,v);
          }
          if(call.getArguments().size()!=1 || !(call.getMethodSelect() instanceof MemberSelectTree))return super.visitMethodInvocation(call,v);
          MemberSelectTree select=(MemberSelectTree)call.getMethodSelect();
          if(!select.getIdentifier().contentEquals("getEquipmentInSlot") && !select.getIdentifier().contentEquals("getCurrentArmor"))return super.visitMethodInvocation(call,v);
          String methodName=select.getIdentifier().toString();
          TreePath sp=new TreePath(getCurrentPath(),select),rp=new TreePath(sp,select.getExpression());
          TypeMirror type=trees.getTypeMirror(rp); boolean nativeBase=false,shadow=false;
          if(type!=null&&type.getKind()==TypeKind.DECLARED) {
            TypeElement element=(TypeElement)((DeclaredType)type).asElement();
            for(Element member:task.getElements().getAllMembers(element)) {
              if(member instanceof ExecutableElement && (((ExecutableElement)member).getParameters().size()==1 || ((ExecutableElement)member).isVarArgs()) && member.getSimpleName().contentEquals(methodName))shadow=true;
            }
            TypeMirror current=type;
            while(current.getKind()==TypeKind.DECLARED) {
              TypeElement owner=(TypeElement)((DeclaredType)current).asElement();
              if(owner.getQualifiedName().contentEquals(methodName.equals("getCurrentArmor")?"net.minecraft.entity.player.EntityPlayer":"net.minecraft.entity.EntityLivingBase")){nativeBase=true;break;}
              current=owner.getSuperclass();
            }
          }
          Element recv=trees.getElement(rp); if(recv instanceof TypeElement || select.getExpression().toString().equals("super") || select.getExpression().toString().endsWith(".super") || packageShadow[0])shadow=true;
          long start=positions.getStartPosition(unit,call),end=positions.getEndPosition(unit,call),rs=positions.getStartPosition(unit,select.getExpression()),re=positions.getEndPosition(unit,select.getExpression());
          long as=positions.getStartPosition(unit,call.getArguments().get(0)), ae=positions.getEndPosition(unit,call.getArguments().get(0));
          String raw=source.substring((int)start,(int)end);
          if(raw.indexOf('\\')>=0)shadow=true;
          System.out.println("{\"path\":"+q(Paths.get(unit.getSourceFile().toUri()))+",\"accept\":"+(nativeBase&&!shadow)+",\"start\":"+start+",\"end\":"+end+",\"rs\":"+rs+",\"re\":"+re+",\"as\":"+as+",\"ae\":"+ae+",\"method\":"+q(methodName)+",\"type\":"+q(type)+"}");
          return super.visitMethodInvocation(call,v);
        }
      }.scan(unit,null);
    }
    System.err.println("Expected incomplete-port attribution errors: "+diagnostics.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count());
  }
}
