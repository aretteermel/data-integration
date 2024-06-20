package org.example;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@SpringBootApplication
public class Main {

    private final DataSource dataSource;

    @Autowired
    public Main(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);

        DataDownloader dataDownloader = new DataDownloader();
        dataDownloader.run();

        JsonToPostgres jsonToPostgres = new JsonToPostgres();
        jsonToPostgres.run();
    }

    @Bean
    public CommandLineRunner runner() {
        return args -> {
            System.out.println("Application started successfully!");

            Flyway flyway = Flyway.configure().dataSource(dataSource).load();
            flyway.migrate();
        };
    }
}
