package dataprovider;

import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.*;
import org.testng.annotations.DataProvider;

import java.util.*;

public class ApiDataProvider {

    @DataProvider(name = "apiData")
    public static Iterator<Object[]> getApiData() throws FilloException {

        String excelPath = "Postman_APIs.xlsx";
        String query = "SELECT * FROM API where Is_automated='Yes'";

        List<Object[]> testData = new ArrayList<>();

        Fillo fillo = new Fillo();
        Connection connection = fillo.getConnection(excelPath);
        Recordset recordset = connection.executeQuery(query);

        List<String> columns = recordset.getFieldNames();

        while (recordset.next()) {
            Map<String, String> rowData = new HashMap<>();

            for (String column : columns) {
                rowData.put(column, recordset.getField(column));
            }

            testData.add(new Object[]{rowData});
        }

        recordset.close();
        connection.close();

        return testData.iterator();
    }
}
