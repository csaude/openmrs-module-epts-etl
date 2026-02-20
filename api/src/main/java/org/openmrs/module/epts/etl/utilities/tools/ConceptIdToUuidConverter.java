package org.openmrs.module.epts.etl.utilities.tools;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConceptIdToUuidConverter {

    private static final String INPUT_FILE = "sesp_form.md";
    private static final String OUTPUT_FILE = "sesp_form_uuid.md";

    // Ajuste conforme o seu ambiente
    private static final String JDBC_URL =
            "jdbc:mysql://localhost:3306/openmrs?useSSL=false&serverTimezone=UTC";
    
    private static final String JDBC_USER = "openmrs";
    private static final String JDBC_PASS = "openmrs";

    private static final Pattern CONCEPT_ID_PATTERN =
            Pattern.compile("conceptId=(\\d+)");

    public static void main(String[] args) throws Exception {

        String content = Files.readString(Path.of(INPUT_FILE));

        Map<Integer, String> conceptCache = loadConceptMap();

        Matcher matcher = CONCEPT_ID_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            int conceptId = Integer.parseInt(matcher.group(1));
            String uuid = conceptCache.get(conceptId);

            if (uuid == null) {
                throw new RuntimeException("Concept ID não encontrado: " + conceptId);
            }

            matcher.appendReplacement(
                    result,
                    "conceptUuid=" + uuid
            );
        }

        matcher.appendTail(result);

        Files.writeString(Path.of(OUTPUT_FILE), result.toString());

        System.out.println("Conversão concluída com sucesso!");
    }

    /**
     * Carrega todos os concept_id → uuid para um Map
     */
    private static Map<Integer, String> loadConceptMap() throws SQLException {
        Map<Integer, String> map = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT concept_id, uuid FROM concept"
             );
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getInt("concept_id"), rs.getString("uuid"));
            }
        }

        return map;
    }
}
