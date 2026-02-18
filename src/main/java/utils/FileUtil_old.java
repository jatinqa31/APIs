package utils;

import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil_old {
    public static String readFileAsString(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read schema file: " + filePath, e);
        }
    }
}
