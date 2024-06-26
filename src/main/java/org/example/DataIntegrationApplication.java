package org.example;

import org.example.data_integration.JsonDataProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataIntegrationApplication.class, args);

        JsonDataProcessor jsonDataProcessor = new JsonDataProcessor();
        jsonDataProcessor.run();
    }

}
