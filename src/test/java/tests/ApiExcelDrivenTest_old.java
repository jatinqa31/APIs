//package tests;
//
//import com.aventstack.extentreports.ExtentTest;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import dataprovider.ApiDataProvider;
//import io.restassured.RestAssured;
//import io.restassured.response.Response;
//import io.restassured.specification.RequestSpecification;
//import org.testng.Assert;
//import org.testng.annotations.Listeners;
//import org.testng.annotations.Test;
//import org.testng.asserts.SoftAssert;
//
//import utils.*;
//
//import java.util.List;
//import java.util.Map;
//
//@Listeners(ExtentTestListener.class)
//public class ApiExcelDrivenTest_old { 
//	
//	int count=0;
////	String actualSchema = "";
////    String expectedSchema = "";
//
//    @Test(dataProvider = "apiData", dataProviderClass = ApiDataProvider.class)
//    public void executeApiTest(Map<String, String> data) throws Exception {
//    	
//        // -------- Extent Test --------
//        ExtentTest extentTest = ExtentTestListener.getTest();
//
//        // -------- Excel Data --------
//        String testCaseId = data.get("Test Case Id");
//        String method = data.get("Method");
//        String endpoint = data.get("Endpoint");
//        String headers = data.get("Headers");
//        String queryParams = data.get("Query Params");
//        String requestBody = data.get("Request Body");
//        String schemaPath = data.get("ExpectedSchemaPath");
//
//        extentTest.info("TestCaseId: " + testCaseId);
//        extentTest.info("Method: " + method);
//        extentTest.info("Endpoint: " + endpoint);
//
//        // -------- Build Request --------
//        RequestSpecification request = RestAssured
//                .given()
//                .relaxedHTTPSValidation()
//                .header("Accept", "application/json")
//                .header("Content-Type", "application/json");
//
//        RequestUtil_old.applyKeyValuePairs(request, headers, true);
//        RequestUtil_old.applyKeyValuePairs(request, queryParams, false);
//
//        if (requestBody != null && !requestBody.trim().isEmpty()) {
//            request.body(requestBody);
//            extentTest.info("Request Body:")
//                    .info("<pre>" + requestBody + "</pre>");
//        }
//
//        // -------- Execute API --------
//        Response response;
//
//        switch (method.toUpperCase()) {
//            case "GET":
//                response = request.get(endpoint);
//                break;
//            case "POST":
//                response = request.post(endpoint);
//                break;
//            case "PUT":
//                response = request.put(endpoint);
//                break; 
//            case "DELETE":
//                response = request.delete(endpoint);
//                break;
//            default:
//                throw new RuntimeException("Invalid HTTP Method: " + method);
//        } 
//
//        // -------- Log Response --------
//        extentTest.info("Status Code: " + response.getStatusCode());
//        extentTest.info("Response Body:")
//                .info("<pre>" + response.getBody().asPrettyString() + "</pre>");
//
////        // -------- Status Code Validation --------
////        String expectedStatusStr = data.get("Expected Status");
////        int expectedStatus = (expectedStatusStr == null || expectedStatusStr.isEmpty())
////                ? 200
////                : Integer.parseInt(expectedStatusStr);
////
////        Assert.assertEquals(
////                response.getStatusCode(),
////                expectedStatus,
////                "Status code mismatch"
////        );
//
//        // -------- JSON Guard (IMPORTANT) --------
//        String contentType = response.getContentType();
//        Assert.assertTrue(
//                contentType != null && contentType.contains("application/json"),
//                "Non-JSON response received"
//        );
//
//        // -------- Schema Generation -------
//    
//     // -------- Schema Generation --------
//        String responseJson = response.getBody().asString();
//
//        // generate schema from actual response (no POJO)
//        JsonNode runtimeSchema =
//                RuntimeSchemaGenerator_old.generateSchema(responseJson);
//    	  
//        // by me jatin
//    	//  System.out.println("runtimeSchema >>>>>> "+ runtimeSchema);
//        
//        // load expected schema JSON from file path (Excel column ExpectedSchemaPath) 
//        String expectedSchemaRaw = FileUtil_old.readFileAsString(schemaPath);
//        
//        JsonNode expectedSchemaNode = new ObjectMapper().readTree(expectedSchemaRaw);
//
////        System.out.println("expectedSchemaNode "+expectedSchemaNode);
//        
//        // high-level schema diff (structure/types)
//        List<String> diffs =
//                SchemaComparator_old.compare(
//                        expectedSchemaNode,
//                        runtimeSchema
//                );
// 
//        diffs.forEach(diff -> extentTest.info("Schema diff: " + diff));
//
//        // ❌ FAIL test if breaking changes exist
//       SoftAssert sa = new SoftAssert();
//       sa.assertTrue(diffs.isEmpty(),
//                "Schema mismatch detected:\n" + diffs);
// 
//        // keep your old string-based compare & $schema removal if you want
//        String actualSchema = runtimeSchema.toPrettyString();
//        String expectedSchema = expectedSchemaRaw;
//
//        System.out.println("actualSchema "+actualSchema);
//        
//        //Removed SchemaSanitizer by me on 18-Feb-2026
//        
//        // -------- STEP : Schema Validation + Reporting  (remove $schema if needed)
////        String actualSchemaClean =
//           //     SchemaSanitizer.removeSchemaKeyword(actualSchema);
//
////        String expectedSchemaClean =
//           //     SchemaSanitizer.removeSchemaKeyword(expectedSchema);
//
//       // sa.assertAll(); 
//        try {
//            SchemaCompareUtil.compareSchemas(
////                    actualSchemaClean,
////                    expectedSchemaClean
//            );
//
//            extentTest.pass("Schema Validation Passed");
//        } catch (AssertionError e) {
//            extentTest.fail("Schema Validation Failed");
//            extentTest.info("Expected Schema:")
//                    .info("<pre>" + expectedSchemaClean + "</pre>");
//            extentTest.info("Actual Schema:")
//                    .info("<pre>" + actualSchemaClean + "</pre>");
//            throw e;
//        }
//    
//    }
//}