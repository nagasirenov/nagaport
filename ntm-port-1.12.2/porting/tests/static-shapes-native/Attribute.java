import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Bounded four-owner native attribution plus parse-only whole-source descendant census. */
public class Attribute {
 static final Set<String> OWNERS=new HashSet<>(Arrays.asList("BlockCrop","BlockMush","BlockFallout","MeltedFlesh"));
 public static void main(String[] args)throws Exception{
  Path root=Paths.get(args[0]);String cp=new String(Files.readAllBytes(Paths.get(args[1])),StandardCharsets.UTF_8).trim();
  JavaCompiler c=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> ds=new DiagnosticCollector<>();StandardJavaFileManager fm=c.getStandardFileManager(ds,null,StandardCharsets.UTF_8);
  List<File> all=new ArrayList<>(),selected=new ArrayList<>();try(java.util.stream.Stream<Path> walk=Files.walk(root.resolve("src/main/java"))){walk.filter(x->x.toString().endsWith(".java")).sorted().forEach(x->{all.add(x.toFile());if(OWNERS.contains(x.getFileName().toString().replace(".java","")))selected.add(x.toFile());});}
  JavacTask parse=(JavacTask)c.getTask(null,fm,ds,Arrays.asList("-proc:none","-encoding","UTF-8"),null,fm.getJavaFileObjectsFromFiles(all));int units=0;
  for(CompilationUnitTree u:parse.parse()){units++;new TreeScanner<Void,Void>(){public Void visitClass(ClassTree tree,Void p){if(tree.getExtendsClause()!=null){String parent=tree.getExtendsClause().toString();if(OWNERS.contains(parent)||OWNERS.stream().anyMatch(n->parent.endsWith("."+n)))throw new AssertionError("Unreviewed descendant "+tree.getSimpleName()+" extends "+parent);}return super.visitClass(tree,p);}}.scan(u,null);}
  if(ds.getDiagnostics().stream().anyMatch(x->x.getKind()==Diagnostic.Kind.ERROR))throw new AssertionError("parse failed");System.out.println("PASS whole-source parse-only closure: "+units+" source units, zero descendants of four selected owners");
  DiagnosticCollector<JavaFileObject> errors=new DiagnosticCollector<>();JavacTask task=(JavacTask)c.getTask(null,fm,errors,Arrays.asList("-proc:none","-encoding","UTF-8","-Xmaxerrs","100000","-classpath",cp,"-sourcepath",args[2]),null,fm.getJavaFileObjectsFromFiles(selected));List<CompilationUnitTree> us=new ArrayList<>();for(CompilationUnitTree u:task.parse())us.add(u);task.analyze();Trees trees=Trees.instance(task);Elements elements=task.getElements();Types types=task.getTypes();TypeElement block=elements.getTypeElement("net.minecraft.block.Block");final int[] boxes={0},overrides={0};
  for(CompilationUnitTree u:us)new TreePathScanner<Void,Void>(){
   public Void visitClass(ClassTree tree,Void p){Element e=trees.getElement(getCurrentPath());if(e instanceof TypeElement&&OWNERS.contains(tree.getSimpleName().toString())){TypeElement t=(TypeElement)e;if(!types.isSubtype(t.asType(),block.asType())||t.getSuperclass().getKind()!=TypeKind.DECLARED)throw new AssertionError("unresolved native ancestor");System.out.println("OWNER "+t.getQualifiedName()+" -> "+t.getSuperclass());}return super.visitClass(tree,p);}
   public Void visitNewClass(NewClassTree tree,Void p){Element e=trees.getElement(getCurrentPath());if(tree.getIdentifier().toString().equals("net.minecraft.util.math.AxisAlignedBB")){if(e==null||!e.getEnclosingElement().toString().equals("net.minecraft.util.math.AxisAlignedBB")||tree.getArguments().size()!=6)throw new AssertionError("fake AABB target");boxes[0]++;System.out.println("NATIVE_CONSTRUCTOR "+e.getEnclosingElement()+" "+e);}return super.visitNewClass(tree,p);}
   public Void visitMethod(MethodTree tree,Void p){String n=tree.getName().toString();if(n.equals("getBoundingBox")||n.equals("getCollisionBoundingBox")){ExecutableElement m=(ExecutableElement)trees.getElement(getCurrentPath());TypeElement owner=(TypeElement)m.getEnclosingElement();boolean found=false;for(Element e:block.getEnclosedElements())if(e.getKind()==ElementKind.METHOD&&e.getSimpleName().contentEquals(n)&&elements.overrides(m,(ExecutableElement)e,owner))found=true;if(!found)throw new AssertionError("not native override: "+m);overrides[0]++;System.out.println("NATIVE_OVERRIDE "+owner+" "+m);}return super.visitMethod(tree,p);}
  }.scan(u,null);
  if(boxes[0]!=4||overrides[0]!=5)throw new AssertionError("bad selected count");System.out.println("PASS 4 genuine six-double native AABB constructors; 5 genuine Block overrides; unrelated complete-owner diagnostics remain "+errors.getDiagnostics().stream().filter(x->x.getKind()==Diagnostic.Kind.ERROR).count());
 }
}
