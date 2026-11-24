package com.affariyet.utils;

import com.affariyet.config.ConfigReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lit test-data/testdata.csv (séparateur « ; », encodage UTF-8).
 * Colonnes : cle;valeur;utilisation
 */
public final class CsvDataReader {

    private static Map<String, String> data;

    private CsvDataReader() {
    }

    public static synchronized String get(String key) {
        if (data == null) {
            data = load();
        }
        String value = data.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Donnée absente de " + ConfigReader.get("testdata.file") + " : " + key);
        }
        return value;
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    private static Map<String, String> load() {
        String file = ConfigReader.get("testdata.file");
        try {
            List<String> lines = Files.readAllLines(Paths.get(file), StandardCharsets.UTF_8);
            Map<String, String> map = new LinkedHashMap<>();
            for (int i = 1; i < lines.size(); i++) { // ligne 0 = en-tête
                String line = lines.get(i).replace("﻿", "").trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] cols = line.split(";", -1);
                if (cols.length >= 2) {
                    map.put(cols[0].trim(), cols[1].trim());
                }
            }
            Log.detail(map.size() + " données de test chargées depuis " + file);
            return map;
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire le fichier de données : " + file, e);
        }
    }
}
