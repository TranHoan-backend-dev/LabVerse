package com.se1853_jv.labverse.data.service.unpaywall;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.dto.response.UnpaywallResponse;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UnpaywallService {
    private static final String UNPAYWALL_API_URL = "https://api.unpaywall.org/v2/";
    private static final String UNPAYWALL_EMAIL = "hoana5k44nknd@gmail.com";
    private final String TAG_NAME = "Unpaywall Service";

    private final Retrofit retrofit;
    private UnpaywallApi api;

    public UnpaywallService() {
        var gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .create();

        var logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        var client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        this.retrofit = new Retrofit.Builder()
                .baseUrl(UNPAYWALL_API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        api = retrofit.create(UnpaywallApi.class);
    }

    public void getPdfUrl(String doi, ApiCallback<String> callback) {
        Call<BaseJsonResponse<UnpaywallResponse>> call = api.getUrlForPdf(doi, UNPAYWALL_EMAIL);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseJsonResponse<UnpaywallResponse>> call,
                    @NonNull Response<BaseJsonResponse<UnpaywallResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var uri = response.body();
                    Log.d(TAG_NAME, uri.toString());
                    callback.onSuccess(uri.toString());
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseJsonResponse<UnpaywallResponse>> call,
                    @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
