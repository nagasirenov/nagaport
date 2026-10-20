import com.sun.source.tree.*;
import com.sun.source.util.*;
import javax.tools.*;
import java.nio.file.*;
import java.util.*;

/** Parse-only javac AST census. No guessed type attribution against broken collaborators. */
public final class PlacementAst {
  static String q(String s) { return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\""; }
  public static void main(String[] args) throws Exception {
    JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fm=compiler.getStandardFileManager(null,null,null);
    DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
    JavacTask task=(JavacTask)compiler.getTask(null,fm,diagnostics,Arrays.asList("-proc:none"),null,fm.getJavaFileObjects(args));
    Trees trees=Trees.instance(task);
    for (CompilationUnitTree unit:task.parse()) {
      String source=unit.getSourceFile().getCharContent(true).toString();
      SourcePositions positions=trees.getSourcePositions();
      boolean worldImport=unit.getImports().stream().anyMatch(t->t.getQualifiedIdentifier().toString().equals("net.minecraft.world.World"));
      boolean blocksImport=unit.getImports().stream().anyMatch(t->t.getQualifiedIdentifier().toString().equals("net.minecraft.init.Blocks"));
      final Set<String> badNames=new HashSet<>();
      new TreeScanner<Void,Void>() { public Void visitVariable(VariableTree t,Void p) { if(t.getName().contentEquals("Blocks")) badNames.add("Blocks");return super.visitVariable(t,p); } public Void visitClass(ClassTree t,Void p) { if(t.getSimpleName().contentEquals("Blocks")||t.getSimpleName().contentEquals("World")) badNames.add(t.getSimpleName().toString());return super.visitClass(t,p); } }.scan(unit,null);
      new TreePathScanner<Void,Void>() {
        long start(Tree t){return positions.getStartPosition(unit,t);} long end(Tree t){return positions.getEndPosition(unit,t);} String raw(Tree t){return source.substring((int)start(t),(int)end(t));}
        MethodTree method(){for(TreePath p=getCurrentPath();p!=null;p=p.getParentPath())if(p.getLeaf() instanceof MethodTree)return (MethodTree)p.getLeaf();return null;}
        boolean owned(String name) {
          MethodTree m=method();if(!worldImport||badNames.contains("World")||m==null)return false;
          boolean has=false;for(VariableTree p:m.getParameters())if(p.getName().contentEquals(name)&&p.getType().toString().equals("World"))has=true;
          if(!has)return false;
          final int[] bindings={0};final boolean[] mutation={false};
          new TreeScanner<Void,Void>() {
            public Void visitVariable(VariableTree t,Void p){if(t.getName().contentEquals(name))bindings[0]++;return super.visitVariable(t,p);}
            public Void visitAssignment(AssignmentTree t,Void p){if(t.getVariable().toString().equals(name))mutation[0]=true;return super.visitAssignment(t,p);}
            public Void visitCompoundAssignment(CompoundAssignmentTree t,Void p){if(t.getVariable().toString().equals(name))mutation[0]=true;return super.visitCompoundAssignment(t,p);}
          }.scan(m,null);
          for(TreePath p=getCurrentPath().getParentPath();p!=null&&p.getLeaf()!=m;p=p.getParentPath())if(p.getLeaf() instanceof ClassTree||p.getLeaf() instanceof LambdaExpressionTree)return false;
          return bindings[0]==1&&!mutation[0];
        }
        boolean xyzParams() { MethodTree m=method(); if(m==null)return false; for(String n:Arrays.asList("x","y","z")){boolean found=false;for(VariableTree v:m.getParameters())if(v.getName().contentEquals(n)&&v.getType().toString().equals("int"))found=true;if(!found)return false;}return true; }
        boolean pure(Tree t) {
          switch(t.getKind()) {
            case INT_LITERAL: case LONG_LITERAL: case BOOLEAN_LITERAL: return true;
            case IDENTIFIER:return true;
            case MEMBER_SELECT:return pure(((MemberSelectTree)t).getExpression());
            case PARENTHESIZED:return pure(((ParenthesizedTree)t).getExpression());
            case PLUS:case MINUS:case MULTIPLY:case LEFT_SHIFT:case RIGHT_SHIFT:case UNSIGNED_RIGHT_SHIFT:case AND:case OR:case XOR: {BinaryTree b=(BinaryTree)t;return pure(b.getLeftOperand())&&pure(b.getRightOperand());}
            case UNARY_PLUS:case UNARY_MINUS:case BITWISE_COMPLEMENT:case LOGICAL_COMPLEMENT:return pure(((UnaryTree)t).getExpression());
            default:return false;
          }
        }
        public Void visitMethodInvocation(MethodInvocationTree t,Void p) {
          if(t.getMethodSelect() instanceof MemberSelectTree) {
            MemberSelectTree sel=(MemberSelectTree)t.getMethodSelect();String name=sel.getIdentifier().toString();String receiver=raw(sel.getExpression());
            if(Arrays.asList("setBlock","getBlock","getBlockMetadata","getTileEntity","setBlockToAir","isAirBlock","getHeightValue","setBlockMetadataWithNotify").contains(name)) {
              System.out.print("{\"kind\":\"call\",\"file\":"+q(Paths.get(unit.getSourceFile().toUri()).toString())+",\"method\":"+q(method()==null?"":method().getName().toString())+",\"name\":"+q(name)+",\"start\":"+start(t)+",\"end\":"+end(t)+",\"line\":"+unit.getLineMap().getLineNumber(start(t))+",\"receiver\":"+q(receiver)+",\"owned\":"+(sel.getExpression() instanceof IdentifierTree&&owned(receiver))+",\"statement\":"+(getCurrentPath().getParentPath().getLeaf() instanceof ExpressionStatementTree)+",\"xyzParams\":"+xyzParams()+",\"args\":[");
              boolean first=true;for(ExpressionTree a:t.getArguments()){if(!first)System.out.print(",");first=false;System.out.print("{\"text\":"+q(raw(a))+",\"kind\":"+q(a.getKind().toString())+",\"pure\":"+pure(a)+"}");}System.out.println("]}");
            }
          }return super.visitMethodInvocation(t,p);
        }
        public Void visitMemberSelect(MemberSelectTree t,Void p) {
          if(t.getExpression().toString().equals("Blocks")&&blocksImport&&!badNames.contains("Blocks"))System.out.println("{\"kind\":\"constant\",\"file\":"+q(Paths.get(unit.getSourceFile().toUri()).toString())+",\"start\":"+start(t)+",\"end\":"+end(t)+",\"name\":"+q(t.getIdentifier().toString())+"}");
          return super.visitMemberSelect(t,p);
        }
      }.scan(unit,null);
    }
    for(Diagnostic<?> d:diagnostics.getDiagnostics())if(d.getKind()==Diagnostic.Kind.ERROR)throw new IllegalArgumentException(d.toString());
    fm.close();
  }
}
