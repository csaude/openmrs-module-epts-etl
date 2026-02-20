package org.openmrs.module.epts.etl.utilities.tools;


import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CsvColumnRemover {

    private static final String COLUMN_TO_REMOVE = "exclude";

    public static void main(String[] args) throws IOException {

        List<String> directories = List.of(
                "/home/jpboane/org/C-SAUDE/PROJECTOS/Centralizacao/Data-Extraction/03_prep/2026/data"
        );

        for (String dir : directories) {
            processDirectory(Paths.get(dir));
        }

        System.out.println("Processamento concluído.");
    }

    /**
     * Percorre recursivamente o diretório procurando ficheiros CSV
     */
    private static void processDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            System.out.println("Diretório não existe: " + dir);
            return;
        }

        Files.walk(dir)
                .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                .forEach(CsvColumnRemover::processCsvFile);
    }

    /**
     * Remove a coluna "exclude" de um ficheiro CSV
     */
    private static void processCsvFile(Path csvPath) {
        System.out.println("Processando: " + csvPath);

        List<String> outputLines = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {

            String header = reader.readLine();
            if (header == null) return;

            String[] columns = header.split(",");
            int removeIndex = -1;

            for (int i = 0; i < columns.length; i++) {
                if (columns[i].trim().equalsIgnoreCase(COLUMN_TO_REMOVE)) {
                    removeIndex = i;
                    break;
                }
            }

            // Se não existir a coluna, não faz nada
            if (removeIndex == -1) {
                System.out.println("Coluna 'exclude' não encontrada.");
                return;
            }

            // Novo header sem a coluna removida
            outputLines.add(removeColumn(columns, removeIndex));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",", -1); // mantém vazios
                outputLines.add(removeColumn(values, removeIndex));
            }

            // Sobrescreve o ficheiro original
            Files.write(csvPath, outputLines);

        } catch (Exception e) {
            System.err.println("Erro ao processar " + csvPath + ": " + e.getMessage());
        }
    }

    /**
     * Remove um índice específico de um array e retorna linha CSV
     */
    private static String removeColumn(String[] arr, int removeIndex) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            if (i == removeIndex) continue;

            if (sb.length() > 0) sb.append(",");
            sb.append(arr[i]);
        }

        return sb.toString();
    }
}
