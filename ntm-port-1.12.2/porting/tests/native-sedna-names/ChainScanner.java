import java.nio.file.*;import java.util.*;import javax.tools.*;import javax.lang.model.element.*;import javax.lang.model.type.*;import com.sun.source.tree.*;import com.sun.source.util.*;
public class ChainScanner {
 static final String OWNER="com.hbm.items.weapon.sedna.ItemGunBaseNT";
 public static void main(String[] a)throws Exception {
 JavaCompiler c=ToolProvider.getSystemJavaCompiler();DiagnosticCollector<JavaFileObject>d=new DiagnosticCollector<>();StandardJavaFileManager f=c.getStandardFileManager(d,null,null);JavacTask t=(JavacTask)c.getTask(null,f,d,Arrays.asList("-proc:none","-source","8","-classpath",new String(Files.readAllBytes(Paths.get(a[1]))).trim(),"-Xmaxerrs","100000"),null,f.getJavaFileObjectsFromStrings(Files.readAllLines(Paths.get(a[0]))));Trees tr=Trees.instance(t);List<CompilationUnitTree>us=new ArrayList<>();for(CompilationUnitTree u:t.parse())us.add(u);t.analyze();
 for(CompilationUnitTree u:us)new TreePathScanner<Void,Void>() {
 String text(Tree n){long s=tr.getSourcePositions().getStartPosition(u,n),e=tr.getSourcePositions().getEndPosition(u,n);try{if(s<0||e<s)return "SYNTHETIC:"+n.toString().replace("\n","\\n");return u.getSourceFile().getCharContent(true).subSequence((int)s,(int)e).toString().replace("\n","\\n").replace("\r","\\r").replace("\t","\\t");}catch(Exception ex){throw new RuntimeException(ex);}}
 TreePath path(Tree n){return TreePath.getPath(u,n);} TypeMirror type(Tree n){return tr.getTypeMirror(path(n));} Element el(Tree n){return tr.getElement(path(n));}
 String exe(Tree n){Element e=el(n);return e instanceof ExecutableElement?e.getEnclosingElement()+"#"+e.toString()+":"+((ExecutableElement)e).getReturnType():"UNRESOLVED";}
 boolean typed(Tree n,String s){TypeMirror ty=type(n);return ty!=null&&ty.getKind()==TypeKind.DECLARED&&ty.toString().equals(s);}
 public Void visitMethodInvocation(MethodInvocationTree n,Void v){
 if(!(n.getMethodSelect() instanceof MemberSelectTree))return super.visitMethodInvocation(n,v);MemberSelectTree sel=(MemberSelectTree)n.getMethodSelect();String name=sel.getIdentifier().toString();if(!name.equals("setUnlocalizedName")&&!name.equals("setTranslationKey"))return super.visitMethodInvocation(n,v);
 ExpressionTree r=sel.getExpression();
 ExpressionTree cursor=r; boolean ok=true; int builders=0;
 while(cursor instanceof MethodInvocationTree){
 MethodInvocationTree call=(MethodInvocationTree)cursor;
 if(!(call.getMethodSelect() instanceof MemberSelectTree)){ok=false;break;}
 MemberSelectTree step=(MemberSelectTree)call.getMethodSelect();String sn=step.getIdentifier().toString();
 Element ce=el(call);boolean allowed=sn.equals("setDefaultAmmo")||sn.equals("setDefaultAmmoExpensive")||sn.equals("setNameMutator");
 ok &= allowed && ce instanceof ExecutableElement && ce.getEnclosingElement().toString().equals(OWNER) && typed(call,OWNER);
 cursor=step.getExpression();builders++;
 }
 if(builders==0||!r.toString().contains("ItemGunBaseNT"))return super.visitMethodInvocation(n,v);
 NewClassTree fresh=cursor instanceof NewClassTree?(NewClassTree)cursor:null;
 ok &= fresh!=null && fresh.getClassBody()==null && typed(fresh,OWNER) && exe(fresh).equals(OWNER+"#ItemGunBaseNT(com.hbm.items.weapon.sedna.ItemGunBaseNT.WeaponQuality,com.hbm.items.weapon.sedna.GunConfig...):void") && n.getArguments().size()==1 && (typed(n.getArguments().get(0),"java.lang.String")||type(n.getArguments().get(0)).getKind()==TypeKind.NULL);
 TypeElement own=t.getElements().getTypeElement(OWNER);for(Element m:t.getElements().getAllMembers(own))if(m instanceof ExecutableElement&&m.getSimpleName().contentEquals("setUnlocalizedName")&&((ExecutableElement)m).getParameters().size()==1)ok=false;
 if(name.equals("setTranslationKey"))ok &= exe(n).equals("net.minecraft.item.Item#setTranslationKey(java.lang.String):net.minecraft.item.Item");
 long end=tr.getSourcePositions().getEndPosition(u,sel);System.out.println((ok?"YES":"NO")+"\t"+Paths.get(u.getSourceFile().toUri()).getFileName()+"\t"+(end-name.length())+"\t"+end+"\t"+name+"\t"+exe(n)+"\t"+(fresh==null?"NONE":exe(fresh))+"\t"+exe(r)+"\t"+text(r)+"\t"+text(n.getArguments().get(0)));
 if(ok){new TreeScanner<Void,Void>(){public Void visitMethodInvocation(MethodInvocationTree x,Void q){System.out.println("EXPR\t"+text(x)+"\t"+exe(x));return super.visitMethodInvocation(x,q);}public Void visitNewClass(NewClassTree x,Void q){System.out.println("NEW\t"+text(x)+"\t"+exe(x));return super.visitNewClass(x,q);}}.scan(r,null);}
 return super.visitMethodInvocation(n,v);}
 public Void visitMethod(MethodTree n,Void v){Element e=el(n);if(e!=null&&e.getEnclosingElement().toString().equals(OWNER)&&(n.getName().contentEquals("setDefaultAmmo")||n.getName().contentEquals("setDefaultAmmoExpensive")||n.getName().contentEquals("setNameMutator")||n.getName().contentEquals("<init>"))){System.out.println("CLOSURE\t"+exe(n)+"\t"+text(n));new TreeScanner<Void,Void>(){public Void visitMethodInvocation(MethodInvocationTree x,Void q){System.out.println("CLOSURECALL\t"+text(x)+"\t"+exe(x));return super.visitMethodInvocation(x,q);}}.scan(n,null);}return super.visitMethod(n,v);}
 }.scan(u,null);System.err.println("Full AST diagnostics (not build): "+d.getDiagnostics().stream().filter(x->x.getKind()==Diagnostic.Kind.ERROR).count()); }
}
