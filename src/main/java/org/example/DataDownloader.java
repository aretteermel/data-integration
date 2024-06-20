package org.example;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.zip.*;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataDownloader {
    public void run() {
        System.out.println("Running DataDownloader...");
        String url = "https://avaandmed.ariregister.rik.ee/sites/default/files/avaandmed/ettevotja_rekvisiidid__maarused.json.zip";
        String zipFilePath = "data.zip";
        String destDir = "output";
        String jsonFilePath = destDir + File.separator + "ettevotja_rekvisiidid__maarused.json";

        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, Paths.get(zipFilePath), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Zip-fail allalaaditud: " + zipFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        File dir = new File(destDir);
        if (!dir.exists()) dir.mkdirs();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String filePath = destDir + File.separator + zipEntry.getName();
                if (!zipEntry.isDirectory()) {
                    extractFile(zis, filePath);
                } else {
                    File dirEntry = new File(filePath);
                    dirEntry.mkdirs();
                }
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonFactory jsonFactory = new JsonFactory();

        try (JsonParser jsonParser = jsonFactory.createParser(new File(jsonFilePath))) {
            while (!jsonParser.isClosed()) {
                JsonNode jsonNode = objectMapper.readTree(jsonParser);
                if (jsonNode != null) {
                    System.out.println("good job!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void extractFile(ZipInputStream zis, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zis.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}


