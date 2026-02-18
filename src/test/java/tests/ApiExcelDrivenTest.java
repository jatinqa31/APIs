package tests;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dataprovider.ApiDataProvider;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.RequestUtil;   // your util
import utils.SchemaCompareUtil;
import utils.FileUtil;     // your util
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Listeners(ExtentTestListener.class) 
public class ApiExcelDrivenTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test(dataProvider = "apiData", dataProviderClass = ApiDataProvider.class)
    public void executeApiTest(Map<String, String> data) throws Exception { 

        String testNameFromExcel = data.get("Request Name");

        // -------- Excel Data --------
        String testCaseId = data.get("Test Case Id");
        String method = data.get("Method");
        String endpoint = data.get("Endpoint");
        String headers = data.get("Headers");
        String queryParams = data.get("Query Params");
        String requestBody = data.get("Request Body");
        String expectedSchemaPath = data.get("ExpectedSchemaPath");
        String expectedStatusStr = data.get("Expected Status");

        ExtentTest extentTest = ExtentTestListener.getTest();
        extentTest.info("TestCaseId: " + testCaseId);
        extentTest.info("Method: " + method);
        extentTest.info("Endpoint: " + endpoint);

        // -------- Build Request --------
        RequestSpecification request = RestAssured
                .given()
                .relaxedHTTPSValidation()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");

        // apply headers / query params
        RequestUtil.applyKeyValuePairs(request, headers, true);
        RequestUtil.applyKeyValuePairs(request, queryParams, false);

        if (requestBody != null && !requestBody.trim().isEmpty()) {
            request.body(requestBody);
            extentTest.info("Request Body:");
            extentTest.info(MarkupHelper.createCodeBlock(requestBody, CodeLanguage.JSON));
        }

        // -------- Execute API --------
        Response response;
        switch (method.toUpperCase()) {
            case "GET":
                response = request.get(endpoint);
                break;
            case "POST":
                response = request.post(endpoint);
                break;
            case "PUT":
                response = request.put(endpoint);
                break;
            case "DELETE":
                response = request.delete(endpoint);
                break;
            default:
                throw new RuntimeException("Invalid HTTP Method: " + method);
        }

        // -------- Log Response --------
        int byteLen = response.getBody().asByteArray().length;
        String rawResponseBody = response.getBody().asString();

        extentTest.info("Status Code: " + response.getStatusCode());
        extentTest.info("Content-Type: " + response.getContentType());
        extentTest.info("Content-Length: " + response.getHeader("Content-Length"));
        extentTest.info("Body Byte Length: " + byteLen);
        extentTest.info("Response Body (raw): '" +
                (rawResponseBody == null || rawResponseBody.trim().isEmpty()
                        ? "(EMPTY)"
                        : rawResponseBody) + "'");
        extentTest.info(MarkupHelper.createCodeBlock(
                rawResponseBody == null ? "" : rawResponseBody,
                CodeLanguage.JSON
        ));

        // -------- Status Code Validation --------
        int expectedStatus = (expectedStatusStr != null && !expectedStatusStr.isEmpty())
                ? Integer.parseInt(expectedStatusStr)
                : 200;
        Assert.assertEquals(response.getStatusCode(), expectedStatus, "Status code mismatch");

        // -------- JSON Guard (Content-Type) --------
        String contentType = response.getContentType();
        Assert.assertTrue(contentType != null && contentType.contains("application/json"),
                "Non-JSON response received");

        // -------- Validate JSON before schema generation --------
        String responseJson = rawResponseBody;
        String trimmedJson = (responseJson == null) ? "" : responseJson.trim();

        if (trimmedJson.isEmpty() || "null".equalsIgnoreCase(trimmedJson)) {
            extentTest.warning("Empty or null response JSON received. Skipping schema generation.");
            extentTest.pass("Status code validated. No response JSON; schema validation skipped.");
            return;
        }

        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(trimmedJson);
            extentTest.info("JSON is valid. Root node type: " + rootNode.getNodeType());
        } catch (Exception e) {
            extentTest.fail("Response body is not valid JSON: " + e.getMessage());
            throw new RuntimeException("JSON parsing failed for response body.", e);
        }

        // -------- Schema Generation & Comparison --------
        extentTest.info("Raw response JSON captured for schema generation.");
        extentTest.info(MarkupHelper.createCodeBlock(responseJson, CodeLanguage.JSON));

        JsonNode actualSchemaNode = generateFixedSchema(rootNode);
        if (actualSchemaNode == null) {
            throw new RuntimeException("generateFixedSchema returned null");
        }

        String actualSchemaPretty = mapper.writeValueAsString(actualSchemaNode);
        extentTest.info("Actual Schema Generated Successfully");
        extentTest.info("Actual Schema (pretty):");
        extentTest.info(MarkupHelper.createCodeBlock(actualSchemaPretty, CodeLanguage.JSON));
        extentTest.info("Actual Schema Node toString: " + actualSchemaNode.toString());

        JsonNode rootTypeNode = actualSchemaNode.get("type");
        if (rootTypeNode == null || rootTypeNode.isNull()) {
            extentTest.warning("Generated schema has type null or missing at root.");
        }

        // -------- Load expected schema --------
        Assert.assertNotNull(expectedSchemaPath, "ExpectedSchemaPath is REQUIRED in Excel");

        String expectedSchemaRaw = FileUtil.readFileAsString(expectedSchemaPath.trim());
        // or direct NIO:
        // String expectedSchemaRaw = new String(
        //     Files.readAllBytes(Paths.get(expectedSchemaPath.trim())),
        //     StandardCharsets.UTF_8
        // );

        Assert.assertFalse(expectedSchemaRaw.isEmpty(),
                "Expected schema file is empty: " + expectedSchemaPath);

        JsonNode expectedSchemaNode = mapper.readTree(expectedSchemaRaw);
        if (expectedSchemaNode == null || expectedSchemaNode.isNull()) {
            throw new RuntimeException("Expected schema JSON is null: " + expectedSchemaPath);
        }

        String expectedSchemaPretty = mapper.writeValueAsString(expectedSchemaNode);

        extentTest.info("Expected Schema Path: " + expectedSchemaPath);
        extentTest.info("Expected Schema (pretty):");
        extentTest.info(MarkupHelper.createCodeBlock(expectedSchemaPretty, CodeLanguage.JSON));

        // -------- Schema Validation (SchemaCompareUtil only) --------
        try {
            SchemaCompareUtil.compareSchemas(actualSchemaPretty, expectedSchemaPretty);
            extentTest.pass("Schema Validation Passed (SchemaCompareUtil)");
        } catch (AssertionError e) {
            extentTest.fail("Schema Validation Failed (SchemaCompareUtil)");
            extentTest.info("Expected Schema:\n\n" + expectedSchemaPretty);
            extentTest.info("Actual Schema:\n\n" + actualSchemaPretty);
            throw e;
        }

        // -------- OPTIONAL structural assertion removed --------
        // Previously:
        // boolean schemasMatch = actualSchemaNode.equals(expectedSchemaNode);
        // SoftAssert sa = new SoftAssert();
        // sa.assertTrue(
        //     schemasMatch,
        //     String.format("SCHEMA MISMATCH!%s%s", expectedSchemaPretty, actualSchemaPretty)
        // );
        // sa.assertAll();
        // extentTest.info("Exact Match: " + schemasMatch);

        extentTest.pass("Schema Validation PASSED (SchemaCompareUtil comparison only)");
        extentTest.info(MarkupHelper.createCodeBlock(
                truncateJson(expectedSchemaPretty, 1500), CodeLanguage.JSON));
        extentTest.info(MarkupHelper.createCodeBlock(
                truncateJson(actualSchemaPretty, 1500), CodeLanguage.JSON));
    }

    // -------- New schema helpers --------

    private JsonNode generateFixedSchema(JsonNode rootNode) {
        if (rootNode == null) {
            return null;
        }
        return generateFullSchema(rootNode);
    }
 
    /**
     * Recursive JSON schema builder that:
     * - Sets type for each node
     * - For objects, sets properties + required for all present fields
     */
    private ObjectNode generateFullSchema(JsonNode node) {
        ObjectNode schema = mapper.createObjectNode();

        if (node.isObject()) {
            schema.put("type", "object");

            ObjectNode properties = mapper.createObjectNode(); 
            ArrayNode requiredArray = mapper.createArrayNode();

            node.fieldNames().forEachRemaining(field -> {
                JsonNode child = node.get(field);
                properties.set(field, generateFullSchema(child));
                // naive approach: mark every present field as required
                requiredArray.add(field);
            });

            schema.set("properties", properties);
            if (requiredArray.size() > 0) {
                schema.set("required", requiredArray);
            }
            schema.put("additionalProperties", false);

        } else if (node.isArray()) {
            schema.put("type", "array");
            if (node.size() > 0) {
                schema.set("items", generateFullSchema(node.get(0)));
            } else {
                ObjectNode items = mapper.createObjectNode();
                items.put("type", "object");
                schema.set("items", items);
            }

        } else if (node.isTextual()) {
            schema.put("type", "string");
        } else if (node.isNumber()) {
            schema.put("type", "number"); // integer/float
        } else if (node.isBoolean()) {
            schema.put("type", "boolean");
        } else if (node.isNull()) {
            schema.put("type", "null");
        } else {
            schema.put("type", "string");
        }

        return schema;
    }

    private String truncateJson(String json, int maxLength) {
        if (json == null) return null;
        if (json.length() <= maxLength) return json;
        return json.substring(0, maxLength) + " ... truncated";
    }
}
