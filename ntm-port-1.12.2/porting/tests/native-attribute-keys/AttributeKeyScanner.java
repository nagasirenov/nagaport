import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import javax.lang.model.element.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
/** Select only native class-qualified three-attribute name reads, never value/registration consumers. */
public class AttributeKeyScanner {
 static final String OWNER="net.minecraft.entity.SharedMonsterAttributes",ATTRIBUTE="net.minecraft.entity.ai.attributes.IAttribute";
 static final Map<String,String> FIELDS=new LinkedHashMap<>();static{FIELDS.put("maxHealth","MAX_HEALTH");FIELDS.put("movementSpeed","MOVEMENT_SPEED");FIELDS.put("attackDamage","ATTACK_DAMAGE");}
 public static void main(String[] args)throws Exception {
  JavaCompiler c=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject> errors=new DiagnosticCollector<>();StandardJavaFileManager fm=c.getStandardFileManager(errors,null,null);
  JavacTask task=(JavacTask)c.getTask(null,fm,errors,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(args[1]))).trim(),"-Xmaxerrs","100000"),null,fm.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(args[0]))));
  List<CompilationUnitTree> units=new ArrayList<>();for(CompilationUnitTree u:task.parse())units.add(u);for(Diagnostic<?> e:errors.getDiagnostics())if(e.getKind()==Diagnostic.Kind.ERROR)throw new AssertionError("parse failure: "+e);task.analyze();Trees trees=Trees.instance(task);SourcePositions positions=trees.getSourcePositions();boolean after=args.length>2&&args[2].equals("after");
  TypeElement actual=task.getElements().getTypeElement(OWNER);if(actual==null)throw new AssertionError("missing native class");
  for(String nativeName:FIELDS.values()){boolean found=false;for(Element f:actual.getEnclosedElements())if(f.getKind()==ElementKind.FIELD&&f.getSimpleName().contentEquals(nativeName)){found=f.asType().toString().equals(ATTRIBUTE)&&f.getModifiers().containsAll(Arrays.asList(Modifier.PUBLIC,Modifier.STATIC,Modifier.FINAL));}if(!found)throw new AssertionError("wrong native constant: "+nativeName);}
  for(final CompilationUnitTree u:units){String file=Paths.get(u.getSourceFile().toUri()).toString();String rel=file.substring(file.indexOf("src/main/java/"));String src=u.getSourceFile().getCharContent(true).toString();
   new TreePathScanner<Void,Void>(){
    void emit(Tree node,String old,String replacement,String kind,String field){long end=positions.getEndPosition(u,node),start=end-old.length();if(!src.substring((int)start,(int)end).equals(old))throw new AssertionError("escaped or unexpected token");System.out.println(rel+"\t"+start+"\t"+end+"\t"+old+"\t"+replacement+"\t"+kind+"\t"+field+"\t"+u.getLineMap().getLineNumber(start));}
    public Void visitImport(ImportTree node,Void p){return null;}
    public Void visitMethodInvocation(MethodInvocationTree node,Void p){
     if(!node.getArguments().isEmpty()||!(node.getMethodSelect() instanceof MemberSelectTree))return super.visitMethodInvocation(node,p);
     MemberSelectTree method=(MemberSelectTree)node.getMethodSelect();if(!method.getIdentifier().contentEquals(after?"getName":"getAttributeUnlocalizedName")||!(method.getExpression() instanceof MemberSelectTree))return super.visitMethodInvocation(node,p);
     MemberSelectTree field=(MemberSelectTree)method.getExpression();String name=field.getIdentifier().toString();String mapped=after?name:FIELDS.get(name);if(mapped==null||!FIELDS.containsValue(mapped))return super.visitMethodInvocation(node,p);
     TreePath mp=new TreePath(getCurrentPath(),method),fp=new TreePath(mp,field),qp=new TreePath(fp,field.getExpression());Element qualifier=trees.getElement(qp);
     if(!(qualifier instanceof TypeElement)||qualifier.getKind()!=ElementKind.CLASS||!((TypeElement)qualifier).getQualifiedName().contentEquals(OWNER))return super.visitMethodInvocation(node,p);
     if(after){Element destination=trees.getElement(fp),getter=trees.getElement(mp);if(destination==null||destination.getKind()!=ElementKind.FIELD||!destination.getEnclosingElement().toString().equals(OWNER)||!destination.getSimpleName().contentEquals(mapped))throw new AssertionError("wrong native field: "+destination);if(!(getter instanceof ExecutableElement)||!getter.getEnclosingElement().toString().equals(ATTRIBUTE)||!getter.getSimpleName().contentEquals("getName")||!((ExecutableElement)getter).getParameters().isEmpty()||!((ExecutableElement)getter).getReturnType().toString().equals("java.lang.String"))throw new AssertionError("wrong native getter: "+getter);}
     emit(field,name,mapped,"FIELD",mapped);emit(method,after?"getName":"getAttributeUnlocalizedName","getName","NAME_GETTER",mapped);return super.visitMethodInvocation(node,p);
    }
   }.scan(u,null);
  }
  if(args.length>3){List<String> rows=new ArrayList<>();for(Diagnostic<? extends JavaFileObject> error:errors.getDiagnostics())if(error.getKind()==Diagnostic.Kind.ERROR){String file=error.getSource()==null?"?":Paths.get(error.getSource().toUri()).toString();if(file.contains("src/main/java/"))file=file.substring(file.indexOf("src/main/java/"));rows.add(file+":"+error.getLineNumber()+" "+error.getMessage(java.util.Locale.ROOT).replace("\n"," | "));}Files.write(Paths.get(args[3]),rows);}
  System.err.println("Attribution diagnostics (unfinished source, NOT full-mod compilation success): "+errors.getDiagnostics().stream().filter(e->e.getKind()==Diagnostic.Kind.ERROR).count());fm.close();
 }
}
