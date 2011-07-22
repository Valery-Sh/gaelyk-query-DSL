package groovyx.gaelyk.dsl.query

import org.junit.Test
import static org.junit.Assert.*
import org.codehaus.groovy.ast.expr.*
/**
 * @author V. Shyshkin
 */
class DefaultExpressionConverterTest {
    @Test
    public void visitMethodCallExpression() {
        print "visitMethodCallExpression()"
        def expr = ExpressionProducer.create("getValue('testing')")
        def support = new DefaultExpressionConverter() 
        def result = support.getResult(expr)
        def expResult = 'this."getValue"("testing")'
        assertEquals expResult,result
        expr = ExpressionProducer.create("obj.getValue(get('testing'))")        
        result = support.getResult(expr)
        expResult = 'this."getValue"("get"("testing"))'
        
        expr = ExpressionProducer.create("obj.getValue(get(123))")        
        result = support.getResult(expr)
        expResult = 'this."getValue"("get"(123))'
        
    }
    @Test
    public void visitPropertyExpression() {
        print "visitPropertyExpression()"
        def expr = ExpressionProducer.create("a.b.c")
        def support = new DefaultExpressionConverter() 
        def result = support.getResult(expr)
        print result
        def expResult = 'a."b"."c"'
        assertEquals expResult,result
        
        expr = ExpressionProducer.create('a."b".c')
        result = support.getResult(expr)
        print result
        expResult = 'a."b"."c"'
        assertEquals expResult,result
    }
    
    @Test
    public void visitConstantExpression() {
        print "visitConstantExpression()"
        def expr = ExpressionProducer.create('"this text"')
        def support = new DefaultExpressionConverter() 
        def result = support.getResult(expr)
        print result
        def expResult = '"this text"'
        assertEquals expResult,result
        
        expr = ExpressionProducer.create("123")
        result = support.getResult(expr)
        print result
        expResult = '123'
        assertEquals expResult,result
    }
    @Test
    public void visitConstructorCallExpression() {
        print "visitConstructorCallExpression()"
        def expr = ExpressionProducer.create("new Person()")
        def support = new DefaultExpressionConverter() 
        def result = support.getResult(expr)
        print result
        def expResult = 'new Person()'
        assertEquals expResult,result
        
        expr = ExpressionProducer.create("new Person('123')")
        result = support.getResult(expr)
        print result
        expResult = 'new Person("123")'
        assertEquals expResult,result
    }
    @Test
    public void visitBinaryExpression() {
        print "visitBinaryExpression()"
        def expr = ExpressionProducer.create("2 + 3")
        def support = new DefaultExpressionConverter() 
        def result = support.getResult(expr)
        print result
        def expResult = '(2+3)'
        assertEquals expResult,result
        
        expr = ExpressionProducer.create("firstName == 'Jone'")
        result = support.getResult(expr)
        print result
        expResult = '(firstName=="Jone")'
        assertEquals expResult,result
        
        expr = ExpressionProducer.create("firstName == getName()")
        result = support.getResult(expr)
        print result
        expResult = '(firstName==this."getName"())'
        assertEquals expResult,result
    }
    
    @Test
    public void visitListExpression() {
        print "visitListExpression()"
        def expr = ExpressionProducer.create("[]")
        def support = new DefaultExpressionConverter() 
        def result = support.getResult(expr)
        print result
        def expResult = '[]'
        assertEquals expResult,result
        
        expr = ExpressionProducer.create("[1, 3, 6]")
        result = support.getResult(expr)
        print result
        expResult = '[1,3,6]'
        assertEquals expResult,result
        
        expr = ExpressionProducer.create('[1,3, ["Black"]]')
        result = support.getResult(expr)
        print result
        expResult = '[1,3,["Black"]]'
        assertEquals expResult,result
    }
    @Test
    public void visitListOfExpressions() {
        print "visitListOfExpression()"
        def expr1 = ExpressionProducer.create("123")
        def expr2 = ExpressionProducer.create('"Black"')        
        List<? extends Expression> list = [expr1, expr2]       
        def support = new DefaultExpressionConverter() 
        def result = support.getResult(list)
        print result
        def expResult = '123,"Black"'
        assertEquals expResult,result
    }
    @Test
    public void visitUnaryPlusExpression() {
        print "visitListOfExpression()"
        def expr = ExpressionProducer.create("+123")
        def support = new DefaultExpressionConverter() 
        def result = support.getResult(expr)
        print result
        def expResult = '123'
        assertEquals expResult,result
    }
    @Test
    public void visitUnaryMinusExpression() {
        print "visitListOfExpression()"
        def expr = ExpressionProducer.create("-123")
        def support = new DefaultExpressionConverter() 
        def result = support.getResult(expr)
        print result
        def expResult = '-123'
        assertEquals expResult,result
    }
    @Test
    public void visitArgumentListExpression() {
        print "visitListOfExpression()"
        def expr = ExpressionProducer.createArgumentList("123,'Black'")
        def support = new DefaultExpressionConverter() 
        def result = support.getResult(expr)
        print result
        def expResult = '(123,"Black")'
        assertEquals expResult,result
    }
    

}

