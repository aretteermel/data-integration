package org.example.data_integration;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JsonDataProcessor {
    private static final String URL = "https://avaandmed.ariregister.rik.ee/sites/default/files/avaandmed/ettevotja_rekvisiidid__maarused.json.zip";
    private static final String ZIP_FILE_PATH = "data.zip";
    private static final String DEST_DIR = "output";
    private static final String JSON_FILE_NAME = "ettevotja_rekvisiidid__maarused.json";
    private static final String JSON_FILE_PATH = DEST_DIR + File.separator + JSON_FILE_NAME;

    public void run() {
        System.out.println("Running DataDownloader...");

        try {
            downloadFile(URL, ZIP_FILE_PATH);
            extractZip(ZIP_FILE_PATH, DEST_DIR);
            processJsonFile(JSON_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cleanup(ZIP_FILE_PATH);
        }

    }

    private void downloadFile(String url, String zipFilePath) throws IOException {
        try (InputStream inputStream = new URL(url).openStream()) {
            Files.copy(inputStream, Paths.get(zipFilePath), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Zip file downloaded: " + zipFilePath);
        }
    }

    private void extractZip(String zipFilePath, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                String filePath = destDir + File.separator + zipEntry.getName();
                if (!zipEntry.isDirectory()) {
                    extractFile(zipInputStream, filePath);
                } else {
                    new File(filePath).mkdirs();
                }
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();
            }
        }
    }

    private void extractFile(ZipInputStream zis, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zis.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    private void processJsonFile(String jsonFilePath) {
        try {
            JsonToPostgres jsonToPostgres = new JsonToPostgres();
            jsonToPostgres.run(jsonFilePath, getDatabaseUrl(), getDatabaseUser(), getDatabasePassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanup(String zipFilePath) {
        File zipFile = new File(zipFilePath);
        if (zipFile.exists()) {
            if (zipFile.delete()) {
                System.out.println("Temporary zip file deleted: " + zipFilePath);
            } else {
                System.err.println("Failed to delete temporary zip file: " + zipFilePath);
            }
        }
    }

    private String getDatabaseUrl() {
        Properties properties = loadProperties();
        return properties.getProperty("spring.datasource.url");
    }

    private String getDatabaseUser() {
        Properties properties = loadProperties();
        return properties.getProperty("spring.datasource.username");
    }

    private String getDatabasePassword() {
        Properties properties = loadProperties();
        return properties.getProperty("spring.datasource.password");
    }

    public static Properties loadProperties() {
        String PROPERTIES_FILE_PATH = "src/main/resources/application.properties";

        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(PROPERTIES_FILE_PATH)) {
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

}
