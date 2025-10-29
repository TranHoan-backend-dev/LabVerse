package com.se1853_jv.labverse.data.api.tag;

import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.domain.infrastructure.tag.model.Tag;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TagApi {
    @GET("paper/{id}")
    Call<BaseJsonResponse<List<Tag>>> getByPaper(@Path("id") String id);
    @GET(".")
    Call<BaseJsonResponse<List<Tag>>> getFiveMostPopularTag();
}
