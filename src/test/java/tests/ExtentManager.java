package tests;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ExtentManager {

    private static ExtentReports extent;
    
    //additional lines of code added by me for custom testName
    //private static ExtentReports extent;

    public static ExtentReports getExtent() {
        if (extent == null) {
            getInstance();
        }
        return extent;
    }
    
    //-------------------Adding again to remote repository-------------------------------------
    

    public static ExtentReports getInstance() {

        if (extent == null) {

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
                    .format(new Date());

//            String reportPath =
////                    "target/extent/ApiAutomationReport_" + timeStamp + ".html";
//                    "target/extent/ApiAutomationReport.html";
            
            String reportPath = System.getProperty("user.dir") + "/target/ExtentReport.html";
            ExtentSparkReporter spark =
                    new ExtentSparkReporter(reportPath);

            //below 2 lines are added by me for jenkins report generation
//            ExtentReports extent = new ExtentReports();
//            extent.attachReporter(spark);
            
            spark.config().setReportName("API Automation Report");
            spark.config().setDocumentTitle("REST Assured + Excel Framework");

            extent = new ExtentReports();
            extent.attachReporter(spark);

            extent.setSystemInfo("Framework", "Excel Driven");
            extent.setSystemInfo("Tester", "Jatin");
            extent.setSystemInfo("Environment", "QA");
        
        //additional lines of code added by me to add the TestCase nam customized
            //createInstance();
        }

        return extent;
    }
    
    private static void createInstance() {

        String reportPath = System.getProperty("user.dir")
                + "/test-output/ExtentReport.html";

        ExtentSparkReporter sparkReporter =
                new ExtentSparkReporter(reportPath);

        sparkReporter.config().setReportName("API Automation Report");
        sparkReporter.config().setDocumentTitle("Execution Results");

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);

        // Optional system info
        extent.setSystemInfo("Framework", "Excel Driven");
        extent.setSystemInfo("Tester", "Jatin");
        extent.setSystemInfo("Environment", "QA");
        }
}
