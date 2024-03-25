package org.example;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceExtractor {
    public static File extractTessdataFolder() throws IOException {
        String tessdataPath = "tessdata"; // JAR中的tessdata路径
        Path tempDirectory = Files.createTempDirectory("tessdata_"); // 创建临时目录

        // 获取ClassLoader
        ClassLoader classLoader = Main.class.getClassLoader();
        // 使用try-with-resources确保InputStream被正确关闭
        try (InputStream stream = classLoader.getResourceAsStream(tessdataPath)) {
            if (stream == null) {
                throw new IOException("Cannot get resource \"" + tessdataPath + "\" from Jar file.");
            }

            // 解压逻辑（简化版）
            // 注意：这里假设tessdata目录下的文件结构是平坦的，对于嵌套目录需要额外的逻辑来处理
            java.util.zip.ZipInputStream zip = new java.util.zip.ZipInputStream(stream);
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                File file = new File(tempDirectory.toFile(), entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zip.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
            }
        }

        return tempDirectory.toFile();
    }
}