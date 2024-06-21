package org.example.data_integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class JsonToPostgres {

    public void run(String jsonFilePath, String url, String user, String password) {
        System.out.println("Running JsonToPostgres...");

        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String insertCompanySQL = "INSERT INTO companies (ariregistri_kood, nimi) VALUES (?, ?) " +
                "ON CONFLICT (ariregistri_kood) DO NOTHING";
        String insertOrderSQL = "INSERT INTO orders (ariregistri_kood, maaruse_nr, maaruse_kpv, kande_kpv, lisatahtaeg, maaruse_liik, maaruse_liik_tekstina, maaruse_olek, maaruse_olek_tekstina, kandeliik, kandeliik_tekstina, joustumise_kpv, joust_olek, joust_olek_tekstina) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                "ON CONFLICT (maaruse_nr) DO NOTHING";
        try (InputStream inputStream = new FileInputStream(jsonFilePath);
             Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement insertCompanyStmt = conn.prepareStatement(insertCompanySQL);
             PreparedStatement insertOrderStmt = conn.prepareStatement(insertOrderSQL)) {

            JsonNode rootNode = objectMapper.readTree(inputStream);
            int count = 0;

            for (JsonNode companyNode : rootNode) {
                // let's add only 100 first company's info to database
                if (count++ >= 100) break;

                long companyCode = companyNode.get("ariregistri_kood").asLong();
                String name = companyNode.get("nimi").asText();

                insertCompanyStmt.setLong(1, companyCode);
                insertCompanyStmt.setString(2, name);
                insertCompanyStmt.executeUpdate();

                JsonNode regulationsNode = companyNode.get("maarused");
                for (JsonNode regulationNode : regulationsNode) {
                    setOrderStatementParams(insertOrderStmt, companyCode, regulationNode);
                    insertOrderStmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setOrderStatementParams(PreparedStatement insertOrderStmt, long companyCode, JsonNode regulationNode) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        insertOrderStmt.setLong(1, companyCode);
        insertOrderStmt.setString(2, regulationNode.get("maaruse_nr").asText());

        setDateOrNull(insertOrderStmt, 3, regulationNode, "maaruse_kpv", dateFormat);
        setDateOrNull(insertOrderStmt, 4, regulationNode, "kande_kpv", dateFormat);
        setDateOrNull(insertOrderStmt, 5, regulationNode, "lisatahtaeg", dateFormat);

        insertOrderStmt.setString(6, regulationNode.get("maaruse_liik").asText());
        insertOrderStmt.setString(7, regulationNode.get("maaruse_liik_tekstina").asText());
        insertOrderStmt.setString(8, regulationNode.get("maaruse_olek").asText());
        insertOrderStmt.setString(9, regulationNode.get("maaruse_olek_tekstina").asText());
        insertOrderStmt.setString(10, regulationNode.get("kandeliik").asText());
        insertOrderStmt.setString(11, regulationNode.get("kandeliik_tekstina").asText());

        setDateOrNull(insertOrderStmt, 12, regulationNode, "joustumise_kpv", dateFormat);

        insertOrderStmt.setString(13, regulationNode.get("joust_olek").asText());
        insertOrderStmt.setString(14, regulationNode.get("joust_olek_tekstina").asText());
    }

    private void setDateOrNull(PreparedStatement insertOrderStmt, int index, JsonNode node, String fieldName, SimpleDateFormat dateFormat) throws Exception {
        String fieldValue = node.has(fieldName) ? node.get(fieldName).asText() : null;
        if (fieldValue != null && !fieldValue.equals("null") && !fieldValue.isEmpty()) {
            insertOrderStmt.setDate(index, new java.sql.Date(dateFormat.parse(fieldValue).getTime()));
        } else {
            insertOrderStmt.setNull(index, Types.DATE);
        }
    }
}
