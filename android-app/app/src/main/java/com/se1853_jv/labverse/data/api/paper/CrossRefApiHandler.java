package com.se1853_jv.labverse.data.api.paper;

import android.util.Log;

import org.json.JSONObject;

import java.util.Objects;

import javax.annotation.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class CrossRefApiHandler {
    private final OkHttpClient client;

    public CrossRefApiHandler() {
        this.client = new OkHttpClient();
    }

    @Nullable
    public JSONObject getArticleUrlFromDOI(String doi) {
        final String BASE_URL = "https://api.crossref.org/works/";
        try {
            var apiUrl = BASE_URL.concat(doi);
            var request = new Request.Builder()
                    .url(apiUrl)
                    .get()
                    .build();
            Log.d("CrossRefApiHandler", request.toCurl());
            try (var response = client.newCall(request).execute()) {
                Log.d("CrossRefApiHandler response", String.valueOf(response.isSuccessful()));
                if (!response.isSuccessful()) return null;
                var body = response.body().string();
                Log.d("CrossRefApiHandler body", body);
                var json = new JSONObject(body);
                Log.d("CrossRefApiHandler json", json.toString());
                return json.getJSONObject("message");
            }
        } catch (Exception e) {
            Log.e("CrossRefApiHandler", Objects.requireNonNull(e.getMessage()));
            return null;
        }
    }
}
