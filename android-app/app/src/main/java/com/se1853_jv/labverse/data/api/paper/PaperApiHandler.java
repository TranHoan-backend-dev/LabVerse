package com.se1853_jv.labverse.data.api.paper;

import android.util.Log;

import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaperApiHandler {
    private final String BASE_URL = "http://localhost:8080/v1/api/papers";
    private final PaperApi apiService;

    public PaperApiHandler() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL.concat("/details"))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(PaperApi.class);
    }

    public void getDetails(String id) {
        Call<PaperResearch> call = apiService.getPaperDetails(id);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PaperResearch> call, Response<PaperResearch> response) {
                if (response.isSuccessful()) {
                    System.out.println("hehe");
                } else {
                    // err from server
                    Log.e("Server Error", "Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PaperResearch> call, Throwable t) {
                // err khong ket noi duoc api
                Log.e("API Error", "Error: " + t.getMessage());
            }
        });
    }
}
