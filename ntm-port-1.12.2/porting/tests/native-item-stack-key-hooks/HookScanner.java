import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;

/** Complete graph attribution; diagnostics are observations, not build acceptance. */
public final class HookScanner {
 static final String[] ROOTS={"tool.ItemModMinecart","block.ItemBlockNamedMeta","block.ItemBlockColoredConcrete","weapon.ItemAmmoArty","weapon.ItemAmmoHIMARS","special.ItemMineralOre","special.ItemAutogen","machine.ItemMetaUpgrade","machine.ItemGear","machine.ItemStampBook","food.ItemCrayon"};
 public static void main(String[] args) throws Exception {
  JavaCompiler compiler=ToolProvider.getSystemJavaCompiler(); DiagnosticCollector<JavaFileObject> ds=new DiagnosticCollector<>();
  StandardJavaFileManager files=compiler.getStandardFileManager(ds,null,null);
  JavacTask task=(JavacTask)compiler.getTask(null,files,ds,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(args[1]))).trim(),"-Xmaxerrs","100000"),null,files.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
  Trees trees=Trees.instance(task); List<CompilationUnitTree> units=new ArrayList<>(); for(CompilationUnitTree u:task.parse())units.add(u); task.analyze();
  TypeElement nativeItem=task.getElements().getTypeElement("net.minecraft.item.Item");
  ExecutableElement base=null; for(Element e:nativeItem.getEnclosedElements()) if(e instanceof ExecutableElement && e.getSimpleName().contentEquals("getTranslationKey") && ((ExecutableElement)e).getParameters().size()==1)base=(ExecutableElement)e;
  final ExecutableElement contract=base;
  for(CompilationUnitTree unit:units)new TreePathScanner<Void,Void>(){
   Element element(Tree t){return trees.getElement(TreePath.getPath(unit,t));}
   boolean family(TypeMirror t){if(t==null||t.getKind()!=TypeKind.DECLARED)return false; for(String r:ROOTS)if(task.getTypes().isSubtype(t,task.getElements().getTypeElement("com.hbm.items."+r).asType()))return true;return false;}
   String file(){String p=unit.getSourceFile().toUri().getPath();return p.substring(p.indexOf("/src/main/java/")+1);}
   String source(Tree t){try{return unit.getSourceFile().getCharContent(true).subSequence((int)trees.getSourcePositions().getStartPosition(unit,t),(int)trees.getSourcePositions().getEndPosition(unit,t)).toString().replace("\r","\\r").replace("\n","\\n").replace("\t","\\t");}catch(Exception e){throw new RuntimeException(e);}}
   String exec(Tree t){Element e=element(t);return e instanceof ExecutableElement?e.getEnclosingElement()+"#"+e+":"+((ExecutableElement)e).getReturnType():"UNRESOLVED";}
   public Void visitClass(ClassTree n,Void u){Element e=element(n);if(e instanceof TypeElement&&family(e.asType())){
    TypeElement c=(TypeElement)e; List<String> chain=new ArrayList<>();TypeMirror p=c.getSuperclass();while(p.getKind()==TypeKind.DECLARED){chain.add(p.toString());p=((TypeElement)((DeclaredType)p).asElement()).getSuperclass();}
    System.out.println("CLASS\t"+e+"\t"+String.join(" > ",chain)+"\t"+file());
   }return super.visitClass(n,u);}
   public Void visitMethod(MethodTree n,Void u){Element e=element(n);if(e instanceof ExecutableElement){ExecutableElement m=(ExecutableElement)e;String name=n.getName().toString();if((name.equals("getUnlocalizedName")||name.equals("getTranslationKey"))&&m.getParameters().size()==1){boolean fam=family(e.getEnclosingElement().asType());System.out.println("DECL\t"+fam+"\t"+exec(n)+"\t"+task.getElements().overrides(m,contract,(TypeElement)e.getEnclosingElement())+"\t"+file());}}return super.visitMethod(n,u);}
   public Void visitMethodInvocation(MethodInvocationTree n,Void u){String name=n.getMethodSelect() instanceof MemberSelectTree?((MemberSelectTree)n.getMethodSelect()).getIdentifier().toString():n.getMethodSelect().toString();if(name.equals("getUnlocalizedName")||name.equals("getTranslationKey")){
    Element e=element(n);TreePath p=getCurrentPath();while(p!=null&&!(p.getLeaf() instanceof ClassTree))p=p.getParentPath();Element owner=p==null?null:trees.getElement(p);
    if((e!=null&&family(e.getEnclosingElement().asType()))||(owner!=null&&family(owner.asType()))) System.out.println("CALL\t"+file()+"\t"+unit.getLineMap().getLineNumber(trees.getSourcePositions().getStartPosition(unit,n))+"\t"+exec(n)+"\t"+source(n));
   }return super.visitMethodInvocation(n,u);}
  }.scan(unit,null);
  System.err.println("Sources="+units.size()+"; javac attribution errors="+ds.getDiagnostics().stream().filter(d->d.getKind()==Diagnostic.Kind.ERROR).count()+"; NOT a full build");
 }
}
