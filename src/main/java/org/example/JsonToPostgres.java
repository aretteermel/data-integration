package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class JsonToPostgres {
    public void run() {
        System.out.println("Running JsonToPostgres...");

        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String url = properties.getProperty("spring.datasource.url");
        String user = properties.getProperty("spring.datasource.username");
        String password = properties.getProperty("spring.datasource.password");
        String jsonFilePath = "output/ettevotja_rekvisiidid__maarused.json";

        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = new FileInputStream(jsonFilePath)) {
            JsonNode rootNode = objectMapper.readTree(inputStream);

            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                String insertCompanySQL = "INSERT INTO companies (ariregistri_kood, nimi) VALUES (?, ?) ON CONFLICT" +
                        "(ariregistri_kood) DO NOTHING";
                String insertOrderSQL = "INSERT INTO orders (ariregistri_kood, maaruse_nr, maaruse_kpv, kande_kpv," +
                        "lisatahtaeg, maaruse_liik, maaruse_liik_tekstina, maaruse_olek, maaruse_olek_tekstina," +
                        "kandeliik, kandeliik_tekstina, joustumise_kpv, joust_olek, joust_olek_tekstina)" +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement insertCompanyStmt = conn.prepareStatement(insertCompanySQL);
                     PreparedStatement insertOrderStmt = conn.prepareStatement(insertOrderSQL)) {

                    int count = 0;
                    for (JsonNode companyNode : rootNode) {
                        // let's add only 100 first company's info to database
                        if (count++ >= 100) break;
                        long ariregistriKood = companyNode.get("ariregistri_kood").asLong();
                        String nimi = companyNode.get("nimi").asText();

                        insertCompanyStmt.setLong(1, ariregistriKood);
                        insertCompanyStmt.setString(2, nimi);
                        insertCompanyStmt.executeUpdate();

                        JsonNode maarusedNode = companyNode.get("maarused");
                        for (JsonNode maarusNode : maarusedNode) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                            insertOrderStmt.setLong(1, ariregistriKood);
                            insertOrderStmt.setString(2, maarusNode.get("maaruse_nr").asText());

                            String maaruseKpv = maarusNode.has("maaruse_kpv") ? maarusNode.get("maaruse_kpv").asText() : null;
                            try {
                                if (maaruseKpv != null && !maaruseKpv.equals("null") && !maaruseKpv.isEmpty()) {
                                    insertOrderStmt.setDate(3, new java.sql.Date(dateFormat.parse(maaruseKpv).getTime()));
                                } else {
                                    insertOrderStmt.setNull(3, Types.DATE);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            String kandeKpv = maarusNode.has("kande_kpv") ? maarusNode.get("kande_kpv").asText() : null;
                            try {
                                if (kandeKpv != null && !kandeKpv.equals("null") && !kandeKpv.isEmpty()) {
                                    java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse(kandeKpv).getTime());
                                    insertOrderStmt.setDate(4, sqlDate);
                                } else {
                                    insertOrderStmt.setNull(4, Types.DATE);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            String lisatahtaeg = maarusNode.has("lisatahtaeg") ? maarusNode.get("lisatahtaeg").asText() : null;
                            try {
                                if (lisatahtaeg != null && !lisatahtaeg.equals("null") && !lisatahtaeg.isEmpty()) {
                                    insertOrderStmt.setDate(5, new java.sql.Date(dateFormat.parse(lisatahtaeg).getTime()));
                                } else {
                                    insertOrderStmt.setNull(5, Types.DATE);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            insertOrderStmt.setString(6, maarusNode.get("maaruse_liik").asText());
                            insertOrderStmt.setString(7, maarusNode.get("maaruse_liik_tekstina").asText());
                            insertOrderStmt.setString(8, maarusNode.get("maaruse_olek").asText());
                            insertOrderStmt.setString(9, maarusNode.get("maaruse_olek_tekstina").asText());
                            insertOrderStmt.setString(10, maarusNode.get("kandeliik").asText());
                            insertOrderStmt.setString(11, maarusNode.get("kandeliik_tekstina").asText());

                            String joustumiseKpv = maarusNode.has("joustumise_kpv") ? maarusNode.get("joustumise_kpv").asText() : null;
                            try {
                                if (joustumiseKpv != null && !joustumiseKpv.equals("null") && !joustumiseKpv.isEmpty()) {
                                    insertOrderStmt.setDate(12, new java.sql.Date(dateFormat.parse(joustumiseKpv).getTime()));
                                } else {
                                    insertOrderStmt.setNull(12, Types.DATE);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            insertOrderStmt.setString(13, maarusNode.get("joust_olek").asText());
                            insertOrderStmt.setString(14, maarusNode.get("joust_olek_tekstina").asText());

                            insertOrderStmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
