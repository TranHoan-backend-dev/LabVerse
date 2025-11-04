package com.se1853_jv.labverse.data.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.se1853_jv.labverse.domain.infrastructure.BibEntry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
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

    private static final Gson gson = new Gson();

    /**
     * Đọc file JSON trong thư mục assets và ánh xạ vào kiểu chỉ định.
     *
     * @param context  context của ứng dụng
     * @param fileName tên file JSON (ví dụ: "discovery.json")
     * @param typeOfT  kiểu dữ liệu cần ánh xạ (vd: new TypeToken<List<MyModel>>(){}.getType())
     * @param <T>      kiểu kết quả mong muốn
     * @return đối tượng hoặc danh sách đối tượng được parse, null nếu lỗi
     */
    @Nullable
    public static <T> T fromJsonAsset(Context context, String fileName, Type typeOfT) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open(fileName))
        )) {
            return gson.fromJson(reader, typeOfT);
        } catch (Exception e) {
            Log.e("ParseFileUtils", "Error parsing JSON from asset: " + e.getMessage());
            return null;
        }
    }

    /**
     * Ánh xạ nội dung JSON string trực tiếp thành object.
     *
     * @param jsonString chuỗi JSON
     * @param classOfT   kiểu class cần ánh xạ
     * @param <T>        kiểu kết quả mong muốn
     * @return đối tượng được parse, null nếu lỗi
     */
    @Nullable
    public static <T> T fromJsonString(String jsonString, Class<T> classOfT) {
        try {
            return gson.fromJson(jsonString, classOfT);
        } catch (Exception e) {
            Log.e("ParseFileUtils", "Error parsing JSON from asset: " + e.getMessage());
            return null;
        }
    }

    /**
     * Chuyển object Java thành chuỗi JSON.
     *
     * @param object object cần chuyển
     * @return chuỗi JSON
     */
    public static String toJson(Object object) {
        return gson.toJson(object);
    }
}
