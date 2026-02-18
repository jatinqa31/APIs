package utils;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {
 public static String readFileAsString(String path) throws Exception {
     return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
 }
}
