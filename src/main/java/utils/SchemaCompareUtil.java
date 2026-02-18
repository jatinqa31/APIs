package utils;

import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class SchemaCompareUtil {

    public static void compareSchemas(String actualSchema, String expectedSchema) throws Exception {
//    	System.out.println("actualSchema ===" +actualSchema);
//    	System.out.println("expectedSchema ===" +expectedSchema);
        JSONAssert.assertEquals(expectedSchema, actualSchema, JSONCompareMode.LENIENT);
    }  
}
