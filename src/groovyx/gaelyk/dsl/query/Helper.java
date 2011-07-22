package groovyx.gaelyk.dsl.query;

import java.lang.reflect.Field;

/**
 *
 * @author V. Shyshkin
 */
public class Helper {
    
    public static final String NAME_SUFFIX = "__123";    
    public static final int MAX_STATEMENT_COUNT = 6;
    public static final int MIN_STATEMENT_COUNT = 2;
    
    public static boolean isSupported(String methodName) {
        boolean result = false;
        if (methodName.equals("select")  || methodName.equals("Select") || 
            methodName.equals("cursor")  || methodName.equals("Cursor") ||    
            methodName.equals("from")    || methodName.equals("From") ||
            methodName.equals("where")   || methodName.equals("Where") ||  
            methodName.equals("orderBy") || methodName.equals("OrderBy") ||
            methodName.equals("options") || methodName.equals("Options") ||
            methodName.equals("fetchOptions") || methodName.equals("FetchOptions")  ) {
            result = true;
        }
        return result;
    }
    public static boolean hasField(Class bean, String fieldName) {
        Field[] fields = bean.getDeclaredFields();
        for ( Field field : fields) {
            if ( field.getName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }
}
