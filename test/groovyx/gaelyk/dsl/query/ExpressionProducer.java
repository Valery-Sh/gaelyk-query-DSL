package groovyx.gaelyk.dsl.query;


import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.CompilePhase;

/**
 *
 * @author V.Shyshkin
 */
public class ExpressionProducer {
    public static Expression create(String source) {
        AstBuilder b = new AstBuilder();

        List<ASTNode> astNodes = b.buildFromString(CompilePhase.CONVERSION, true, source);
        
        BlockStatement block = (BlockStatement)astNodes.get(0);
        return ((ExpressionStatement)block.getStatements().get(0)).getExpression(); 
    }
    public static Expression createArgumentList(String source) {
        AstBuilder b = new AstBuilder();
        String call = "m(" + source + ")";
        List<ASTNode> astNodes = b.buildFromString(CompilePhase.CONVERSION, true,call );
        
        BlockStatement block = (BlockStatement)astNodes.get(0);
        MethodCallExpression mce = (MethodCallExpression)((ExpressionStatement)block.getStatements().get(0)).getExpression(); 
        ArgumentListExpression args = (ArgumentListExpression) mce.getArguments();
        return args;
    }
    
}
