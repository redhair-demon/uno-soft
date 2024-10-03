package org.example;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Main {
    private static final List<Map<String, String>> columns = new ArrayList<>();
    private static final Map<String, Set<String>> groups = new HashMap<>();
    private static final Map<String, String> tree = new HashMap<>();
    private static final String outputFile = "groups.txt";

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java -jar Main.jar <input-file>.txt");
            return;
        }

        String inputFilePath = args[0];
        long startTime = System.currentTimeMillis();
        readTree(inputFilePath);
        long readTreeTime = System.currentTimeMillis();
        processGroups();
        long processGroupTime = System.currentTimeMillis();
        printGroups();
        long endTime = System.currentTimeMillis();
        System.out.printf("read tree = %d, process groups = %d, print = %d, total = %d",
                readTreeTime-startTime, processGroupTime-readTreeTime, endTime-processGroupTime, endTime-startTime);
    }

    private static void readTree(String inputFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            Pattern pattern = Pattern.compile("((\"([^\"]*)\")|(;))+");
            while ((line = reader.readLine()) != null) {
                if (!pattern.matcher(line).matches()) continue;
                processLine(line);
            }
        }
    }

    private static void processLine(String line) {
        String[] cols = line.replace("\"", "").split(";");
        for (int i=0; i<cols.length; i++) {
            String column = cols[i];
            if (columns.size() <= i) {
                columns.add(new HashMap<>());
            }
            if (column.isBlank()) continue;
            Map<String, String> columnMap = columns.get(i);
            if (columnMap.containsKey(column)) {
                union(line, columnMap.get(column));
            } else if (!tree.containsKey(line)) {
                tree.put(line, line);
            }
            columnMap.put(column, line);
        }

    }

    private static String find(String x) {
        while (tree.get(x) != null && !Objects.equals(tree.get(x), x)) {
            x = tree.get(x);
        }
        return x;
    }

    private static void union(String a, String b) {
        String rootA = find(a);
        String rootB = find(b);
        if (!Objects.equals(rootA, rootB)) {
            tree.put(rootA, rootB);
        }
    }

    private static void processGroups() {
        for (String leaf : tree.keySet()) {
            String root = find(leaf);
            groups.computeIfAbsent(root, k -> new HashSet<>()).add(leaf);
        }
    }

    private static void printGroups() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            List<Set<String>> groupsSorted = groups.values().stream().sorted((a, b) -> b.size()-a.size()).toList();
            writer.write("%d\n".formatted(groupsSorted.stream().mapToInt(s -> (s.size()>1) ? 1 : 0).sum()));
            int n = 1;
            for (Set<String> set : groupsSorted) {
                writer.write("Группа %d\n".formatted(n));
                for (String line : set) {
                    writer.write(line);
                    writer.newLine();
                }
                writer.newLine();
                n++;
            }
        }
    }

}
