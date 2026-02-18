package utils;

import io.restassured.specification.RequestSpecification;
import java.util.Map;

public class RequestUtil {
    public static void applyKeyValuePairs(RequestSpecification request, String keyValuePairs, boolean asHeaders) {
        if (keyValuePairs == null || keyValuePairs.trim().isEmpty()) return;
        
        String[] pairs = keyValuePairs.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                String key = kv[0].trim();
                String value = kv[1].trim();
                if (asHeaders) {
                    request.header(key, value);
                } else {
                    request.queryParam(key, value);
                }
            }
        }
    }
}
