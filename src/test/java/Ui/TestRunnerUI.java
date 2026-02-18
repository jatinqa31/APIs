package Ui;

import org.testng.TestNG;
//import tests.ApiExcelDrivenTest_old;
import tests.ApiExcelDrivenTest;
import utils.PostmanJsonToExcel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class TestRunnerUI extends JFrame {

    // Upload Excel
    private JTextField excelPathField;
    private File excelFile;

    // Upload Postman JSON
    private JTextField postmanJsonPathField;
    private File postmanJsonFile;

    private JButton submitButton;

    public TestRunnerUI() {
        setTitle("API Test Runner");
        setSize(700, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        /* ---------------- Upload Postman JSON (TOP) ---------------- */
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Upload Postman JSON:"), gbc);

        postmanJsonPathField = new JTextField(30);
        postmanJsonPathField.setEditable(false);
        gbc.gridx = 1;
        panel.add(postmanJsonPathField, gbc);

        JButton uploadPostmanJsonBtn = new JButton("Upload Response JSON");
        gbc.gridx = 2;
        panel.add(uploadPostmanJsonBtn, gbc);

        uploadPostmanJsonBtn.addActionListener(e -> { 
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                postmanJsonFile = chooser.getSelectedFile();
                postmanJsonPathField.setText(postmanJsonFile.getAbsolutePath());
            }
        });

        /* ---------------- Upload Excel (BELOW) ---------------- */
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Excel File:"), gbc);

        excelPathField = new JTextField(30);
        excelPathField.setEditable(false);
        gbc.gridx = 1;
        panel.add(excelPathField, gbc);

        JButton uploadExcelBtn = new JButton("Upload Excel");
        gbc.gridx = 2;
        panel.add(uploadExcelBtn, gbc);

        uploadExcelBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xlsx"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                excelFile = chooser.getSelectedFile();
                excelPathField.setText(excelFile.getAbsolutePath());
            }
        });

        /* ---------------- Submit Button (BOTTOM) ---------------- */
        submitButton = new JButton("Submit & Run");
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(submitButton, gbc);

        submitButton.addActionListener(e -> runAll());

        add(panel);
    }

    /* ================= MAIN EXECUTION FLOW ================= */

    private void runAll() {
        try {
            // 1️⃣ Convert Postman JSON ➜ Excel
            if (postmanJsonFile != null) {
                runPostmanJsonToExcel(postmanJsonFile);
            }

            // 2️⃣ Run TestNG execution
            runTestNGTests();

            JOptionPane.showMessageDialog(
                    this,
                    "Execution completed successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Execution failed:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /* ================= POSTMAN JSON ➜ EXCEL ================= */

    private void runPostmanJsonToExcel(File jsonFile) throws Exception {
        String outputExcelPath =
                "C:\\Eclipse_workspace_3\\APIs_validations\\Postman_APIs.xlsx";

        PostmanJsonToExcel converter = new PostmanJsonToExcel();
        converter.convert(jsonFile.getAbsolutePath(), outputExcelPath);

        System.out.println("Postman JSON converted to Excel successfully");
    }

    /* ================= TESTNG EXECUTION ================= */

    private void runTestNGTests() {
        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[]{ApiExcelDrivenTest.class});
        testng.run();
    }

    /* ================= MAIN ================= */

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TestRunnerUI().setVisible(true));
    }
}
