package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;

public class PostmanJsonToExcel {

    private static final String[] HEADERS = {
            "Test Case Id",
            "Request Name",
            "Method",
            "Endpoint",
            "Headers",
            "Query Params",
            "Request Body",
            "ExpectedSchemaPath",
            "Auth Type",
            "Expected Status",
            "Remarks", 
            "Is_automated"
    };

    /**
     * ✅ This method is called from TestRunnerUI
     */
    public void convert(String jsonPath, String excelPath) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(new File(jsonPath));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("API");

        // -------- Create Header Row --------
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            headerRow.createCell(i).setCellValue(HEADERS[i]);
        }

        JsonNode items = rootNode.path("item");
        int rowNum = 1;
        int testCaseId = 1;

        for (JsonNode item : items) {

            Row row = sheet.createRow(rowNum++);

            String requestName = item.path("name").asText("");
            JsonNode request = item.path("request");

            String method = request.path("method").asText("");
            String endpoint = request.path("url").path("raw").asText("");

            // -------- Headers --------
            StringBuilder headers = new StringBuilder();
            for (JsonNode h : request.path("header")) {
                headers.append(h.path("key").asText())
                        .append("=")
                        .append(h.path("value").asText())
                        .append("; ");
            }

            // -------- Query Params --------
            StringBuilder queryParams = new StringBuilder();
            for (JsonNode q : request.path("url").path("query")) {
                queryParams.append(q.path("key").asText())
                        .append("=")
                        .append(q.path("value").asText())
                        .append("; ");
            }

            // -------- Request Body --------
            String requestBody = request.path("body").path("raw").asText("");

            // -------- Auth Type --------
            String authType = request.path("auth").path("type").asText("noauth");

            // -------- Fill Row --------
            row.createCell(0).setCellValue("TC_" + testCaseId++);
            row.createCell(1).setCellValue(requestName);
            row.createCell(2).setCellValue(method);
            row.createCell(3).setCellValue(endpoint);
            row.createCell(4).setCellValue(headers.toString());
            row.createCell(5).setCellValue(queryParams.toString());
            row.createCell(6).setCellValue(requestBody);
            row.createCell(7).setCellValue("Schemas/user-schema_1.json");          // ExpectedSchemaPath
            row.createCell(8).setCellValue(authType);
            row.createCell(9).setCellValue("200");       // Expected Status
            row.createCell(10).setCellValue("");         // Remarks
            row.createCell(11).setCellValue("Yes");      // Is_automated
        }

        // -------- Auto-size columns --------
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }
 
        // -------- Write Excel --------
        try (FileOutputStream fos = new FileOutputStream(excelPath)) {
            workbook.write(fos);
        } 
        workbook.close();

        System.out.println("✅ Excel created successfully: " + excelPath);
    }

    /**
     * Optional standalone run
     */
    public static void main(String[] args) throws Exception {
        new PostmanJsonToExcel().convert(
                "reqres_collection.json",
                "Postman_APIs.xlsx"
        );
    }
}
