package com.se1853_jv.labverse.data.api.tag;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.domain.infrastructure.tag.model.Tag;


import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TagApiHandler {
    private final TagApi tagApi;

    public TagApiHandler() {
        final var BASE_URL = "http://10.0.2.2:8080/v1/api/tags/";
        var gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .create();

        var logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        var client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        var retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        tagApi = retrofit.create(TagApi.class);
    }

    public void getTagsByPaperId(String id, ApiCallback<List<Tag>> callback) {
        Log.d("Tag_DATA", "getTagsByPaperId: " + id);
        Call<BaseJsonResponse<List<Tag>>> call = tagApi.getByPaper(id);
        call.enqueue(new Callback<>() {

            @Override
            public void onResponse(
                    @NonNull Call<BaseJsonResponse<List<Tag>>> call,
                    @NonNull Response<BaseJsonResponse<List<Tag>>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d("Tag_DATA", "Tag: " + result.toString());
                } else {
                    Log.e("Server Error", "Error: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseJsonResponse<List<Tag>>> call,
                    @NonNull Throwable t
            ) {
                Log.e("API Error", "Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }
}
