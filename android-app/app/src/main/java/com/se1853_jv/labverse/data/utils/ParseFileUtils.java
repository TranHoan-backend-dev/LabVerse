package com.se1853_jv.labverse.data.utils;

import androidx.annotation.NonNull;

import com.se1853_jv.labverse.domain.infrastructure.BibEntry;

import java.util.ArrayList;
import java.util.List;

public class ParseFileUtils {
    @NonNull
    public static List<BibEntry> parseBibEntries(@NonNull String bibContent) {
        List<BibEntry> entries = new ArrayList<>();
        String[] rawEntries = bibContent.split("@");

        for (var raw : rawEntries) {
            if (raw.trim().isEmpty()) continue;

            var entry = new BibEntry();
            entry.setType(raw.substring(0, raw.indexOf("{")).trim());

            var body = raw.substring(raw.indexOf("{") + 1);
            body = body.replaceAll("\\}\\s*$", ""); // remove ending brace

            // tach field theo key = {value}
            for (var line : body.split(",")) {
                if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    var key = parts[0].trim().toLowerCase();
                    var value = parts[1].replaceAll("[{}\"]", "").trim();

                    switch (key) {
                        case "title" -> entry.setTitle(value);
                        case "author" -> entry.setAuthor(value);
                        case "year" -> entry.setYear(value);
                        case "journal", "booktitle" -> entry.setSource(value);
                        case "page" -> entry.setPages(value);
                        case "doi" -> entry.setDoi(value);
                    }
                }
            }
            entries.add(entry);
        }
        return entries;
    }
}
