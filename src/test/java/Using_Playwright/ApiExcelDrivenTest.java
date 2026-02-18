//package Using_Playwright;
//
//
//import com.microsoft.playwright.*;
//import com.microsoft.playwright.options.RequestOptions;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.aventstack.extentreports.ExtentTest;
//import com.aventstack.extentreports.markuputils.MarkupHelper;
//import com.aventstack.extentreports.markuputils.CodeLanguage;
//import dataprovider.ApiDataProvider;
//import tests.ExtentManager;
//import tests.ExtentTestListener;
//
//import org.testng.Assert;
//import org.testng.annotations.*;
//
//import java.util.Map;
//
//public class ApiExcelDrivenTest {
//
//    private static Playwright playwright;
//    private static APIRequestContext apiContext;
//    private ObjectMapper mapper = new ObjectMapper();
//
//    @BeforeClass
//    public void setup() {
//        playwright = Playwright.create();
//
//        apiContext = playwright.request().newContext(
//                new APIRequest.NewContextOptions()
//                        .setBaseURL("https://rahulshettyacademy.com/")
//                        .setIgnoreHTTPSErrors(true)
//        );
//    }
//
//    @Test(dataProvider = "apiData", dataProviderClass = ApiDataProvider.class)
//    public void apiTest(Map<String, String> data) throws Exception {
//
//        String method = data.get("method");
//        String endpoint = data.get("endpoint");
//        String requestBody = data.get("request_body");
//        int expectedStatus = Integer.parseInt(data.get("expected_status"));
//
//        APIResponse response;
//
//        switch (method.toUpperCase()) {
//            case "POST":
//                response = apiContext.post(endpoint,
//                        RequestOptions.create()
//                                .setHeader("Content-Type", "application/json")
//                                .setData(requestBody)
//                );
//                break;
//
//            case "PUT":
//                response = apiContext.put(endpoint,
//                        RequestOptions.create()
//                                .setHeader("Content-Type", "application/json")
//                                .setData(requestBody)
//                );
//                break;
//
//            case "DELETE":
//                response = apiContext.delete(endpoint);
//                break;
//
//            default:
//                response = apiContext.get(endpoint);
//        }
//
//        int actualStatus = response.status();
//        String responseBody = response.text();
//
//        JsonNode jsonResponse = mapper.readTree(responseBody);
//
//        // ---- Assertions ----
//        Assert.assertEquals(actualStatus, expectedStatus, "Status code mismatch");
//
//        // ---- Reporting (Extent Compatible) ----
////        ExtentTest test = ExtentManager.getTest();
//        
//        ExtentTest test = ExtentTestListener.getTest();
//        test.info("Endpoint: " + endpoint);
//        test.info("Method: " + method);
//        test.info("Response:"+
//                MarkupHelper.createCodeBlock(responseBody, CodeLanguage.JSON)
//        );
//    }
//
//    @AfterClass
//    public void teardown() {
//        apiContext.dispose();
//        playwright.close();
//    }
//}
