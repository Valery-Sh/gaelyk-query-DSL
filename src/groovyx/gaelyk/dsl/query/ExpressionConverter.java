package groovyx.gaelyk.dsl.query;

import org.codehaus.groovy.ast.expr.Expression;

/**
 * The classes that implement this interface may convert 
 * <code>Groovy AST Expressions</code> to string representation.
 * 
 * @see DefaultExpressionConverter
 * @author V. Shyshkin
 */
public interface ExpressionConverter {
    String convert(Expression expression);
}
