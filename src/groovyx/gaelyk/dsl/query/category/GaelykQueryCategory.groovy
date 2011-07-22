package groovyx.gaelyk.dsl.query.category

import com.google.appengine.api.datastore.DatastoreService

/**
 *
 * @author V.Shyshkin
 */
class GaelykQueryCategory {
    static Object query(DatastoreService service, Closure closure) {    
        closure(service)
    }
    static Closure defineQuery(DatastoreService service, Closure closure) {    
        closure
    }
    /**
     * Not yet implemented
     */
    static String transformedText(DatastoreService service, Closure closure) {    
        ""
    }

}
