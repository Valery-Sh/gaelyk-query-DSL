package groovyx.gaelyk.dsl.query;

import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;

/**
 *
 * @author V. Shyshkin
 */
public interface VisitClosureHandler {
    void success(ClosureExpression closure, CastExpression fromKind);
}
