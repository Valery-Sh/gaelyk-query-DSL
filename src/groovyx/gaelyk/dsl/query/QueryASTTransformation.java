package groovyx.gaelyk.dsl.query;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.transform.*;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;

import java.util.List;

/**
 * @author V. Shyshkin
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
//@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class QueryASTTransformation implements ASTTransformation {

    @Override
    public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        ModuleNode moduleNode = sourceUnit.getAST();

        List<ClassNode> classes = null;

/*        if (moduleNode == null) {
            return;
        }
*/
        classes = moduleNode.getClasses();

        QueryTransformer queryTransformer = new QueryTransformer(sourceUnit);
        //ClosureVisitorSupport visitorSupport = new ClosureVisitorSupport();
        MethodCallVisitorSupport visitorSupport = new MethodCallVisitorSupport();
        /*
         * visitorSupport notifies queryTransformer when finds a
         * closure which it considers as "a query DSL"
         */
        visitorSupport.addVisitClosureHandler(queryTransformer);

        for (ClassNode classNode : classes) {
            /* ========================================================
             * Walk trough all fields, definded in the class classNode.
             * They may contain init expressions with a method calls
             *=========================================================*/
            List<FieldNode> fields = classNode.getFields();
            for (FieldNode fieldNode : fields) {
                Expression initExpr = fieldNode.getInitialExpression();
                if (initExpr != null) {
                    initExpr.visit(visitorSupport);
                }
            }//for
            /* ========================================================
             * Walk trough all methods, definded in the class classNode
             *=========================================================*/
            List<MethodNode> methods = classNode.getMethods();
            if (methods == null) {
                continue;
            }
            for (MethodNode method : methods) {
                if (method.getName().equals("main")) {
                    continue;
                }
                Statement methodStatement = method.getCode();
                
                if (methodStatement == null || !(methodStatement instanceof BlockStatement)) {
                    continue;
                }
                //((BlockStatement)statement).
                methodStatement.visit(visitorSupport);
            }
            
        }
        queryTransformer.transform();

    }//visit

}//class
