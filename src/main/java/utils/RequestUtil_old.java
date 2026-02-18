package utils;

import io.restassured.specification.RequestSpecification;

public class RequestUtil_old {

    public static void applyKeyValuePairs(RequestSpecification request, String data, boolean isHeader) {
        if (data == null || data.trim().isEmpty()) return;

        String[] pairs = data.split(";");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2); 
            if (kv.length == 2) {
                if (isHeader) {
                    request.header(kv[0].trim(), kv[1].trim());
                } else {
                    request.queryParam(kv[0].trim(), kv[1].trim());
                }
            }
        }
    }
}