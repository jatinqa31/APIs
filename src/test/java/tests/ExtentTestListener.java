package tests;

import com.aventstack.extentreports.*;

import java.util.Map;

import org.testng.*;

public class ExtentTestListener implements ITestListener {

    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

//    @Override
//    public void onTestStart(ITestResult result) {
//        ExtentTest extentTest =
//                ExtentManager.getExtent()
//                        .createTest(result.getMethod().getMethodName());
//        test.set(extentTest);
//    }

    @Override
    public void onTestStart(ITestResult result) {
        Object[] params = result.getParameters();
        String testName = "executeApiTest";

        if (params.length > 0 && params[0] instanceof Map) {
            Map<?, ?> data = (Map<?, ?>) params[0];
            testName = data.get("Request Name").toString();
        }

        ExtentTest extentTest =
                ExtentManager.getExtent().createTest(testName);

        test.set(extentTest);
    }

    
    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().pass("Test Passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.get().fail(result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.getExtent().flush();
    }

    public static ExtentTest getTest() {
        return test.get();
    }
}
